package com.ftn.socialnetwork.service.implementation;

import com.ftn.socialnetwork.model.*;
import com.ftn.socialnetwork.model.dto.CommentDTO;
import com.ftn.socialnetwork.repository.CommentRepository;
import com.ftn.socialnetwork.repository.PostRepository;
import com.ftn.socialnetwork.security.jwt.JwtTokenUtil;
import com.ftn.socialnetwork.service.ICommentService;
import com.ftn.socialnetwork.util.exception.EntityNotFoundException;
import com.ftn.socialnetwork.util.exception.UnauthorizedException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class CommentService implements ICommentService {

    private final UserService userService;
    private final PostRepository postRepository;
    private final JwtTokenUtil jwtTokenUtil;
    private final CommentRepository commentRepository;
    private final CommentLikeService commentLikeService;
    private final int commentsPerPage = 4;


    public CommentService(UserService userService, PostRepository postRepository, JwtTokenUtil jwtTokenUtil,
                          CommentRepository commentRepository, CommentLikeService commentLikeService) {
        this.userService = userService;
        this.postRepository = postRepository;
        this.jwtTokenUtil = jwtTokenUtil;
        this.commentRepository = commentRepository;
        this.commentLikeService = commentLikeService;
    }


    @Override
    public Comment findOne(Long id) {
        Optional<Comment> comment = commentRepository.findById(id);
        if(comment.isEmpty()) {
            throw new NoSuchElementException("Comment with id = " + id + " not found!");
        }
        return comment.get();
    }

    public List<CommentWithData> getCommentsWithData(String token, List<Comment> comments){
        Long userId = jwtTokenUtil.getUserId(token);

        List<CommentWithData> commentsWithData = new ArrayList<>();
        for (Comment comment: comments){
            CommentWithData commentWithData = new CommentWithData();
            commentWithData.setComment(comment);
            Page<User> commentLikesPage = commentLikeService.findAllForComment(token, comment.getId(),0);
            commentWithData.setCommentLikes(commentLikesPage.getContent());
            commentWithData.setTotalLikes((int)commentLikesPage.getTotalElements());
            commentWithData.setLiked(commentLikeService.userLikedComment(userId, comment.getId()));
            commentsWithData.add(commentWithData);
        }

        return commentsWithData;
    }

    @Override
    public Page<CommentWithData> findAllForPost(String token, Long postId, int page) {
        Long userId = jwtTokenUtil.getUserId(token);

        Optional<Post> postOpt = postRepository.findById(postId);
        if (postOpt.isEmpty()){
            throw new EntityNotFoundException("The post whose comments you are trying to fetch does not exist.");
        }
        Post post = postOpt.get();
        // if user is not a friend of a post owner and post is not visible to PUBLIC, don't show comments
        if (!post.getUser().getId().equals(userId) && !userService.areFriends(userId, post.getUser().getId()) &&
            !post.getVisibility().equals("PUBLIC")){
            throw new UnauthorizedException("You are not authorized for this action.");
        }
        // if user is a friend of a post owner but post is only visible to the OWNER, don't show comments
        if (!post.getUser().getId().equals(userId) && userService.areFriends(userId, post.getUser().getId()) &&
                post.getVisibility().equals("ME")){
            throw new UnauthorizedException("You are not authorized for this action.");
        }

        List<CommentWithData> commentsWithData = getCommentsWithData(token, commentRepository.findAllByPostId(postId));
        // sorting
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        commentsWithData.sort(Comparator.comparing(c -> LocalDateTime.parse(c.getComment().getDatePosted(), formatter)));
        Collections.reverse(commentsWithData);

        Pageable pageable = PageRequest.of(page,this.commentsPerPage);
        final int start = (int)pageable.getOffset();
        final int end = Math.min((start + pageable.getPageSize()), commentsWithData.size());
        return new PageImpl<>(commentsWithData.subList(start, end), pageable, commentsWithData.size());
    }

    @Override
    public CommentWithData save(String token, CommentDTO commentDTO) {
        Long userId = jwtTokenUtil.getUserId(token);

        Optional<Post> postOpt = postRepository.findById(commentDTO.getPostId());
        if (postOpt.isEmpty()){
            throw new EntityNotFoundException("The post on which you are trying to comment does not exist.");
        }
        Post post = postOpt.get();
        // if user is not a friend of a post owner and post is not visible to PUBLIC, forbid commenting
        if (!post.getUser().getId().equals(userId) && !userService.areFriends(userId, post.getUser().getId()) &&
                !post.getVisibility().equals("PUBLIC")){
            throw new UnauthorizedException("You are not authorized for this action.");
        }
        // if user is a friend of a post owner but post is only visible to the OWNER, forbid commenting
        if (!post.getUser().getId().equals(userId) && userService.areFriends(userId, post.getUser().getId()) &&
                post.getVisibility().equals("ME")){
            throw new UnauthorizedException("You are not authorized for this action.");
        }

        User user = userService.findOne(userId);
        if (user == null){
            throw new UnauthorizedException("The user from session does not exist.");
        }

        Comment comment = new Comment();
        comment.setDatePosted(LocalDateTime.now().toString().substring(0,16).replace("T"," "));
        comment.setUser(user);
        comment.setPost(post);
        comment.setText(commentDTO.getText());
        comment.setEdited(false);

        Comment commentReturned = commentRepository.save(comment);

        CommentWithData commentWithData = new CommentWithData();
        commentWithData.setComment(commentReturned);
        commentWithData.setCommentLikes(commentLikeService.findAllForComment(token, commentReturned.getId(),0).getContent());
//        commentWithData.setLiked(commentLikeService.userLikedComment(userId, commentReturned.getId()));
        return commentWithData;
    }

    @Override
    public Comment update(String token, CommentDTO commentDTO) {
        Long userId = jwtTokenUtil.getUserId(token);

        Optional<Post> postOpt = postRepository.findById(commentDTO.getPostId());
        if (postOpt.isEmpty()){
            throw new EntityNotFoundException("The post on which you are trying to edit the comment does not exist.");
        }
        Post post = postOpt.get();
        // if user is not a friend of a post owner and post is not visible to PUBLIC, forbid commenting
        if (!post.getUser().getId().equals(userId) && !userService.areFriends(userId, post.getUser().getId()) &&
                !post.getVisibility().equals("PUBLIC")){
            throw new UnauthorizedException("You are not authorized for this action.");
        }
        // if user is a friend of a post owner but post is only visible to the OWNER, forbid commenting
        if (!post.getUser().getId().equals(userId) && userService.areFriends(userId, post.getUser().getId()) &&
                post.getVisibility().equals("ME")){
            throw new UnauthorizedException("You are not authorized for this action.");
        }

        Optional<Comment> commentOpt = commentRepository.findById(commentDTO.getId());
        if (commentOpt.isEmpty()){
            throw new EntityNotFoundException("The comment you are trying to edit does not exist.");
        }

        Comment comment = commentOpt.get();
        if (!comment.getUser().getId().equals(userId)){
            throw new UnauthorizedException("You are not authorized for this action.");
        }

        comment.setText(commentDTO.getText());
        comment.setEdited(true);
        return commentRepository.save(comment);
    }

    @Override
    public void delete(String token, Long id) {
        Long userId = jwtTokenUtil.getUserId(token);

        Optional<Comment> commentOpt = commentRepository.findById(id);
        if (commentOpt.isEmpty()){
            throw new EntityNotFoundException("The comment you are trying to delete does not exist.");
        }

        Comment comment = commentOpt.get();
        // validate if user owns the comment he is trying to delete
        if (!comment.getUser().getId().equals(userId)){
            throw new UnauthorizedException("You are not authorized for this action.");
        }
        commentRepository.deleteById(id);
    }
}
