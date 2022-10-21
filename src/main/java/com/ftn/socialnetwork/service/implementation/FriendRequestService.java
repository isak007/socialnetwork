package com.ftn.socialnetwork.service.implementation;

import com.ftn.socialnetwork.model.FriendRequest;
import com.ftn.socialnetwork.model.User;
import com.ftn.socialnetwork.model.dto.FriendRequestDTO;
import com.ftn.socialnetwork.model.dto.FriendRequestWithDataDTO;
import com.ftn.socialnetwork.model.dto.FriendRequestsDTO;
import com.ftn.socialnetwork.repository.FriendRequestRepository;
import com.ftn.socialnetwork.security.jwt.JwtTokenUtil;
import com.ftn.socialnetwork.service.IFriendRequestService;
import com.ftn.socialnetwork.util.exception.EntityExistsException;
import com.ftn.socialnetwork.util.exception.EntityNotFoundException;
import com.ftn.socialnetwork.util.exception.UnauthorizedException;
import com.ftn.socialnetwork.util.mapper.FriendRequestMapper;
import com.ftn.socialnetwork.util.mapper.UserMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    private final FriendRequestMapper friendRequestMapper;
    private final UserMapper userMapper;
    private final int friendsPerPage = 21;
    private final int friendRequestsPerPage = 10;


    public FriendRequestService(FriendRequestRepository friendRequestRepository, JwtTokenUtil jwtTokenUtil,
                                UserService userService, FriendRequestMapper friendRequestMapper, UserMapper userMapper) {
        this.friendRequestRepository = friendRequestRepository;
        this.jwtTokenUtil = jwtTokenUtil;
        this.userService = userService;
        this.friendRequestMapper = friendRequestMapper;
        this.userMapper = userMapper;
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
    public FriendRequest checkIfFriendRequestExists(String token, Long user1Id, Long user2Id) {
        Long userId = jwtTokenUtil.getUserId(token);
        // if user is trying to check if pending or accepted friend request exists for two other users throw unauthorized
        if (!userId.equals(user1Id) && !userId.equals(user2Id)){
            throw new UnauthorizedException("You are not authorized for this action.");
        }

        Optional<FriendRequest> friendRequestAcceptedOrPending = friendRequestRepository.checkIfFriendRequestExists(user1Id,user2Id,"PENDING","ACCEPTED");
        if (friendRequestAcceptedOrPending.isEmpty()){
            return null;
        }

        return friendRequestAcceptedOrPending.get();
    }
    @Override
    public FriendRequestsDTO findAllForUser(String token, int page) {
        Long userId = jwtTokenUtil.getUserId(token);
        Pageable pageable = PageRequest.of(page,this.friendRequestsPerPage);
        Page<FriendRequest> friendRequestsPage = friendRequestRepository.findByReceiverIdAndRequestStatus(userId, "PENDING", pageable);

        FriendRequestsDTO friendRequestsDTO = new FriendRequestsDTO();
        friendRequestsDTO.setTotalFriendRequests((int)friendRequestsPage.getTotalElements());

        List<FriendRequestWithDataDTO> friendRequestsWithDataDTO = new ArrayList<>();

        for (FriendRequest friendRequest : friendRequestsPage.getContent()){
            FriendRequestWithDataDTO friendRequestWithDataDTO = new FriendRequestWithDataDTO();
            friendRequestWithDataDTO.setFriendRequestDTO(friendRequestMapper.toDto(friendRequest));
            friendRequestWithDataDTO.setUserDTO(userMapper.toDto(friendRequest.getSender()));
            friendRequestsWithDataDTO.add(friendRequestWithDataDTO);
        }

        friendRequestsDTO.setFriendRequestsWithDataDTO(friendRequestsWithDataDTO);

        return friendRequestsDTO;
    }

    @Override
    public FriendRequestsDTO findAllNonPendingForUser(String token, int page) {
        Long userId = jwtTokenUtil.getUserId(token);
        Pageable pageable = PageRequest.of(page,this.friendRequestsPerPage);
        Page<FriendRequest> friendRequestsPage =
                friendRequestRepository.findByReceiverIdAndRequestStatusAcceptedDeclined(userId, "ACCEPTED", "DECLINED", pageable);

        FriendRequestsDTO friendRequestsDTO = new FriendRequestsDTO();
        friendRequestsDTO.setTotalFriendRequests((int)friendRequestsPage.getTotalElements());

        List<FriendRequestWithDataDTO> friendRequestsWithDataDTO = new ArrayList<>();

        for (FriendRequest friendRequest : friendRequestsPage.getContent()){
            FriendRequestWithDataDTO friendRequestWithDataDTO = new FriendRequestWithDataDTO();
            friendRequestWithDataDTO.setFriendRequestDTO(friendRequestMapper.toDto(friendRequest));
            friendRequestWithDataDTO.setUserDTO(userMapper.toDto(friendRequest.getSender()));
            friendRequestsWithDataDTO.add(friendRequestWithDataDTO);
        }

        friendRequestsDTO.setFriendRequestsWithDataDTO(friendRequestsWithDataDTO);

        return friendRequestsDTO;
    }

    @Override
    public List<User> findFriendsForUser(String token, Long userId, int page) {
        Long sessionUserId = jwtTokenUtil.getUserId(token);
        // if user is trying to view some other user's friends list
        // that he's not a friend of
//        if (!sessionUserId.equals(userId) && !userService.areFriends(sessionUserId,userId)){
//            throw new UnauthorizedException("You are not authorized for this action.");
//        }

        List<User> friends = new ArrayList<>();
        List<FriendRequest> sentAcceptedRequests = friendRequestRepository.findBySenderIdAndRequestStatus(userId,"ACCEPTED");
        List<FriendRequest> receivedAcceptedRequests = friendRequestRepository.findByReceiverIdAndRequestStatus(userId,"ACCEPTED");

        for (FriendRequest friendRequest : sentAcceptedRequests){
            friends.add(friendRequest.getReceiver());
        }
        for (FriendRequest friendRequest : receivedAcceptedRequests){
            friends.add(friendRequest.getSender());
        }

        Pageable pageable = PageRequest.of(page,this.friendsPerPage);
        final int start = (int)pageable.getOffset();
        final int end = Math.min((start + pageable.getPageSize()), friends.size());
        final Page<User> friendsPage = new PageImpl<>(friends.subList(start, end), pageable, friends.size());

        return friendsPage.getContent();
    }

    @Override
    public FriendRequest save(String token, FriendRequestDTO friendRequestDTO) {
        Long userId = jwtTokenUtil.getUserId(token);
        if (!friendRequestDTO.getSenderId().equals(userId) || friendRequestDTO.getReceiverId().equals(userId)) {
            throw new UnauthorizedException("You are not authorized for this action.");
        }
//        Optional<FriendRequest> pendingFriendRequestOpt =
//                friendRequestRepository.findBySenderIdAndReceiverIdAndRequestStatus(friendRequestDTO.getSenderId(),friendRequestDTO.getReceiverId(),"PENDING");
//        Optional<FriendRequest> acceptedFriendRequestOpt =
//                friendRequestRepository.findBySenderIdAndReceiverIdAndRequestStatus(friendRequestDTO.getSenderId(),friendRequestDTO.getReceiverId(),"ACCEPTED");

        // if friend request exists for 2 users that is still pending or is already accepted
        // forbid sending new one
        if (this.checkIfFriendRequestExists(token,friendRequestDTO.getSenderId(),friendRequestDTO.getReceiverId())!= null){
            throw new EntityExistsException("The friend request already exists.");
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
        if (friendRequestDTO.getSenderId().equals(userId) || !friendRequestDTO.getReceiverId().equals(userId) || friendRequestDTO.getRequestStatus().equals("PENDING")){
            throw new UnauthorizedException("You are not authorized for this action.");
        }

        Optional<FriendRequest> friendRequestOpt = friendRequestRepository.findBySenderIdAndReceiverIdAndRequestStatus(friendRequestDTO.getSenderId(),friendRequestDTO.getReceiverId(),"PENDING");
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
        // not needed since user can remove friends thus doesn't have to be the receiver
//        if (!friendRequest.getReceiver().getId().equals(userId)){
//            throw new UnauthorizedException("You are not authorized for this action.");
//        }

        friendRequestRepository.deleteById(id);
    }
}
