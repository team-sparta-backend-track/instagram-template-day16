package com.example.instagramclone.domain.hashtag.application;

import com.example.instagramclone.core.common.dto.SliceResponse;
import com.example.instagramclone.core.exception.HashtagErrorCode;
import com.example.instagramclone.core.exception.HashtagException;
import com.example.instagramclone.domain.hashtag.api.HashtagMetaResponse;
import com.example.instagramclone.domain.hashtag.domain.Hashtag;
import com.example.instagramclone.domain.hashtag.domain.HashtagRepository;
import com.example.instagramclone.domain.hashtag.domain.PostHashtag;
import com.example.instagramclone.domain.hashtag.domain.PostHashtagRepository;
import com.example.instagramclone.domain.hashtag.infrastructure.HashtagMapper;
import com.example.instagramclone.domain.hashtag.infrastructure.HashtagRepositoryCustom;
import com.example.instagramclone.domain.hashtag.support.HashtagParser;
import com.example.instagramclone.core.exception.PostErrorCode;
import com.example.instagramclone.core.exception.PostException;
import com.example.instagramclone.domain.post.api.ProfilePostResponse;
import com.example.instagramclone.domain.post.domain.Post;
import com.example.instagramclone.domain.post.domain.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 해시태그 파싱·영속화·메타·태그 피드 (Day 16 라이브 코딩 대상).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HashtagService {

    private final HashtagRepository hashtagRepository;
    private final PostHashtagRepository postHashtagRepository;
    private final PostRepository postRepository;
    private final HashtagMapper hashtagMapper;

    /**
     * 캡션을 파싱한 뒤 기존 {@link PostHashtag} 를 비우고, 현재 본문 기준으로 다시 맞춥니다 (작성·수정 공통).
     */
    @Transactional
    public void syncHashtagsForPost(Long postId, String caption) {

    }

    /**
     * 여러 게시물에 붙은 해시태그 이름을 배치 조회합니다. 피드·상세 DTO 조립용 (엔티티 컬렉션에 의존하지 않음).
     */
    public Map<Long, List<String>> findHashtagNamesByPostIds(List<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return Map.of();
        }

        return null;
    }



    /**
     * TODO Day 16 Step 3: {@link HashtagRepository#findProfilePostSliceByHashtagName(String, Pageable)} 위임 후
     * {@link SliceResponse#of(boolean, List)} 로 감싸기 (프로필 그리드와 동일 계약).
     */
    public SliceResponse<ProfilePostResponse> getPostsByHashtag(String name, Pageable pageable) {
        return null;
    }


}
