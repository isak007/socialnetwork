package com.ftn.socialnetwork.controller;

import com.ftn.socialnetwork.model.dto.PostDTO;
import com.ftn.socialnetwork.service.IPostService;
import com.ftn.socialnetwork.util.mapper.PostMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping(value = "posts")
public class PostController {

    private final IPostService postService;
    private final PostMapper postMapper;

    public PostController(IPostService postService, PostMapper postMapper) {
        this.postService = postService;
        this.postMapper = postMapper;
    }


    @GetMapping
    public ResponseEntity<List<PostDTO>> findAllForMainPage(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        String token = header.substring(7);

        return new ResponseEntity<List<PostDTO>>(
                postService.findAllForMainPage(token).stream().map(postMapper::toDto).collect(Collectors.toList()),
                HttpStatus.OK);
    }

    @GetMapping(value = "user/{userId}")
    public ResponseEntity<List<PostDTO>> findAllForUser(HttpServletRequest request, @PathVariable Long userId) {
        String header = request.getHeader("Authorization");
        String token = header.substring(7);

        return new ResponseEntity<List<PostDTO>>(
                postService.findAllForUser(token, userId).stream().map(postMapper::toDto).collect(Collectors.toList()),
                HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<PostDTO> createPost(HttpServletRequest request, @RequestBody PostDTO postDTO) {
        String header = request.getHeader("Authorization");
        String token = header.substring(7);

        return new ResponseEntity<PostDTO>(postMapper.toDto(postService.save(token,postDTO)), HttpStatus.OK);
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
