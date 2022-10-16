package com.ftn.socialnetwork.controller;

import com.ftn.socialnetwork.model.dto.FriendRequestDTO;
import com.ftn.socialnetwork.model.dto.UserDTO;
import com.ftn.socialnetwork.service.implementation.FriendRequestService;
import com.ftn.socialnetwork.util.mapper.FriendRequestMapper;
import com.ftn.socialnetwork.util.mapper.UserMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.websocket.server.PathParam;
import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping(value = "friend-requests")
@CrossOrigin("http://localhost:8081/")
public class FriendRequestController {

    private final FriendRequestService friendRequestService;
    private final FriendRequestMapper friendRequestMapper;
    private final UserMapper userMapper;

    public FriendRequestController(FriendRequestService friendRequestService, FriendRequestMapper friendRequestMapper, UserMapper userMapper) {
        this.friendRequestService = friendRequestService;
        this.friendRequestMapper = friendRequestMapper;
        this.userMapper = userMapper;
    }

    @GetMapping
    public ResponseEntity<List<FriendRequestDTO>> findAllForUser(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        String token = header.substring(7);

        return new ResponseEntity<List<FriendRequestDTO>>(
                friendRequestService.findAllForUser(token).stream().map(friendRequestMapper::toDto).collect(Collectors.toList()),
                HttpStatus.OK);
    }

    @GetMapping(value="friends/{userId}")
    public ResponseEntity<List<UserDTO>> findFriendsForUser(HttpServletRequest request,
                                                            @PathParam(value = "page") Integer page,
                                                            @PathVariable Long userId) {
        String header = request.getHeader("Authorization");
        String token = header.substring(7);

        return new ResponseEntity<List<UserDTO>>(
                friendRequestService.findFriendsForUser(token,userId, page).stream().map(userMapper::toDto).collect(Collectors.toList()),
                HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<FriendRequestDTO> createFriendRequest(HttpServletRequest request, @RequestBody FriendRequestDTO friendRequestDTO) {
        String header = request.getHeader("Authorization");
        String token = header.substring(7);

        return new ResponseEntity<FriendRequestDTO>(friendRequestMapper.toDto(friendRequestService.save(token,friendRequestDTO)), HttpStatus.OK);
    }

    @PutMapping
    public ResponseEntity<FriendRequestDTO> editFriendRequest(HttpServletRequest request, @RequestBody FriendRequestDTO friendRequestDTO) {
        String header = request.getHeader("Authorization");
        String token = header.substring(7);

        return new ResponseEntity<FriendRequestDTO>(friendRequestMapper.toDto(friendRequestService.update(token,friendRequestDTO)), HttpStatus.OK);
    }

    @DeleteMapping(value = "{id}")
    public ResponseEntity<Void> deleteFriendRequest(HttpServletRequest request, @PathVariable(value = "id") Long id){
        String header = request.getHeader("Authorization");
        String token = header.substring(7);

        friendRequestService.delete(token, id);

        return new ResponseEntity<>(HttpStatus.OK);
    }
}
