package com.example.instagramclone.domain.post.domain;

import com.example.instagramclone.domain.post.infrastructure.PostRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long>, PostRepositoryCustom {
}
