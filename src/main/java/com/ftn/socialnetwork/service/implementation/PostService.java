package com.ftn.socialnetwork.service.implementation;

import com.ftn.socialnetwork.model.FriendRequest;
import com.ftn.socialnetwork.model.Post;
import com.ftn.socialnetwork.model.User;
import com.ftn.socialnetwork.model.dto.PostDTO;
import com.ftn.socialnetwork.repository.FriendRequestRepository;
import com.ftn.socialnetwork.repository.PostRepository;
import com.ftn.socialnetwork.security.jwt.JwtTokenUtil;
import com.ftn.socialnetwork.service.IPostService;
import com.ftn.socialnetwork.util.exception.EntityNotFoundException;
import com.ftn.socialnetwork.util.exception.UnauthorizedException;
import com.ftn.socialnetwork.util.mapper.UserMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static java.lang.String.format;

@Service
public class PostService implements IPostService {

    private final PostRepository postRepository;
    private final FriendRequestRepository friendRequestRepository;
    private final JwtTokenUtil jwtTokenUtil;
    private final UserMapper userMapper;
    private final UserService userService;

    public PostService(PostRepository postRepository, FriendRequestRepository friendRequestRepository, JwtTokenUtil jwtTokenUtil, UserMapper userMapper, UserService userService) {
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
    public List<Post> findAllForUser(String token, Long userId) {

        // if user is on his own profile
        if (Long.valueOf(jwtTokenUtil.getUserId(token)).equals(userId)) {
            // creating posts list with personal posts
            return new ArrayList<>(postRepository.findByUserId(userId));
        }

        // else
        else {
            List<Post> posts = new ArrayList<>();
            Long sessionUserId = Long.valueOf(jwtTokenUtil.getUserId(token));

            // adding posts visible to FRIENDS if friends
            if (userService.areFriends(sessionUserId, userId)){
                posts.addAll(postRepository.findByUserIdAndVisibility(userId,"FRIENDS"));
            }

            // adding posts visible to EVERYONE
            posts.addAll(postRepository.findByUserIdAndVisibility(userId, "PUBLIC"));

            return posts;
        }

    }

    @Override
    public List<Post> findAllForMainPage(String token) {
        Long userId = Long.valueOf(jwtTokenUtil.getUserId(token));
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

        return posts;
    }

    @Override
    public Post save(String token, PostDTO postDTO) {
        // validate if user is creating post for himself
        Long userId = Long.valueOf(jwtTokenUtil.getUserId(token));

        User user = userService.findOne(userId);
        if (user == null){
            throw new EntityNotFoundException(format("User with id '%s' from session doesn't exist.",userId));
        }

        Post post = new Post();
        post.setPicture(postDTO.getPicture());
        post.setText(postDTO.getText());
        post.setDatePosted(LocalDateTime.now().toString());
        post.setVisibility(postDTO.getVisibility());
        post.setUser(user);
        post.setEdited(false);

        return postRepository.save(post);
    }

    @Override
    public Post update(String token, PostDTO postDTO) {
        Optional<Post> postOpt = postRepository.findById(postDTO.getId());
        if (postOpt.isEmpty()){
            throw new EntityNotFoundException("The post you are trying to edit does not exist.");
        }

        Post post = postOpt.get();
        // validate if user is editing his post
        if (!Long.valueOf(jwtTokenUtil.getUserId(token)).equals(post.getUser().getId())){
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
        if (!postOpt.get().getUser().getId().equals(Long.valueOf(jwtTokenUtil.getUserId(token)))){
            throw new UnauthorizedException("You are not authorized for this action.");
        }

        postRepository.deleteById(id);
    }
}
