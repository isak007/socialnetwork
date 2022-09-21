package com.ftn.socialnetwork.controller;

import com.ftn.socialnetwork.model.dto.PostLikeDTO;
import com.ftn.socialnetwork.service.IPostLikeService;
import com.ftn.socialnetwork.util.mapper.PostLikeMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping(value = "post-likes")
public class PostLikeController {

    private final IPostLikeService postLikeService;
    private final PostLikeMapper postLikeMapper;

    public PostLikeController(IPostLikeService postLikeService, PostLikeMapper postLikeMapper) {
        this.postLikeService = postLikeService;
        this.postLikeMapper = postLikeMapper;
    }


    @GetMapping(value = "post/{postId}")
    public ResponseEntity<List<PostLikeDTO>> findAllForPost(HttpServletRequest request, @PathVariable Long postId){
        String header = request.getHeader("Authorization");
        String token = header.substring(7);

        return new ResponseEntity<List<PostLikeDTO>>(
                postLikeService.findAllForPost(token, postId).stream().map(postLikeMapper::toDto).collect(Collectors.toList()),
                HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<PostLikeDTO> createPostLike(HttpServletRequest request, @RequestBody PostLikeDTO postLikeDTO) {
        String header = request.getHeader("Authorization");
        String token = header.substring(7);

        return new ResponseEntity<PostLikeDTO>(postLikeMapper.toDto(postLikeService.save(token,postLikeDTO)), HttpStatus.OK);
    }


    @DeleteMapping(value = "{id}")
    public ResponseEntity<Void> deletePostLike(HttpServletRequest request, @PathVariable(value = "id") Long id){
        String header = request.getHeader("Authorization");
        String token = header.substring(7);

        postLikeService.delete(token, id);

        return new ResponseEntity<>(HttpStatus.OK);
    }
}
