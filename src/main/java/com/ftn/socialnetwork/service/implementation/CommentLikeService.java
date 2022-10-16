package com.ftn.socialnetwork.service.implementation;

import com.ftn.socialnetwork.model.*;
import com.ftn.socialnetwork.model.dto.CommentLikeDTO;
import com.ftn.socialnetwork.repository.CommentLikeRepository;
import com.ftn.socialnetwork.repository.CommentRepository;
import com.ftn.socialnetwork.repository.PostRepository;
import com.ftn.socialnetwork.security.jwt.JwtTokenUtil;
import com.ftn.socialnetwork.service.ICommentLikeService;
import com.ftn.socialnetwork.util.exception.EntityExistsException;
import com.ftn.socialnetwork.util.exception.EntityNotFoundException;
import com.ftn.socialnetwork.util.exception.UnauthorizedException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class CommentLikeService implements ICommentLikeService {

    private final CommentLikeRepository commentLikeRepository;
    private final JwtTokenUtil jwtTokenUtil;
    private final CommentRepository commentRepository;
    private final UserService userService;
    private final PostRepository postRepository;
    private final int usersPerPage = 15;


    public CommentLikeService(CommentLikeRepository commentLikeRepository, JwtTokenUtil jwtTokenUtil, CommentRepository commentRepository, UserService userService, PostRepository postRepository) {
        this.commentLikeRepository = commentLikeRepository;
        this.jwtTokenUtil = jwtTokenUtil;
        this.commentRepository = commentRepository;
        this.userService = userService;
        this.postRepository = postRepository;
    }

    @Override
    public CommentLike findOne(Long id) {
        Optional<CommentLike> commentLike = commentLikeRepository.findById(id);
        if(commentLike.isEmpty()) {
            throw new NoSuchElementException("Comment like with id = " + id + " not found!");
        }
        return commentLike.get();
    }

    public boolean userLikedComment(Long userId, Long commentId){
        Optional<CommentLike> commentLikeOpt = commentLikeRepository.findByUserIdAndCommentId(userId, commentId);
        return commentLikeOpt.isPresent();
    }

    @Override
    public Page<User> findAllForComment(String token, Long commentId, int page) {
        Long userId = jwtTokenUtil.getUserId(token);

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

        List<CommentLike> commentLikes = commentLikeRepository.findByCommentId(commentId);
        List<User> users = new ArrayList<>();
        for (CommentLike commentLike : commentLikes){
            users.add(commentLike.getUser());
        }

        Pageable pageable = PageRequest.of(page,this.usersPerPage);
        final int start = (int)pageable.getOffset();
        final int end = Math.min((start + pageable.getPageSize()), users.size());
        final Page<User> usersPage = new PageImpl<>(users.subList(start, end), pageable, users.size());

        return usersPage;
    }

    @Override
    public User save(String token, CommentLikeDTO commentLikeDTO) {
        Long userId = jwtTokenUtil.getUserId(token);

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
        commentLikeRepository.save(commentLike);

        return user;
    }

    @Override
    public void delete(String token, CommentLikeDTO commentLikeDTO) {
        Long userId = jwtTokenUtil.getUserId(token);

        // if user wasn't the one who liked the post, forbid disliking/deleting
        if (!commentLikeDTO.getUserId().equals(userId)){
            throw new UnauthorizedException("You are not authorized for this action.");
        }

        Optional<CommentLike> commentLikeOpt = commentLikeRepository.findByUserIdAndCommentId(commentLikeDTO.getUserId(),commentLikeDTO.getCommentId());
        if (commentLikeOpt.isEmpty()){
            throw new EntityNotFoundException("The like you are trying to delete does not exist.");
        }
        CommentLike commentLike = commentLikeOpt.get();

        if (commentLike.getComment() == null){
            throw new EntityNotFoundException("The comment whose like you are trying to delete does not exist.");
        }

        Optional<Post> postOpt = postRepository.findById(commentLike.getComment().getPost().getId());
        if (postOpt.isEmpty()){
            throw new EntityNotFoundException("The post whose like you are trying to delete does not exist.");
        }
        Post post = postOpt.get();
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

        commentLikeRepository.deleteById(commentLike.getId());
    }
}
