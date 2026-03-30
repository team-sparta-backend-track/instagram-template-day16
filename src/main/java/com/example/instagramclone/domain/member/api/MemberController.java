package com.example.instagramclone.domain.member.api;

import com.example.instagramclone.core.common.dto.ApiResponse;
import com.example.instagramclone.domain.member.application.MemberProfileService;
import com.example.instagramclone.infrastructure.security.annotation.LoginUser;
import com.example.instagramclone.infrastructure.security.dto.LoginUserInfoDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberProfileService memberProfileService;

    /**
     * username 기반 프로필 조회.
     *
     * 프론트의 /:username 라우트와 바로 연결하기 위한 엔드포인트.
     */
    @GetMapping("/api/profiles/{username}")
    public ResponseEntity<ApiResponse<MemberProfileResponse>> getProfileByUsername(
            @PathVariable String username,
            @LoginUser LoginUserInfoDto loginUser) {
        MemberProfileResponse response = memberProfileService.getProfileByUsername(loginUser.id(), username);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
