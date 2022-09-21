package com.ftn.socialnetwork.repository;

import com.ftn.socialnetwork.model.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    List<PostLike> findByPostId(Long postId);

    Optional<PostLike> findByUserIdAndPostId(Long userId, Long postId);

}
