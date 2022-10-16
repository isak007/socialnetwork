package com.ftn.socialnetwork.service;

import com.ftn.socialnetwork.model.CommentLike;
import com.ftn.socialnetwork.model.User;
import com.ftn.socialnetwork.model.dto.CommentLikeDTO;
import org.springframework.data.domain.Page;

public interface ICommentLikeService {

    CommentLike findOne(Long id);

    Page<User> findAllForComment(String token, Long commentId, int page);

    User save(String token, CommentLikeDTO commentLikeDTO);

    void delete(String token, CommentLikeDTO commentLikeDTO);
}
