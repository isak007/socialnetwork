package com.ftn.socialnetwork.service.implementation;

import com.ftn.socialnetwork.model.Comment;
import com.ftn.socialnetwork.repository.CommentRepository;
import com.ftn.socialnetwork.service.ICommentService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class CommentService implements ICommentService {

    private final CommentRepository commentRepository;

    public CommentService(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }


    @Override
    public Comment findOne(Long id) {
        Optional<Comment> comment = commentRepository.findById(id);
        if(comment.isEmpty()) {
            throw new NoSuchElementException("Comment with id = " + id + " not found!");
        }
        return comment.get();
    }

    @Override
    public List<Comment> findAll() {
        List<Comment> comments = commentRepository.findAll();
        return comments;
    }

    @Override
    public Comment save(Comment comment) {
        return commentRepository.save(comment);
    }

    @Override
    public Comment update(Comment comment) {
        return commentRepository.save(comment);
    }

    @Override
    public void delete(Long id) {
        commentRepository.deleteById(id);
    }
}
