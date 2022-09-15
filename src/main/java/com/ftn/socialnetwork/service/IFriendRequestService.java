package com.ftn.socialnetwork.service;

import com.ftn.socialnetwork.model.FriendRequest;

import java.util.List;

public interface IFriendRequestService {

    FriendRequest findOne(Long id);

    List<FriendRequest> findAll();

    FriendRequest save(FriendRequest friendRequest);

    FriendRequest update(FriendRequest friendRequest);

    void delete(Long id);
}
