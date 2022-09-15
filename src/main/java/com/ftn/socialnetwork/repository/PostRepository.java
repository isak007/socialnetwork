package com.ftn.socialnetwork.repository;

import com.ftn.socialnetwork.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
}
