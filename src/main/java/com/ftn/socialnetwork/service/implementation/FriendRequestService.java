package com.ftn.socialnetwork.service.implementation;

import com.ftn.socialnetwork.model.FriendRequest;
import com.ftn.socialnetwork.model.User;
import com.ftn.socialnetwork.model.dto.FriendRequestDTO;
import com.ftn.socialnetwork.repository.FriendRequestRepository;
import com.ftn.socialnetwork.security.jwt.JwtTokenUtil;
import com.ftn.socialnetwork.service.IFriendRequestService;
import com.ftn.socialnetwork.util.exception.EntityNotFoundException;
import com.ftn.socialnetwork.util.exception.UnauthorizedException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static java.lang.String.format;

@Service
public class FriendRequestService implements IFriendRequestService {

    private final FriendRequestRepository friendRequestRepository;
    private final JwtTokenUtil jwtTokenUtil;
    private final UserService userService;

    public FriendRequestService(FriendRequestRepository friendRequestRepository, JwtTokenUtil jwtTokenUtil, UserService userService) {
        this.friendRequestRepository = friendRequestRepository;
        this.jwtTokenUtil = jwtTokenUtil;
        this.userService = userService;
    }

    @Override
    public FriendRequest findOne(Long id) {
        Optional<FriendRequest> friendRequest = friendRequestRepository.findById(id);
        if(friendRequest.isEmpty()) {
            throw new NoSuchElementException("Friend request with id = " + id + " not found!");
        }
        return friendRequest.get();
    }

    @Override
    public List<FriendRequest> findAllForUser(String token) {
        Long userId = jwtTokenUtil.getUserId(token);
        return friendRequestRepository.findByReceiverIdAndRequestStatus(userId, "PENDING");
    }

    @Override
    public List<User> findFriendsForUser(String token, Long userId) {
        Long sessionUserId = jwtTokenUtil.getUserId(token);
        // if user is trying to view some other user's friends list
        // that he's not a friend of
        if (!sessionUserId.equals(userId) && !userService.areFriends(sessionUserId,userId)){
            throw new UnauthorizedException("You are not authorized for this action.");
        }

        List<User> friends = new ArrayList<>();
        List<FriendRequest> sentAcceptedRequests = friendRequestRepository.findBySenderIdAndRequestStatus(userId,"ACCEPTED");
        List<FriendRequest> receivedAcceptedRequests = friendRequestRepository.findByReceiverIdAndRequestStatus(userId,"ACCEPTED");

        for (FriendRequest friendRequest : sentAcceptedRequests){
            friends.add(friendRequest.getReceiver());
        }
        for (FriendRequest friendRequest : receivedAcceptedRequests){
            friends.add(friendRequest.getSender());
        }

        return friends;
    }

    @Override
    public FriendRequest save(String token, FriendRequestDTO friendRequestDTO) {
        Long userId = jwtTokenUtil.getUserId(token);
        if (!friendRequestDTO.getSenderId().equals(userId) || friendRequestDTO.getReceiverId().equals(userId)){
            throw new UnauthorizedException("You are not authorized for this action.");
        }

        FriendRequest friendRequest = new FriendRequest();
        friendRequest.setRequestStatus("PENDING");

        User sender = userService.findOne(friendRequestDTO.getSenderId());
        User receiver = userService.findOne(friendRequestDTO.getReceiverId());
        friendRequest.setSender(sender);
        friendRequest.setReceiver(receiver);

        return friendRequestRepository.save(friendRequest);
    }

    @Override
    public FriendRequest update(String token, FriendRequestDTO friendRequestDTO) {
        Long userId = jwtTokenUtil.getUserId(token);
        if (friendRequestDTO.getSenderId().equals(userId) || !friendRequestDTO.getReceiverId().equals(userId)){
            throw new UnauthorizedException("You are not authorized for this action.");
        }

        Optional<FriendRequest> friendRequestOpt = friendRequestRepository.findById(friendRequestDTO.getId());
        if (friendRequestOpt.isEmpty()){
            throw new EntityNotFoundException(format("Friend request with id '%s' not found.",friendRequestDTO.getId()));
        }

        FriendRequest friendRequest = friendRequestOpt.get();
        friendRequest.setRequestStatus(friendRequestDTO.getRequestStatus());

        return friendRequestRepository.save(friendRequest);
    }

    @Override
    public void delete(String token, Long id) {
        Optional<FriendRequest> friendRequestOpt = friendRequestRepository.findById(id);
        if (friendRequestOpt.isEmpty()){
            throw new EntityNotFoundException(format("Friend request with id '%s' not found.",id));
        }

        FriendRequest friendRequest = friendRequestOpt.get();
        Long userId = jwtTokenUtil.getUserId(token);
        if (!friendRequest.getReceiver().getId().equals(userId)){
            throw new UnauthorizedException("You are not authorized for this action.");
        }

        friendRequestRepository.deleteById(id);
    }
}
