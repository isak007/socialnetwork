package com.ftn.socialnetwork.repository;

import com.ftn.socialnetwork.model.CommentLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {
}
