package com.example.instagramclone.domain.hashtag.support;

import java.util.Collections;
import java.util.List;

/**
 * 캡션 문자열에서 해시태그 토큰을 추출·검증합니다.
 *
 * <p>TODO Day 16 Step 2: 정규식(예: {@code #([0-9A-Za-z가-힣_]+)}), 정규화(소문자·trim),
 * 글 내 중복 제거, 최대 개수·태그 길이 상한 검증({@link com.example.instagramclone.core.exception.HashtagErrorCode}).
 */
public final class HashtagParser {

    private HashtagParser() {
    }

    /**
     * TODO Day 16 Step 2: 본문에서 해시태그를 파싱해 저장용 정규화 이름 목록으로 반환합니다 (순서 유지 또는 정렬은 팀 규칙).
     */
    public static List<String> extractNormalizedUniqueTags(String caption) {
        return Collections.emptyList();
    }

    /**
     * TODO Day 16 Step 2: URL/본문에 붙은 {@code #} 제거·소문자 등 저장 단위 키로 통일.
     */
    public static String normalizeHashtagName(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return "";
        }
        String t = rawToken.strip();
        if (t.startsWith("#")) {
            t = t.substring(1);
        }
        return t.toLowerCase();
    }
}
