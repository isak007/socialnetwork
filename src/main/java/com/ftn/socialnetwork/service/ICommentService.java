package com.ftn.socialnetwork.service;

import com.ftn.socialnetwork.model.Comment;

import java.util.List;

public interface ICommentService {

    Comment findOne(Long id);

    List<Comment> findAll();

    Comment save(Comment comment);

    Comment update(Comment comment);

    void delete(Long id);
}
