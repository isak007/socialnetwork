package com.ftn.socialnetwork.util.mapper;

import com.ftn.socialnetwork.model.FriendRequest;
import com.ftn.socialnetwork.model.dto.FriendRequestDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface FriendRequestMapper {

    @Mapping(target = "senderId", source = "sender.id")
    @Mapping(target = "receiverId", source = "receiver.id")
    FriendRequestDTO toDto(FriendRequest friendRequest);

    @Mapping(target = "sender.id", source = "senderId")
    @Mapping(target = "receiver.id", source = "receiverId")
    FriendRequest toEntity(FriendRequestDTO friendRequestDTO);
}
