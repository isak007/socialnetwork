package com.ftn.socialnetwork.controller;

import com.ftn.socialnetwork.model.dto.CommentLikeDTO;
import com.ftn.socialnetwork.service.ICommentLikeService;
import com.ftn.socialnetwork.util.mapper.CommentLikeMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping(value = "comment-likes")
@CrossOrigin("http://localhost:8081/")
public class CommentLikeController {

    private final ICommentLikeService commentLikeService;
    private final CommentLikeMapper commentLikeMapper;

    public CommentLikeController(ICommentLikeService commentLikeService, CommentLikeMapper commentLikeMapper) {
        this.commentLikeService = commentLikeService;
        this.commentLikeMapper = commentLikeMapper;
    }


    @GetMapping(value = "comment/{commentId}")
    public ResponseEntity<List<CommentLikeDTO>> findAllForComment(HttpServletRequest request, @PathVariable Long commentId){
        String header = request.getHeader("Authorization");
        String token = header.substring(7);

        return new ResponseEntity<List<CommentLikeDTO>>(
                commentLikeService.findAllForComment(token, commentId).stream().map(commentLikeMapper::toDto).collect(Collectors.toList()),
                HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<CommentLikeDTO> createCommentLike(HttpServletRequest request, @RequestBody CommentLikeDTO commentLikeDTO) {
        String header = request.getHeader("Authorization");
        String token = header.substring(7);

        return new ResponseEntity<CommentLikeDTO>(commentLikeMapper.toDto(commentLikeService.save(token,commentLikeDTO)), HttpStatus.OK);
    }


    @DeleteMapping(value = "{id}")
    public ResponseEntity<Void> deleteCommentLike(HttpServletRequest request, @PathVariable(value = "id") Long id){
        String header = request.getHeader("Authorization");
        String token = header.substring(7);

        commentLikeService.delete(token, id);

        return new ResponseEntity<>(HttpStatus.OK);
    }
}
