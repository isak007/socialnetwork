package com.ftn.socialnetwork.service.implementation;

import com.ftn.socialnetwork.model.CommentLike;
import com.ftn.socialnetwork.repository.CommentLikeRepository;
import com.ftn.socialnetwork.service.ICommentLikeService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class CommentLikeService implements ICommentLikeService {

    private final CommentLikeRepository commentLikeRepository;

    public CommentLikeService(CommentLikeRepository commentLikeRepository) {
        this.commentLikeRepository = commentLikeRepository;
    }

    @Override
    public CommentLike findOne(Long id) {
        Optional<CommentLike> commentLike = commentLikeRepository.findById(id);
        if(commentLike.isEmpty()) {
            throw new NoSuchElementException("Comment like with id = " + id + " not found!");
        }
        return commentLike.get();
    }

    @Override
    public List<CommentLike> findAll() {
        List<CommentLike> commentLikes = commentLikeRepository.findAll();
        return commentLikes;
    }

    @Override
    public CommentLike save(CommentLike commentLike) {
        return commentLikeRepository.save(commentLike);
    }

    @Override
    public CommentLike update(CommentLike commentLike) {
        return commentLikeRepository.save(commentLike);
    }

    @Override
    public void delete(Long id) {
        commentLikeRepository.deleteById(id);
    }
}
