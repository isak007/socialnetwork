package com.ftn.socialnetwork.controller;

import com.ftn.socialnetwork.model.CommentWithData;
import com.ftn.socialnetwork.model.dto.CommentDTO;
import com.ftn.socialnetwork.model.dto.CommentWithDataDTO;
import com.ftn.socialnetwork.model.dto.CommentsDTO;
import com.ftn.socialnetwork.service.ICommentService;
import com.ftn.socialnetwork.util.mapper.CommentMapper;
import com.ftn.socialnetwork.util.mapper.CommentWithDataMapper;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.websocket.server.PathParam;
import java.util.stream.Collectors;


@RestController
@RequestMapping(value = "comments")
@CrossOrigin("http://localhost:8081/")
public class CommentController {

    private final CommentMapper commentMapper;
    private final CommentWithDataMapper commentWithDataMapper;
    private final ICommentService commentService;

    public CommentController(CommentMapper commentMapper, CommentWithDataMapper commentWithDataMapper, ICommentService commentService) {
        this.commentMapper = commentMapper;
        this.commentWithDataMapper = commentWithDataMapper;
        this.commentService = commentService;
    }

    @GetMapping(value = "post/{id}")
    public ResponseEntity<CommentsDTO> findAllForPost(HttpServletRequest request,
                                                                   @PathParam(value = "page") Integer page,
                                                                   @PathVariable(value = "id") Long postId) {
        String header = request.getHeader("Authorization");
        String token = header.substring(7);

        Page<CommentWithData> commentsWithDataPage = commentService.findAllForPost(token, postId, page);

        CommentsDTO commentsDTO = new CommentsDTO();
        commentsDTO.setCommentsWithDataDTO(commentsWithDataPage.getContent().stream().map(commentWithDataMapper::toDto).collect(Collectors.toList()));
        commentsDTO.setTotalComments((int)commentsWithDataPage.getTotalElements());

        return new ResponseEntity<CommentsDTO>(commentsDTO,HttpStatus.OK);
    }

    @GetMapping(value = "{id}")
    public ResponseEntity<CommentDTO> getComment(HttpServletRequest request, @PathVariable Long id) {
        String header = request.getHeader("Authorization");
        String token = header.substring(7);

        return new ResponseEntity<CommentDTO>(commentMapper.toDto(commentService.findOne(token,id)), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<CommentWithDataDTO> createComment(HttpServletRequest request, @RequestBody CommentDTO commentDTO) {
        String header = request.getHeader("Authorization");
        String token = header.substring(7);

        return new ResponseEntity<CommentWithDataDTO>(commentWithDataMapper.toDto(commentService.save(token,commentDTO)), HttpStatus.OK);
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
