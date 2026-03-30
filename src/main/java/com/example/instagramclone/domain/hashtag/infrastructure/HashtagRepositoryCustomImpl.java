package com.example.instagramclone.domain.hashtag.infrastructure;


import com.example.instagramclone.domain.post.api.ProfilePostResponse;
import com.example.instagramclone.domain.post.domain.Post;
import com.example.instagramclone.domain.post.infrastructure.PostGridQueryHelper;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;

import static com.example.instagramclone.domain.hashtag.domain.QHashtag.hashtag;
import static com.example.instagramclone.domain.hashtag.domain.QPostHashtag.postHashtag;
import static com.example.instagramclone.domain.post.domain.QPost.post;

@Repository
@RequiredArgsConstructor
public class HashtagRepositoryCustomImpl implements HashtagRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final PostGridQueryHelper postGridQueryHelper;

    @Override
    public Slice<ProfilePostResponse> findProfilePostSliceByHashtagName(String normalizedHashtagName, Pageable pageable) {
        JPQLQuery<Post> baseQuery = queryFactory
                .selectFrom(post)
                .innerJoin(postHashtag).on(postHashtag.post.eq(post))
                .innerJoin(hashtag).on(postHashtag.hashtag.eq(hashtag))
                .where(hashtagNameEq(normalizedHashtagName))
                .orderBy(post.id.desc());

        return postGridQueryHelper.findProfilePostSlice(baseQuery, pageable);
    }

    /**
     * 정규화된 태그명으로 필터 — 이후 공개 범위·삭제 여부 등과 AND 로 묶기 쉽게 한곳에 둔다.
     */
    private static BooleanExpression hashtagNameEq(String normalizedHashtagName) {
        return hashtag.name.eq(normalizedHashtagName);
    }
}
