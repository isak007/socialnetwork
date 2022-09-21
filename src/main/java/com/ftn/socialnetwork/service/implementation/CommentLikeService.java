package com.ftn.socialnetwork.service.implementation;

import com.ftn.socialnetwork.model.*;
import com.ftn.socialnetwork.model.dto.CommentLikeDTO;
import com.ftn.socialnetwork.repository.CommentLikeRepository;
import com.ftn.socialnetwork.repository.CommentRepository;
import com.ftn.socialnetwork.security.jwt.JwtTokenUtil;
import com.ftn.socialnetwork.service.ICommentLikeService;
import com.ftn.socialnetwork.util.exception.EntityExistsException;
import com.ftn.socialnetwork.util.exception.EntityNotFoundException;
import com.ftn.socialnetwork.util.exception.UnauthorizedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class CommentLikeService implements ICommentLikeService {

    private final CommentLikeRepository commentLikeRepository;
    private final JwtTokenUtil jwtTokenUtil;
    private final CommentRepository commentRepository;
    private final UserService userService;

    public CommentLikeService(CommentLikeRepository commentLikeRepository, JwtTokenUtil jwtTokenUtil, CommentRepository commentRepository, UserService userService) {
        this.commentLikeRepository = commentLikeRepository;
        this.jwtTokenUtil = jwtTokenUtil;
        this.commentRepository = commentRepository;
        this.userService = userService;
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
    public List<CommentLike> findAllForComment(String token, Long commentId) {
        Long userId = Long.valueOf(jwtTokenUtil.getUserId(token));

        Optional<Comment> commentOpt = commentRepository.findById(commentId);
        if (commentOpt.isEmpty()){
            throw new EntityNotFoundException("The comment whose likes you are trying to fetch does not exist.");
        }
        Post post = commentOpt.get().getPost();
        // if user is not a friend of a post owner and post is not visible to PUBLIC, don't show comment likes
        if (!post.getUser().getId().equals(userId) && !userService.areFriends(userId, post.getUser().getId()) &&
                !post.getVisibility().equals("PUBLIC")){
            throw new UnauthorizedException("You are not authorized for this action.");
        }
        // if user is a friend of a post owner but post is only visible to the OWNER, don't show comment likes
        if (!post.getUser().getId().equals(userId) && userService.areFriends(userId, post.getUser().getId()) &&
                post.getVisibility().equals("ME")){
            throw new UnauthorizedException("You are not authorized for this action.");
        }

        return commentLikeRepository.findByCommentId(commentId);
    }

    @Override
    public CommentLike save(String token, CommentLikeDTO commentLikeDTO) {
        Long userId = Long.valueOf(jwtTokenUtil.getUserId(token));

        if (commentLikeRepository.findByUserIdAndCommentId(userId,commentLikeDTO.getCommentId()).isPresent()){
            throw new EntityExistsException("This entity has already been created.");
        }

        Optional<Comment> commentOpt = commentRepository.findById(commentLikeDTO.getCommentId());
        if (commentOpt.isEmpty()){
            throw new EntityNotFoundException("The comment which you are trying to like does not exist.");
        }
        Post post = commentOpt.get().getPost();
        // if user is not a friend of a post owner and post is not visible to PUBLIC, forbid liking the comment
        if (!post.getUser().getId().equals(userId) && !userService.areFriends(userId, post.getUser().getId()) &&
                !post.getVisibility().equals("PUBLIC")){
            throw new UnauthorizedException("You are not authorized for this action.");
        }
        // if user is a friend of a post owner but post is only visible to the OWNER, forbid liking the comment
        if (!post.getUser().getId().equals(userId) && userService.areFriends(userId, post.getUser().getId()) &&
                post.getVisibility().equals("ME")){
            throw new UnauthorizedException("You are not authorized for this action.");
        }

        User user = userService.findOne(userId);
        if (user == null){
            throw new EntityNotFoundException("The user from session does not exist.");
        }

        CommentLike commentLike = new CommentLike();
        commentLike.setUser(user);
        commentLike.setComment(commentOpt.get());

        return commentLikeRepository.save(commentLike);
    }

    @Override
    public void delete(String token, Long id) {
        Long userId = Long.valueOf(jwtTokenUtil.getUserId(token));

        Optional<CommentLike> commentLikeOpt = commentLikeRepository.findById(id);
        if (commentLikeOpt.isEmpty()){
            throw new EntityNotFoundException("The like you are trying to delete does not exist.");
        }
        CommentLike commentLike = commentLikeOpt.get();
        // if user wasn't the one who liked the comment, forbid disliking/deleting
        if (!commentLike.getUser().getId().equals(userId)){
            throw new UnauthorizedException("You are not authorized for this action.");
        }

        Post post = commentLikeOpt.get().getComment().getPost();
        // if user is not a friend of a post owner and post is not visible to PUBLIC, forbid disliking/deleting
        if (!post.getUser().getId().equals(userId) && !userService.areFriends(userId, post.getUser().getId()) &&
                !post.getVisibility().equals("PUBLIC")){
            throw new UnauthorizedException("You are not authorized for this action.");
        }
        // if user is a friend of a post owner but post is only visible to the OWNER, forbid disliking/deleting
        if (!post.getUser().getId().equals(userId) && userService.areFriends(userId, post.getUser().getId()) &&
                post.getVisibility().equals("ME")){
            throw new UnauthorizedException("You are not authorized for this action.");
        }

        commentLikeRepository.deleteById(id);
    }
}
