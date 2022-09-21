package com.ftn.socialnetwork.repository;

import com.ftn.socialnetwork.model.CommentLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {

    List<CommentLike> findByCommentId(Long commentId);

    Optional<CommentLike> findByUserIdAndCommentId(Long userId, Long commentId);

}
