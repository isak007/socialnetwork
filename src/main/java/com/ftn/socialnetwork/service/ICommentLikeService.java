package com.ftn.socialnetwork.service;

import com.ftn.socialnetwork.model.CommentLike;
import com.ftn.socialnetwork.model.dto.CommentLikeDTO;

import java.util.List;

public interface ICommentLikeService {

    CommentLike findOne(Long id);

    List<CommentLike> findAllForComment(String token, Long commentId);

    CommentLike save(String token, CommentLikeDTO commentLikeDTO);

    void delete(String token, CommentLikeDTO commentLikeDTO);
}
