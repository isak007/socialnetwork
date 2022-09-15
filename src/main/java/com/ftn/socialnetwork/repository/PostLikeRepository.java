package com.ftn.socialnetwork.repository;

import com.ftn.socialnetwork.model.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
}
