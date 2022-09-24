package com.ftn.socialnetwork.controller;

import com.ftn.socialnetwork.model.dto.FriendRequestDTO;
import com.ftn.socialnetwork.service.implementation.FriendRequestService;
import com.ftn.socialnetwork.util.mapper.FriendRequestMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping(value = "friend-requests")
@CrossOrigin("http://localhost:8081/")
public class FriendRequestController {

    private final FriendRequestService friendRequestService;
    private final FriendRequestMapper friendRequestMapper;

    public FriendRequestController(FriendRequestService friendRequestService, FriendRequestMapper friendRequestMapper) {
        this.friendRequestService = friendRequestService;
        this.friendRequestMapper = friendRequestMapper;
    }

    @GetMapping
    public ResponseEntity<List<FriendRequestDTO>> findAllForUser(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        String token = header.substring(7);

        return new ResponseEntity<List<FriendRequestDTO>>(
                friendRequestService.findAllForUser(token).stream().map(friendRequestMapper::toDto).collect(Collectors.toList()),
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
