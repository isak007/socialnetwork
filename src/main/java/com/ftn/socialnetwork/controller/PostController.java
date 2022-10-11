package com.ftn.socialnetwork.controller;

import com.ftn.socialnetwork.model.dto.PostDTO;
import com.ftn.socialnetwork.model.dto.PostWithDataDTO;
import com.ftn.socialnetwork.service.IPostService;
import com.ftn.socialnetwork.util.mapper.PostMapper;
import com.ftn.socialnetwork.util.mapper.PostWithDataMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping(value = "posts")
@CrossOrigin("http://localhost:8081/")
public class PostController {

    private final PostWithDataMapper postWithDataMapper;
    private final IPostService postService;
    private final PostMapper postMapper;

    public PostController(PostWithDataMapper postWithDataMapper, IPostService postService, PostMapper postMapper) {
        this.postWithDataMapper = postWithDataMapper;
        this.postService = postService;
        this.postMapper = postMapper;
    }


    @GetMapping
    public ResponseEntity<List<PostWithDataDTO>> findAllForMainPage(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        String token = header.substring(7);

        return new ResponseEntity<List<PostWithDataDTO>>(
                postService.findAllForMainPage(token).stream().map(postWithDataMapper::toDto).collect(Collectors.toList()),
                HttpStatus.OK);
    }

    @GetMapping(value = "user/{userId}")
    public ResponseEntity<List<PostWithDataDTO>> findAllForUser(HttpServletRequest request, @PathVariable Long userId) {
        String header = request.getHeader("Authorization");
        String token = header.substring(7);

        return new ResponseEntity<List<PostWithDataDTO>>(
                postService.findAllForUser(token, userId).stream().map(postWithDataMapper::toDto).collect(Collectors.toList()),
                HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<PostWithDataDTO> createPost(HttpServletRequest request, @RequestBody PostDTO postDTO) {
        String header = request.getHeader("Authorization");
        String token = header.substring(7);

        return new ResponseEntity<PostWithDataDTO>(postWithDataMapper.toDto(postService.save(token,postDTO)), HttpStatus.OK);
    }

    @PutMapping
    public ResponseEntity<PostDTO> editPost(HttpServletRequest request, @RequestBody PostDTO postDTO) {
        String header = request.getHeader("Authorization");
        String token = header.substring(7);

        return new ResponseEntity<PostDTO>(postMapper.toDto(postService.update(token,postDTO)), HttpStatus.OK);
    }

    @DeleteMapping(value = "{id}")
    public ResponseEntity<Void> deletePost(HttpServletRequest request, @PathVariable(value = "id") Long id){
        String header = request.getHeader("Authorization");
        String token = header.substring(7);

        postService.delete(token, id);

        return new ResponseEntity<>(HttpStatus.OK);
    }
}
