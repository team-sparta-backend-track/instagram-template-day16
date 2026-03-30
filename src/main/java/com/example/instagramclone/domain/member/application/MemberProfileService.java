package com.example.instagramclone.domain.member.application;
import com.example.instagramclone.domain.member.api.MemberProfileResponse;
import com.example.instagramclone.domain.member.domain.Member;
import com.example.instagramclone.domain.member.domain.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 프로필 조회 전용 서비스.
 *
 * 프로필 헤더는 MemberRepository의 QueryDSL 커스텀 쿼리로
 * (팔로워/팔로잉/게시물 수 + isFollowing + isCurrentUser)까지 한 번에 내려준다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberProfileService {

    private final MemberService memberService;
    private final MemberRepository memberRepository;

    /**
     * 특정 회원의 프로필 1건 조회.
     *
     * - targetMember: 실제 존재 여부를 검증해야 하므로 findByUsername()
     * - isFollowing/카운트/정합성은 getProfileHeader()에서 QueryDSL로 계산한다.
     */
    public MemberProfileResponse getProfileByUsername(Long loginMemberId, String username) {
        // 1. 프로필 주인(타겟)의 정보를 DB에서 가져옵니다. (진짜 존재하는지 검증 필요)
        Member targetMember = memberService.findByUsername(username);

        // 2~5. 프로필 헤더는 MemberRepositoryQueryDSL로 한 번에 계산해 내려줍니다.
        return memberRepository.getProfileHeader(targetMember.getId(), loginMemberId);
    }
}
