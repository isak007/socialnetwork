package com.ftn.socialnetwork.service;

import com.ftn.socialnetwork.model.Comment;
import com.ftn.socialnetwork.model.dto.CommentDTO;

import java.util.List;

public interface ICommentService {

    Comment findOne(Long id);

    List<Comment> findAllForPost(String token, Long postId);

    Comment save(String token, CommentDTO commentDTO);

    Comment update(String token, CommentDTO commentDTO);

    void delete(String token, Long id);
}
