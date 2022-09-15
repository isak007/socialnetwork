package com.ftn.socialnetwork.service.implementation;

import com.ftn.socialnetwork.model.FriendRequest;
import com.ftn.socialnetwork.repository.FriendRequestRepository;
import com.ftn.socialnetwork.service.IFriendRequestService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class FriendRequestService implements IFriendRequestService {

    private final FriendRequestRepository friendRequestRepository;

    public FriendRequestService(FriendRequestRepository friendRequestRepository) {
        this.friendRequestRepository = friendRequestRepository;
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
    public List<FriendRequest> findAll() {
        List<FriendRequest> friendRequests = friendRequestRepository.findAll();
        return friendRequests;
    }

    @Override
    public FriendRequest save(FriendRequest friendRequest) {
        return friendRequestRepository.save(friendRequest);
    }

    @Override
    public FriendRequest update(FriendRequest friendRequest) {
        return friendRequestRepository.save(friendRequest);
    }

    @Override
    public void delete(Long id) {
        friendRequestRepository.deleteById(id);
    }
}
