package com.ftn.socialnetwork.controller;

import com.ftn.socialnetwork.model.dto.CommentDTO;
import com.ftn.socialnetwork.service.ICommentService;
import com.ftn.socialnetwork.service.IPostService;
import com.ftn.socialnetwork.service.implementation.FriendRequestService;
import com.ftn.socialnetwork.util.mapper.CommentMapper;
import com.ftn.socialnetwork.util.mapper.FriendRequestMapper;
import com.ftn.socialnetwork.util.mapper.PostMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping(value = "comments")
public class CommentController {

    private final CommentMapper commentMapper;
    private final ICommentService commentService;
    private final FriendRequestService friendRequestService;
    private final IPostService postService;
    private final PostMapper postMapper;
    private final FriendRequestMapper friendRequestMapper;

    public CommentController(CommentMapper commentMapper, ICommentService commentService,
                             FriendRequestService friendRequestService, IPostService postService, PostMapper postMapper,
                             FriendRequestMapper friendRequestMapper) {
        this.commentMapper = commentMapper;
        this.commentService = commentService;
        this.friendRequestService = friendRequestService;
        this.postService = postService;
        this.postMapper = postMapper;
        this.friendRequestMapper = friendRequestMapper;
    }


    @GetMapping(value = "post/{id}")
    public ResponseEntity<List<CommentDTO>> findAllForPost(HttpServletRequest request, @PathVariable(value = "id") Long postId) {
        String header = request.getHeader("Authorization");
        String token = header.substring(7);

        return new ResponseEntity<List<CommentDTO>>(
                commentService.findAllForPost(token, postId).stream().map(commentMapper::toDto).collect(Collectors.toList()),
                HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<CommentDTO> createComment(HttpServletRequest request, @RequestBody CommentDTO commentDTO) {
        String header = request.getHeader("Authorization");
        String token = header.substring(7);

        return new ResponseEntity<CommentDTO>(commentMapper.toDto(commentService.save(token,commentDTO)), HttpStatus.OK);
    }

    @PutMapping
    public ResponseEntity<CommentDTO> editComment(HttpServletRequest request, @RequestBody CommentDTO commentDTO) {
        String header = request.getHeader("Authorization");
        String token = header.substring(7);

        return new ResponseEntity<CommentDTO>(commentMapper.toDto(commentService.update(token,commentDTO)), HttpStatus.OK);
    }

    @DeleteMapping(value = "{id}")
    public ResponseEntity<Void> deleteComment(HttpServletRequest request, @PathVariable(value = "id") Long id){
        String header = request.getHeader("Authorization");
        String token = header.substring(7);

        commentService.delete(token, id);

        return new ResponseEntity<>(HttpStatus.OK);
    }
}
