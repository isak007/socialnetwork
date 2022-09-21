package com.ftn.socialnetwork.repository;

import com.ftn.socialnetwork.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    List<Post> findByUserId(Long userId);
    List<Post> findByUserIdAndVisibility(Long userId, String visibility);
    List<Post> findByVisibility(String visibility);

}
