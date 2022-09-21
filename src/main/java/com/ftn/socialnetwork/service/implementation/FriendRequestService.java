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
        Long userId = Long.valueOf(jwtTokenUtil.getUserId(token));
        return friendRequestRepository.findByReceiverIdAndRequestStatus(userId, "PENDING");
    }

    @Override
    public FriendRequest save(String token, FriendRequestDTO friendRequestDTO) {
        Long userId = Long.valueOf(jwtTokenUtil.getUserId(token));
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
        Long userId = Long.valueOf(jwtTokenUtil.getUserId(token));
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
        Long userId = Long.valueOf(jwtTokenUtil.getUserId(token));
        if (!friendRequest.getReceiver().getId().equals(userId)){
            throw new UnauthorizedException("You are not authorized for this action.");
        }

        friendRequestRepository.deleteById(id);
    }
}
