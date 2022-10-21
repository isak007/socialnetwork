package com.ftn.socialnetwork.controller;

import com.ftn.socialnetwork.model.User;
import com.ftn.socialnetwork.model.dto.LikesDTO;
import com.ftn.socialnetwork.model.dto.PostLikeDTO;
import com.ftn.socialnetwork.model.dto.UserDTO;
import com.ftn.socialnetwork.service.IPostLikeService;
import com.ftn.socialnetwork.util.mapper.PostLikeMapper;
import com.ftn.socialnetwork.util.mapper.UserMapper;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.websocket.server.PathParam;
import java.util.stream.Collectors;


@RestController
@RequestMapping(value = "post-likes")
@CrossOrigin("http://localhost:8081/")
public class PostLikeController {

    private final IPostLikeService postLikeService;
    private final PostLikeMapper postLikeMapper;
    private final UserMapper userMapper;

    public PostLikeController(IPostLikeService postLikeService, PostLikeMapper postLikeMapper, UserMapper userMapper) {
        this.postLikeService = postLikeService;
        this.postLikeMapper = postLikeMapper;
        this.userMapper = userMapper;
    }


    @GetMapping(value = "post/{postId}")
    public ResponseEntity<LikesDTO> findAllForPost(HttpServletRequest request,
                                                        @PathParam(value = "page") Integer page,
                                                        @PathVariable Long postId){
        String header = request.getHeader("Authorization");
        String token = header.substring(7);
        // for testing purposes
//        try {
//            Thread.sleep(10 * 1000);
//        } catch (InterruptedException ie) {
//            Thread.currentThread().interrupt();
//        }

        Page<User> postLikePage = postLikeService.findAllForPost(token, postId, page);
        LikesDTO likesDTO = new LikesDTO();
        likesDTO.setUsers(postLikePage.getContent().stream().map(userMapper::toDto).collect(Collectors.toList()));
        likesDTO.setTotalLikes((int)postLikePage.getTotalElements());


        return new ResponseEntity<LikesDTO>(likesDTO, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<UserDTO> createPostLike(HttpServletRequest request, @RequestBody PostLikeDTO postLikeDTO) {
        String header = request.getHeader("Authorization");
        String token = header.substring(7);

        return new ResponseEntity<UserDTO>(userMapper.toDto(postLikeService.save(token,postLikeDTO)), HttpStatus.OK);
    }


    @DeleteMapping
    public ResponseEntity<Void> deletePostLike(HttpServletRequest request, @RequestBody PostLikeDTO postLikeDTO){
        String header = request.getHeader("Authorization");
        String token = header.substring(7);

        postLikeService.delete(token, postLikeDTO);

        return new ResponseEntity<>(HttpStatus.OK);
    }
}
