package com.example.instagramclone.domain.hashtag.domain;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface PostHashtagRepository extends JpaRepository<PostHashtag, Long> {

    /**
     * {@link PostHashtag#post} 연관의 id 기준 삭제 (Spring Data: {@code post} + {@code id} → {@code Post_Id}).
     */
    void deleteByPost_Id(Long postId);

    List<PostHashtag> findByPost_Id(Long postId);

    long countByHashtag_Id(Long hashtagId);

    /**
     * 피드 배치 조회용 — {@link Hashtag} 를 함께 로드해 N+1 완화.
     */
    @EntityGraph(attributePaths = {"hashtag"})
    List<PostHashtag> findByPost_IdIn(Collection<Long> postIds);
}
