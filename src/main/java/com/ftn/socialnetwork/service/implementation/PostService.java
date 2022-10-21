package com.ftn.socialnetwork.service.implementation;

import com.ftn.socialnetwork.model.*;
import com.ftn.socialnetwork.model.dto.PostDTO;
import com.ftn.socialnetwork.repository.FriendRequestRepository;
import com.ftn.socialnetwork.repository.PostRepository;
import com.ftn.socialnetwork.security.jwt.JwtTokenUtil;
import com.ftn.socialnetwork.service.IPostService;
import com.ftn.socialnetwork.util.exception.EntityNotFoundException;
import com.ftn.socialnetwork.util.exception.UnauthorizedException;
import com.ftn.socialnetwork.util.mapper.UserMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static java.lang.String.format;

@Service
public class PostService implements IPostService {

    private final PostLikeService postLikeService;
    private final CommentService commentService;
    private final PostRepository postRepository;
    private final FriendRequestRepository friendRequestRepository;
    private final JwtTokenUtil jwtTokenUtil;
    private final UserMapper userMapper;
    private final UserService userService;
    private final String PROFILE_TYPE = "profile";
    private final String POST_TYPE = "post";
    private final int postsPerPage = 3;


    public PostService(PostLikeService postLikeService, CommentService commentService, PostRepository postRepository,
                       FriendRequestRepository friendRequestRepository, JwtTokenUtil jwtTokenUtil,
                       UserMapper userMapper, UserService userService) {
        this.postLikeService = postLikeService;
        this.commentService = commentService;
        this.postRepository = postRepository;
        this.friendRequestRepository = friendRequestRepository;
        this.jwtTokenUtil = jwtTokenUtil;
        this.userMapper = userMapper;
        this.userService = userService;
    }


    @Override
    public Post findOne(Long id) {
        Optional<Post> post = postRepository.findById(id);
        if(post.isEmpty()) {
            throw new NoSuchElementException("Post with id = " + id + " not found!");
        }
        return post.get();
    }

    @Override
    public Page<PostWithData> findAllForUser(String token, Long userId, int page) {

        // if user is on his own profile
        if (jwtTokenUtil.getUserId(token).equals(userId)) {
            // creating posts list with personal posts
            List<PostWithData> postsWithData = getPostsWithData(token, postRepository.findByUserId(userId));
            // sorting
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            postsWithData.sort(Comparator.comparing(p -> LocalDateTime.parse(p.getPost().getDatePosted(), formatter)));
            Collections.reverse(postsWithData);

            Pageable pageable = PageRequest.of(page,this.postsPerPage);
            final int start = (int)pageable.getOffset();
            final int end = Math.min((start + pageable.getPageSize()), postsWithData.size());
            return new PageImpl<>(postsWithData.subList(start, end), pageable, postsWithData.size());


//            LocalDateTime from = LocalDateTime.now();
//            LocalDateTime to = LocalDateTime.now().minusMinutes(5);
//
//            Duration duration = Duration.between(from, to);
//            System.out.println(duration.getSeconds());

//            return postsWithDataPage;
        }

        // else
        else {
            List<Post> posts = new ArrayList<>();
            Long sessionUserId = jwtTokenUtil.getUserId(token);

            // adding posts visible to FRIENDS if friends
            if (userService.areFriends(sessionUserId, userId)){
                posts.addAll(postRepository.findByUserIdAndVisibility(userId,"FRIENDS"));
            }
            // adding posts visible to EVERYONE
            posts.addAll(postRepository.findByUserIdAndVisibility(userId, "PUBLIC"));

            List<PostWithData> postsWithData = getPostsWithData(token, posts);
            // sorting
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            postsWithData.sort(Comparator.comparing(p -> LocalDateTime.parse(p.getPost().getDatePosted(), formatter)));
            Collections.reverse(postsWithData);

            Pageable pageable = PageRequest.of(page,this.postsPerPage);
            final int start = (int)pageable.getOffset();
            final int end = Math.min((start + pageable.getPageSize()), postsWithData.size());
            return new PageImpl<>(postsWithData.subList(start, end), pageable, postsWithData.size());
        }

    }

    public List<PostWithData> getPostsWithData(String token, List<Post> posts){
        Long userId = jwtTokenUtil.getUserId(token);

        List<PostWithData> postsWithData = new ArrayList<>();
        for (Post post: posts){
            PostWithData postWithData = new PostWithData();
            postWithData.setPost(post);
            Page<User> postLikesPage = postLikeService.findAllForPost(token, post.getId(),0);
            postWithData.setPostLikes(postLikesPage.getContent());
            postWithData.setTotalLikes((int)postLikesPage.getTotalElements());
            postWithData.setLiked(postLikeService.userLikedPost(userId, post.getId()));
            postsWithData.add(postWithData);
        }

        return postsWithData;
    }

    @Override
    public Page<PostWithData> findAllForMainPage(String token, int page) {
        Long userId = jwtTokenUtil.getUserId(token);
        // finding all accepted friend request for user
        List<FriendRequest> friendRequests = friendRequestRepository.
                findBySenderIdAndRequestStatus(userId, "ACCEPTED");
        friendRequests.addAll(friendRequestRepository.findByReceiverIdAndRequestStatus(userId,"ACCEPTED"));

        // creating posts list with personal posts
        List<Post> posts = new ArrayList<>(postRepository.findByUserId(userId));

        // adding friend's posts
        for (FriendRequest friendRequest : friendRequests){
            if (friendRequest.getSender().getId().equals(userId)) {
                posts.addAll(postRepository.findByUserIdAndVisibility(friendRequest.getReceiver().getId(),"FRIENDS"));
            }
            else {
                posts.addAll(postRepository.findByUserIdAndVisibility(friendRequest.getSender().getId(),"FRIENDS"));
            }
        }

        // adding posts visible to EVERYONE if not already added
        for (Post post : postRepository.findByVisibility("PUBLIC")){
            if (!posts.contains(post)){
                posts.add(post);
            }
        }

        List<PostWithData> postsWithData = getPostsWithData(token, posts);
        // sorting
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        postsWithData.sort(Comparator.comparing(p -> LocalDateTime.parse(p.getPost().getDatePosted(), formatter)));
        Collections.reverse(postsWithData);

        Pageable pageable = PageRequest.of(page,this.postsPerPage);
        final int start = (int)pageable.getOffset();
        final int end = Math.min((start + pageable.getPageSize()), postsWithData.size());
        return new PageImpl<>(postsWithData.subList(start, end), pageable, postsWithData.size());
    }

    @Override
    public PostWithData save(String token, PostDTO postDTO) {
        // validate if user is creating post for himself
        Long userId = jwtTokenUtil.getUserId(token);

        User user = userService.findOne(userId);
        if (user == null){
            throw new EntityNotFoundException(format("User with id '%s' from session doesn't exist.",userId));
        }

        Post post = new Post();
        post.setText(postDTO.getText());
        post.setDatePosted(LocalDateTime.now().toString().substring(0,16).replace("T"," "));
        post.setVisibility(postDTO.getVisibility());
        post.setUser(user);
        post.setEdited(false);

        if(postDTO.getPicture() != null && !postDTO.getPicture().equals("") && postDTO.getPictureBase64() != null) {
            post.setPicture(postDTO.getPicture());
            userService.uploadPicture(post.getUser().getId(), postDTO.getPicture(), postDTO.getPictureBase64(),this.POST_TYPE);
        }

        Post postReturned = postRepository.save(post);

        PostWithData postWithData = new PostWithData();
        postWithData.setPost(postReturned);
        postWithData.setPostLikes(postLikeService.findAllForPost(token, postReturned.getId(),0).getContent());
        //postWithData.setLiked(postLikeService.userLikedPost(userId, postReturned.getId()));
        return postWithData;
    }

    @Override
    public Post update(String token, PostDTO postDTO) {
        Optional<Post> postOpt = postRepository.findById(postDTO.getId());
        if (postOpt.isEmpty()){
            throw new EntityNotFoundException("The post you are trying to edit does not exist.");
        }

        Post post = postOpt.get();
        // validate if user is editing his post
        if (!jwtTokenUtil.getUserId(token).equals(post.getUser().getId())){
            throw new UnauthorizedException("You are not authorized for this action.");
        }

//        post.setPicture(postDTO.getPicture());
        post.setText(postDTO.getText());
        post.setVisibility(postDTO.getVisibility());
        post.setEdited(true);

        return postRepository.save(post);
    }

    @Override
    public void delete(String token, Long id) {
        Optional<Post> postOpt = postRepository.findById(id);
        if (postOpt.isEmpty()){
            throw new EntityNotFoundException("The post you are trying to delete does not exist.");
        }

        // validate if user owns the post he's trying to delete
        if (!postOpt.get().getUser().getId().equals(jwtTokenUtil.getUserId(token))){
            throw new UnauthorizedException("You are not authorized for this action.");
        }

        postRepository.deleteById(id);
    }
}
