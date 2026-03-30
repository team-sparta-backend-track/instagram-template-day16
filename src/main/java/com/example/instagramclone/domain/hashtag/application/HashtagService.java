package com.example.instagramclone.domain.hashtag.application;


import com.example.instagramclone.core.common.dto.SliceResponse;
import com.example.instagramclone.core.exception.HashtagErrorCode;
import com.example.instagramclone.core.exception.HashtagException;
import com.example.instagramclone.core.exception.PostErrorCode;
import com.example.instagramclone.core.exception.PostException;
import com.example.instagramclone.domain.hashtag.domain.Hashtag;
import com.example.instagramclone.domain.hashtag.domain.HashtagRepository;
import com.example.instagramclone.domain.hashtag.domain.PostHashtag;
import com.example.instagramclone.domain.hashtag.domain.PostHashtagRepository;
import com.example.instagramclone.domain.hashtag.infrastructure.HashtagMapper;
import com.example.instagramclone.domain.hashtag.support.HashtagParser;
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
 * 해시태그 파싱·영속화·메타·태그 피드
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HashtagService {

    /** 해시태그 마스터(이름 유니크) */
    private final HashtagRepository hashtagRepository;

    /** 게시물–해시태그 연결 행만 관리 (Post/Hashtag에 컬렉션 두지 않음) */
    private final PostHashtagRepository postHashtagRepository;

    private final PostRepository postRepository;
    private final HashtagMapper hashtagMapper;

    /**
     * 캡션을 파싱한 뒤 기존 {@link PostHashtag} 를 비우고, 현재 본문 기준으로 다시 맞춥니다 (작성·수정 공통).
     */
    @Transactional
    public void syncHashtagsForPost(Long postId, String caption) {
        // 영속 컨텍스트에 올라간 Post가 있어야 PostHashtag.create(post, ...) 시 연관이 안전하게 걸린다
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostException(PostErrorCode.POST_NOT_FOUND));

        // 파싱·검증은 Parser에만 둔다 (비즈니스 규칙: 정규화·개수·길이 상한)
        List<String> normalizedTags = HashtagParser.extractNormalizedUniqueTags(caption);

        // 수정 시에도 동일: 기존 연결을 전부 지우고 캡션 기준으로 다시 맞춤 (캡션과 DB 불일치 방지)
        postHashtagRepository.deleteByPost_Id(postId);

        for (String name : normalizedTags) {
            // findOrCreate: 이미 있는 태그는 재사용, 없으면 마스터 행만 새로 저장
            Hashtag hashtag = hashtagRepository.findByName(name)
                    .orElseGet(() -> hashtagRepository.save(Hashtag.create(name)));

            // 중간 엔티티로만 연결
            postHashtagRepository.save(PostHashtag.create(post, hashtag));
        }
    }

    /**
     * 여러 게시물에 붙은 해시태그 이름을 배치 조회합니다. 피드·상세 DTO 조립용 (엔티티 컬렉션에 의존하지 않음).
     */
    public Map<Long, List<String>> findHashtagNamesByPostIds(List<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return Map.of();
        }

        // IN 한 번으로 연결 행만 가져온 뒤 메모리에서 postId별로 묶는다 (N+1 회피)
        List<PostHashtag> links = postHashtagRepository.findByPost_IdIn(postIds);

        // 같은 게시물에 달린 PostHashtag 여러 개를 postId 기준으로 그룹핑
        Map<Long, List<PostHashtag>> byPost = links.stream()
                .collect(Collectors.groupingBy(ph -> ph.getPost().getId()));

        // 호출자가 넘긴 postIds 순서를 유지 (피드 행 순서와 맞추기 위함)
        Map<Long, List<String>> result = new LinkedHashMap<>();
        for (Long postId : postIds) {
            List<PostHashtag> row = byPost.getOrDefault(postId, List.of());
            // 연결 행 삽입 순서(= id 오름차순)대로 태그명 나열 — sync 시 저장 순서와 대체로 일치
            List<String> names = row.stream()
                    .sorted(Comparator.comparing(PostHashtag::getId))
                    .map(ph -> ph.getHashtag().getName())
                    .toList();
            result.put(postId, names);
        }
        return result;
    }

    /**
     * TODO Day 16 Step 3: {@link HashtagRepository#findProfilePostSliceByHashtagName(String, Pageable)} 위임 후
     * {@link SliceResponse#of(boolean, List)} 로 감싸기 (프로필 그리드와 동일 계약).
     */
    // HashtagService.java (getPostsByHashtag 메서드 완성)
    public SliceResponse<ProfilePostResponse> getPostsByHashtag(String name, Pageable pageable) {
        String normalized = HashtagParser.normalizeHashtagName(name);
        if (normalized.isBlank()) {
            throw new HashtagException(HashtagErrorCode.INVALID_HASHTAG_TOKEN);
        }
        if (!hashtagRepository.existsByName(normalized)) {
            throw new HashtagException(HashtagErrorCode.HASHTAG_NOT_FOUND);
        }

        Slice<ProfilePostResponse> slice = hashtagRepository.findProfilePostSliceByHashtagName(normalized, pageable);
        return SliceResponse.of(slice.hasNext(), slice.getContent());
    }
}