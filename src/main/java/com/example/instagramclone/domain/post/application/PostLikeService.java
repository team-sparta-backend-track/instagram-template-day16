package com.example.instagramclone.domain.post.application;

import com.example.instagramclone.core.exception.PostErrorCode;
import com.example.instagramclone.core.exception.PostException;
import com.example.instagramclone.domain.member.application.MemberService;
import com.example.instagramclone.domain.member.domain.Member;
import com.example.instagramclone.domain.post.api.LikeStatusResponse;
import com.example.instagramclone.domain.post.domain.Post;
import com.example.instagramclone.domain.post.domain.PostLike;
import com.example.instagramclone.domain.post.domain.PostLikeRepository;
import com.example.instagramclone.domain.post.domain.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * [Day 12 Step 2~3] 좋아요 토글 API.
 *
 * - Step 2: PostLike Insert/Delete 분기
 * - Step 3: Post.likeCount 비정규화 (+1 / -1). 응답 likeCount는 COUNT(*) 대신 post.likeCount 사용.
 *   ※ 동시에 같은 글에 좋아요가 몰리면 lost update 가능 → 나중 강의에서 락으로 다룸.
 * ※ 취소 직후 DB 오류 등으로 post_like와 likeCount가 어긋날 수 있음 → 배치 보정 등 실무 대응.
 */
@Service
@RequiredArgsConstructor
public class PostLikeService {

    private final PostLikeRepository postLikeRepository;
    private final PostRepository postRepository;
    private final MemberService memberService;

    /**
     * 좋아요 토글: 이미 누른 상태면 취소, 아니면 추가.
     * 비정규화 likeCount는 삭제/저장과 같은 트랜잭션에서 -1 / +1 (응답·피드에서 COUNT 생략).
     */
    @Transactional
    public LikeStatusResponse toggleLike(Long loginMemberId, Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostException(PostErrorCode.POST_NOT_FOUND));

        Member member = memberService.getReferenceById(loginMemberId);
        boolean alreadyLiked = postLikeRepository.existsByMemberAndPost(member, post);

        if (alreadyLiked) {
            postLikeRepository.deleteByMemberAndPost(member, post);
            post.changeLikeCountBy(-1);
            return new LikeStatusResponse(false, post.getLikeCount());
        }
        PostLike postLike = PostLike.create(member, post);
        postLikeRepository.save(postLike);
        post.changeLikeCountBy(1);
        return new LikeStatusResponse(true, post.getLikeCount());
    }
}
