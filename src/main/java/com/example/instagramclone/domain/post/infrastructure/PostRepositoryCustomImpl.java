package com.example.instagramclone.domain.post.infrastructure;

import com.example.instagramclone.domain.post.api.ProfilePostResponse;
import com.example.instagramclone.domain.post.domain.QPostImage;
import com.example.instagramclone.domain.post.domain.Post;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

import static com.example.instagramclone.domain.post.domain.QPost.*;
import static com.example.instagramclone.domain.post.domain.QPostLike.*;
import static com.example.instagramclone.domain.comment.domain.QComment.comment;

/**
 * PostRepositoryCustom의 QueryDSL 구현체입니다.
 * 기존 @Query JPQL 피드 조회 쿼리를 타입 세이프한 QueryDSL 코드로 대체합니다.
 *
 * [네이밍 컨벤션 필수]
 * Spring Data JPA는 fragment 인터페이스명 + "Impl" 접미사로 구현체를 탐색합니다.
 * PostRepositoryCustom → PostRepositoryCustomImpl (같은 패키지에 위치해야 함)
 *
 * [JPQL → QueryDSL 변환]
 * JPQL:    "SELECT p FROM Post p JOIN FETCH p.writer"
 * QueryDSL: post.writer 를 fetchJoin() 으로 연결 → 컴파일 타임에 오타 검증 가능
 */
@Repository
@RequiredArgsConstructor
public class PostRepositoryCustomImpl implements PostRepositoryCustom {

    private final JPAQueryFactory queryFactory;


    /**
     * 특정 회원의 게시글을 최신순으로 페이징 조회합니다.
     *
     */
    @Override
    public Slice<ProfilePostResponse> findAllByWriterId(Long writerId, Pageable pageable) {
        /*
         * Native SQL(동등 로직) - 극단적 1쿼리(one query)
         *
         * 개념상 select 절에 상관 서브쿼리 3개를 같이 올립니다.
         * - thumbnail_url: img_order = 1 이미지 1건 (없으면 null)
         * - multiple_images: img_order > 1 존재 여부
         * - comment_count: 원댓글(parent_id is null) 개수
         *
         SELECT
             p.id,
             p.content,
             p.member_id,
             (SELECT pi.image_url
                  FROM post_images pi
                  WHERE pi.post_id = p.id
                    AND pi.img_order = 1
                  LIMIT 1) AS thumbnail_url,

              EXISTS(
                  SELECT 1
                   FROM post_images pi2
                   WHERE pi2.post_id = p.id
                     AND pi2.img_order > 1
                ) AS multiple_images,

               (SELECT COUNT(c.id)
                   FROM comments c
                  WHERE c.post_id = p.id
                    AND c.parent_id IS NULL) AS comment_count,

              p.like_count
          FROM posts p
          WHERE p.member_id = 1
          ORDER BY p.id DESC
          LIMIT 5
          OFFSET 0;

         * - 이 쿼리는 limit(pageSize + 1)로 hasNext를 판단한 뒤 마지막 row를 제거합니다.
         */
        // 1) imgOrder=1 썸네일(imageUrl) + imgOrder>1 존재 여부로 multipleImages 계산
        QPostImage pi = QPostImage.postImage;

        Expression<String> thumbnailExpr = JPAExpressions
                .select(pi.imageUrl)
                .from(pi)
                .where(
                        pi.post.id.eq(post.id),
                        pi.imgOrder.eq(1)
                )
                .limit(1);

        BooleanExpression multipleImagesExpr = JPAExpressions
                .selectOne()
                .from(pi)
                .where(
                        pi.post.id.eq(post.id),
                        pi.imgOrder.gt(1)
                )
                .exists();

        // 2) 댓글 수도 같은 SELECT에 넣기 (원댓글 only, parent is null)
        Expression<Long> commentCountExpr = JPAExpressions
                .select(comment.id.count())
                .from(comment)
                .where(
                        comment.post.id.eq(post.id),
                        comment.parent.isNull()
                );

        // 3) 타겟 유저 게시물 + 썸네일/다중 여부/댓글 수를 한 번에 조회
        // limit + 1로 Slice hasNext 판별
        List<Tuple> tuples = queryFactory
                .select(post, thumbnailExpr, multipleImagesExpr, commentCountExpr)
                .from(post)
                .where(post.writer.id.eq(writerId))
                .orderBy(post.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1L)
                .fetch();

        if (tuples.isEmpty()) {
            return new SliceImpl<>(Collections.emptyList(), pageable, false);
        }

        boolean hasNext = tuples.size() > pageable.getPageSize();
        if (hasNext) {
            tuples = tuples.subList(0, pageable.getPageSize());
        }

        // 4) 최종 DTO 조립
        List<ProfilePostResponse> content = tuples.stream()
                .map(t -> {
                    Post p = t.get(post);
                    String thumbnailUrl = t.get(1, String.class);
                    Boolean multipleImages = t.get(2, Boolean.class);
                    Long commentCount = t.get(3, Long.class);

                    long likeCount = p.getLikeCount(); // 비정규화 컬럼 그대로 사용
                    long commentCountInt = commentCount != null ? commentCount : 0L;

                    return new ProfilePostResponse(
                            p.getId(),
                            thumbnailUrl,
                            Boolean.TRUE.equals(multipleImages),
                            likeCount,
                            commentCountInt
                    );
                })
                .toList();

        return new SliceImpl<>(content, pageable, hasNext);
    }

    /**
     * 메인 피드: Post + writer fetchJoin + 로그인 회원 기준 liked.
     *
     * <p><b>현재 구현 — EXISTS</b> (아래 쿼리와 동등한 의미)</p>
     * <pre>
     * SELECT p.*, EXISTS (
     *   SELECT 1 FROM post_like pl
     *   WHERE pl.post_id = p.id AND pl.member_id = ?
     * ) AS liked
     * FROM posts p INNER JOIN member ... (writer fetch)
     * ORDER BY p.id DESC
     * </pre>
     * Post 행이 늘지 않아 조인/fetch 확장 시에도 안전함.
     *
     * <p><b>대안 — LEFT JOIN + id IS NOT NULL (수업·실무에서 동일 논리로 자주 씀)</b></p>
     * PostLike를 한 유저·한 글당 최대 1행으로 두는 전제(복합 유니크)에서,
     * 조인에 걸리면 true, 안 걸리면 false를 DB가 바로 뽑아줄 수 있음.
     * <pre>
     * // QueryDSL 예시 (구현은 EXISTS 유지)
     * queryFactory
     *     .select(post, postLike.id.isNotNull())   // liked = 조인 성공 여부
     *     .from(post)
     *     .join(post.writer).fetchJoin()
     *     .leftJoin(postLike).on(
     *         postLike.post.eq(post).and(postLike.member.id.eq(loginMemberId))
     *     )
     *     .orderBy(post.id.desc())
     *     ...
     * </pre>
     * 주의: 같은 쿼리에서 PostImage 등으로 행이 늘어나는 join과 섞으면 post당 여러 행이 될 수 있음.
     * 그때는 DISTINCT 또는 EXISTS 쪽이 단순함. 지금처럼 writer만 fetch하면 LEFT JOIN PostLike는 post당 1행 유지.
     */
    @Override
    public Slice<PostFeedRow> findFeedWithLiked(Pageable pageable, Long loginMemberId) {

        // EXISTS 방식: semi join, 결과 행 수 = Post 행 수

        BooleanExpression likedExpr = JPAExpressions
                .selectFrom(postLike)
                .where(postLike.post.eq(post).and(postLike.member.id.eq(loginMemberId)))
                .exists();

        List<Tuple> tuples = queryFactory
                .select(post, likedExpr)
                .from(post)
                .join(post.writer).fetchJoin()
                .orderBy(post.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1L)
                .fetch();


        boolean hasNext = tuples.size() > pageable.getPageSize();
        if (hasNext) {
            tuples = tuples.subList(0, pageable.getPageSize());
        }

        List<PostFeedRow> rows = tuples.stream()
                .map(t -> new PostFeedRow(
                        t.get(post),
                        toBoolean(t.get(likedExpr))))
                .toList();

        return new SliceImpl<>(rows, pageable, hasNext);
    }

    private static boolean toBoolean(Object o) {
        if (o == null) {
            return false;
        }
        if (o instanceof Boolean b) {
            return b;
        }
        if (o instanceof Number n) {
            return n.intValue() != 0;
        }
        return false;
    }

    @Override
    public PrevNextPostIds findPrevAndNextPostIdByProfile(Long memberId, Long postId) {
        Long prevPostId = queryFactory
                .select(post.id)
                .from(post)
                .where(
                        post.writer.id.eq(memberId),
                        post.id.gt(postId)
                )
                .orderBy(post.id.asc())
                .fetchFirst();

        Long nextPostId = queryFactory
                .select(post.id)
                .from(post)
                .where(
                        post.writer.id.eq(memberId),
                        post.id.lt(postId)
                )
                .orderBy(post.id.desc())
                .fetchFirst();

        return new PrevNextPostIds(prevPostId, nextPostId);
    }
}
