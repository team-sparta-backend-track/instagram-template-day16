package com.example.instagramclone.domain.hashtag.infrastructure;

import com.example.instagramclone.domain.hashtag.domain.Hashtag;
import com.example.instagramclone.domain.post.api.ProfilePostResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;

/**
 * 해시태그 관련 QueryDSL 조회 (JPQL {@code @Query} 문자열 사용하지 않음).
 *
 * <p>TODO Day 16 Step 3: 프로필 그리드 {@link com.example.instagramclone.domain.post.infrastructure.PostRepositoryCustomImpl#findAllByWriterId}
 * 와 동일한 {@link ProfilePostResponse}·Slice 패턴으로, WHERE 만 {@code hashtag.name} + {@code PostHashtag} 조인으로 치환.
 */
public interface HashtagRepositoryCustom {

    /**
     * TODO Day 16 Step 3: 태그가 붙은 게시물만 프로필 그리드와 동일 스펙으로 Slice 조회 (정렬·썸네일·댓글 수·likeCount 포함).
     */
    Slice<ProfilePostResponse> findProfilePostSliceByHashtagName(String normalizedHashtagName, Pageable pageable);

}
