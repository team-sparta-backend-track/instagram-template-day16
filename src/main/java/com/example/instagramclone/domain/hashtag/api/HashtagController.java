package com.example.instagramclone.domain.hashtag.api;

import com.example.instagramclone.core.common.dto.ApiResponse;
import com.example.instagramclone.core.common.dto.SliceResponse;
import com.example.instagramclone.core.util.PageableUtil;
import com.example.instagramclone.domain.hashtag.application.HashtagService;
import com.example.instagramclone.domain.post.api.ProfilePostResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * TODO Day 16: 팀 컨벤션에 따라 경로를 {@code /api/posts?hashtag=} 형태로 바꿀 수 있음.
 */
@RestController
@RequiredArgsConstructor
public class HashtagController {

    private final HashtagService hashtagService;

    /**
     * TODO Day 16 Step 3: 태그 피드 — {@link SliceResponse}{@code <}{@link ProfilePostResponse}{@code >} 재사용.
     */
    @GetMapping("/api/hashtags/{name}/posts")
    public ResponseEntity<ApiResponse<SliceResponse<ProfilePostResponse>>> getPostsByHashtag(
            @PathVariable String name,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "12") int size) {

        Pageable pageable = PageableUtil.createSafePageableDesc(page, size, "id");
        SliceResponse<ProfilePostResponse> body = hashtagService.getPostsByHashtag(name, pageable);
        return ResponseEntity.ok(ApiResponse.success(body));
    }

}
