package com.ftn.socialnetwork.service;

import com.ftn.socialnetwork.model.Comment;
import com.ftn.socialnetwork.model.CommentWithData;
import com.ftn.socialnetwork.model.dto.CommentDTO;
import org.springframework.data.domain.Page;

public interface ICommentService {

    Comment findOne(Long id);

    Page<CommentWithData> findAllForPost(String token, Long postId, int page);

    CommentWithData save(String token, CommentDTO commentDTO);

    Comment update(String token, CommentDTO commentDTO);

    void delete(String token, Long id);
}
