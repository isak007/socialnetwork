package com.ftn.socialnetwork.service;

import com.ftn.socialnetwork.model.CommentLike;

import java.util.List;

public interface ICommentLikeService {

    CommentLike findOne(Long id);

    List<CommentLike> findAll();

    CommentLike save(CommentLike commentLike);

    CommentLike update(CommentLike commentLike);

    void delete(Long id);
}
