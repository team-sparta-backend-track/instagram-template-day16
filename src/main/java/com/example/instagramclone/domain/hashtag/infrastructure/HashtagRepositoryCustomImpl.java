package com.example.instagramclone.domain.hashtag.infrastructure;

import com.example.instagramclone.domain.hashtag.domain.Hashtag;
import com.example.instagramclone.domain.post.api.ProfilePostResponse;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

/**
 * TODO Day 16 Step 3~4·과제: {@link com.example.instagramclone.domain.post.domain.QPost},
 * {@link com.example.instagramclone.domain.hashtag.domain.QPostHashtag},
 * {@link com.example.instagramclone.domain.hashtag.domain.QHashtag} 조인·집계·페이징 구현.
 */
@Repository
@RequiredArgsConstructor
public class HashtagRepositoryCustomImpl implements HashtagRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public long countPostsByHashtagName(String normalizedHashtagName) {
        // TODO Day 16 Step 4: QueryDSL COUNT (태그 없으면 0)
        return 0L;
    }

    @Override
    public Slice<ProfilePostResponse> findProfilePostSliceByHashtagName(String normalizedHashtagName, Pageable pageable) {
        // TODO Day 16 Step 3: PostRepositoryCustomImpl.findAllByWriterId 와 동일 튜플/서브쿼리 패턴, WHERE 만 태그 조건으로 교체
        return new SliceImpl<>(Collections.emptyList(), pageable, false);
    }

    @Override
    public List<Hashtag> findTopHashtagsByPostCount(String prefix, int limit) {
        // TODO Day 16 과제: 그룹 카운트·정렬·prefix startsWith / contains
        return Collections.emptyList();
    }
}
