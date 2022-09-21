package com.ftn.socialnetwork.service.implementation;

import com.ftn.socialnetwork.model.Post;
import com.ftn.socialnetwork.model.PostLike;
import com.ftn.socialnetwork.model.User;
import com.ftn.socialnetwork.model.dto.PostLikeDTO;
import com.ftn.socialnetwork.repository.PostLikeRepository;
import com.ftn.socialnetwork.repository.PostRepository;
import com.ftn.socialnetwork.security.jwt.JwtTokenUtil;
import com.ftn.socialnetwork.service.IPostLikeService;
import com.ftn.socialnetwork.util.exception.EntityExistsException;
import com.ftn.socialnetwork.util.exception.EntityNotFoundException;
import com.ftn.socialnetwork.util.exception.UnauthorizedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class PostLikeService implements IPostLikeService {

    private final UserService userService;
    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final JwtTokenUtil jwtTokenUtil;

    public PostLikeService(UserService userService, PostRepository postRepository,
                           PostLikeRepository postLikeRepository, JwtTokenUtil jwtTokenUtil) {
        this.userService = userService;
        this.postRepository = postRepository;
        this.postLikeRepository = postLikeRepository;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @Override
    public PostLike findOne(Long id) {
        Optional<PostLike> postLike = postLikeRepository.findById(id);
        if(postLike.isEmpty()) {
            throw new NoSuchElementException("Post like with id = " + id + " not found!");
        }
        return postLike.get();
    }

    public boolean userLikedPost(Long userId, Long postId){
        Optional<PostLike> postLikeOpt = postLikeRepository.findByUserIdAndPostId(userId, postId);
        return postLikeOpt.isPresent();
    }

    @Override
    public List<PostLike> findAllForPost(String token, Long postId) {
        Long userId = Long.valueOf(jwtTokenUtil.getUserId(token));

        Optional<Post> postOpt = postRepository.findById(postId);
        if (postOpt.isEmpty()){
            throw new EntityNotFoundException("The post whose likes you are trying to fetch does not exist.");
        }
        Post post = postOpt.get();
        // if user is not a friend of a post owner and post is not visible to PUBLIC, don't show likes
        if (!post.getUser().getId().equals(userId) && !userService.areFriends(userId, post.getUser().getId()) &&
                !post.getVisibility().equals("PUBLIC")){
            throw new UnauthorizedException("You are not authorized for this action.");
        }
        // if user is a friend of a post owner but post is only visible to the OWNER, don't show likes
        if (!post.getUser().getId().equals(userId) && userService.areFriends(userId, post.getUser().getId()) &&
                post.getVisibility().equals("ME")){
            throw new UnauthorizedException("You are not authorized for this action.");
        }

        return postLikeRepository.findByPostId(postId);
    }

    @Override
    public PostLike save(String token, PostLikeDTO postLikeDTO) {
        Long userId = Long.valueOf(jwtTokenUtil.getUserId(token));

        if (postLikeRepository.findByUserIdAndPostId(userId,postLikeDTO.getPostId()).isPresent()){
            throw new EntityExistsException("This entity has already been created.");
        }

        Optional<Post> postOpt = postRepository.findById(postLikeDTO.getPostId());
        if (postOpt.isEmpty()){
            throw new EntityNotFoundException("The post whose likes you are trying to fetch does not exist.");
        }
        Post post = postOpt.get();
        // if user is not a friend of a post owner and post is not visible to PUBLIC, forbid liking
        if (!post.getUser().getId().equals(userId) && !userService.areFriends(userId, post.getUser().getId()) &&
                !post.getVisibility().equals("PUBLIC")){
            throw new UnauthorizedException("You are not authorized for this action.");
        }
        // if user is a friend of a post owner but post is only visible to the OWNER, forbid liking
        if (!post.getUser().getId().equals(userId) && userService.areFriends(userId, post.getUser().getId()) &&
                post.getVisibility().equals("ME")){
            throw new UnauthorizedException("You are not authorized for this action.");
        }

        User user = userService.findOne(userId);
        if (user == null){
            throw new EntityNotFoundException("The user from session does not exist.");
        }

        PostLike postLike = new PostLike();
        postLike.setPost(post);
        postLike.setUser(user);

        return postLikeRepository.save(postLike);
    }

    @Override
    public void delete(String token, Long id) {
        Long userId = Long.valueOf(jwtTokenUtil.getUserId(token));

        Optional<PostLike> postLikeOpt = postLikeRepository.findById(id);
        if (postLikeOpt.isEmpty()){
            throw new EntityNotFoundException("The like you are trying to delete does not exist.");
        }
        PostLike postLike = postLikeOpt.get();
        // if user wasn't the one who liked the post, forbid disliking/deleting
        if (!postLike.getUser().getId().equals(userId)){
            throw new UnauthorizedException("You are not authorized for this action.");
        }

        Optional<Post> postOpt = postRepository.findById(postLike.getPost().getId());
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

        postLikeRepository.deleteById(id);
    }
}
