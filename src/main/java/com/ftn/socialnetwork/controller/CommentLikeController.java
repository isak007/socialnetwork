package com.ftn.socialnetwork.controller;

import com.ftn.socialnetwork.model.User;
import com.ftn.socialnetwork.model.dto.CommentLikeDTO;
import com.ftn.socialnetwork.model.dto.LikesDTO;
import com.ftn.socialnetwork.model.dto.UserDTO;
import com.ftn.socialnetwork.service.ICommentLikeService;
import com.ftn.socialnetwork.util.mapper.CommentLikeMapper;
import com.ftn.socialnetwork.util.mapper.UserMapper;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.websocket.server.PathParam;
import java.util.stream.Collectors;


@RestController
@RequestMapping(value = "comment-likes")
@CrossOrigin("http://localhost:8081/")
public class CommentLikeController {

    private final ICommentLikeService commentLikeService;
    private final CommentLikeMapper commentLikeMapper;
    private final UserMapper userMapper;

    public CommentLikeController(ICommentLikeService commentLikeService, CommentLikeMapper commentLikeMapper, UserMapper userMapper) {
        this.commentLikeService = commentLikeService;
        this.commentLikeMapper = commentLikeMapper;
        this.userMapper = userMapper;
    }


    @GetMapping(value = "comment/{commentId}")
    public ResponseEntity<LikesDTO> findAllForComment(HttpServletRequest request,
                                                                  @PathParam(value = "page") Integer page,
                                                                  @PathVariable Long commentId){
        String header = request.getHeader("Authorization");
        String token = header.substring(7);

        Page<User> commentLikePage = commentLikeService.findAllForComment(token, commentId, page);
        LikesDTO likesDTO = new LikesDTO();
        likesDTO.setUsers(commentLikePage.getContent().stream().map(userMapper::toDto).collect(Collectors.toList()));
        likesDTO.setTotalLikes((int)commentLikePage.getTotalElements());

        return new ResponseEntity<LikesDTO>(likesDTO, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<UserDTO> createCommentLike(HttpServletRequest request, @RequestBody CommentLikeDTO commentLikeDTO) {
        String header = request.getHeader("Authorization");
        String token = header.substring(7);

        return new ResponseEntity<UserDTO>(userMapper.toDto(commentLikeService.save(token,commentLikeDTO)), HttpStatus.OK);
    }


    @DeleteMapping
    public ResponseEntity<Void> deleteCommentLike(HttpServletRequest request, @RequestBody CommentLikeDTO commentLikeDTO){
        String header = request.getHeader("Authorization");
        String token = header.substring(7);

        commentLikeService.delete(token, commentLikeDTO);

        return new ResponseEntity<>(HttpStatus.OK);
    }
}
