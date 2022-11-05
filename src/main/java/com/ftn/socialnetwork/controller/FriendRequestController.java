package com.ftn.socialnetwork.controller;

import com.ftn.socialnetwork.model.FriendRequest;
import com.ftn.socialnetwork.model.User;
import com.ftn.socialnetwork.model.dto.FriendRequestDTO;
import com.ftn.socialnetwork.model.dto.FriendRequestsDTO;
import com.ftn.socialnetwork.model.dto.FriendsDTO;
import com.ftn.socialnetwork.service.implementation.FriendRequestService;
import com.ftn.socialnetwork.util.mapper.FriendRequestMapper;
import com.ftn.socialnetwork.util.mapper.UserMapper;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.websocket.server.PathParam;
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

    @GetMapping("check-request")
    public ResponseEntity<FriendRequestDTO> checkIfFriendRequestExists(HttpServletRequest request,
                                                                        @PathParam(value = "user1Id") Long user1Id,
                                                                        @PathParam(value = "user2Id") Long user2Id){
        String header = request.getHeader("Authorization");
        String token = header.substring(7);

        FriendRequest friendRequest = friendRequestService.checkIfFriendRequestExists(token,user1Id,user2Id);
        if (friendRequest == null){
            return new ResponseEntity<FriendRequestDTO>(HttpStatus.OK);
        }
        return new ResponseEntity<FriendRequestDTO>(friendRequestMapper.toDto(friendRequest), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<FriendRequestsDTO> findAllForUser(HttpServletRequest request, @PathParam(value = "page") Integer page) {
        String header = request.getHeader("Authorization");
        String token = header.substring(7);

        return new ResponseEntity<FriendRequestsDTO>(friendRequestService.findAllForUser(token, page), HttpStatus.OK);
    }

    @GetMapping(value="non-pending")
    public ResponseEntity<FriendRequestsDTO> findAllNonPendingForUser(HttpServletRequest request, @PathParam(value = "page") Integer page) {
        String header = request.getHeader("Authorization");
        String token = header.substring(7);

        return new ResponseEntity<FriendRequestsDTO>(friendRequestService.findAllNonPendingForUser(token, page), HttpStatus.OK);
    }

    @GetMapping(value="friends/{userId}")
    public ResponseEntity<FriendsDTO> findFriendsForUser(HttpServletRequest request,
                                                            @PathParam(value = "page") Integer page,
                                                            @PathVariable Long userId) {
        String header = request.getHeader("Authorization");
        String token = header.substring(7);

        FriendsDTO friendsDTO = new FriendsDTO();
        Page<User> friendsPage = friendRequestService.findFriendsForUser(token,userId, page);

        // for testing purposes
//        try {
//            Thread.sleep(10 * 1000);
//        } catch (InterruptedException ie) {
//            Thread.currentThread().interrupt();
//        }

        friendsDTO.setTotalFriends((int)friendsPage.getTotalElements());
        friendsDTO.setUsers(friendsPage.getContent().stream().map(userMapper::toDto).collect(Collectors.toList()));

        return new ResponseEntity<FriendsDTO>(friendsDTO,HttpStatus.OK);
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
