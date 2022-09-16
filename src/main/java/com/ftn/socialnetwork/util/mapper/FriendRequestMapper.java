package com.ftn.socialnetwork.util.mapper;

import com.ftn.socialnetwork.model.FriendRequest;
import com.ftn.socialnetwork.model.dto.FriendRequestDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface FriendRequestMapper {

    @Mapping(target = "senderDTO", source = "sender")
    @Mapping(target = "receiverDTO", source = "receiver")
    FriendRequestDTO toDto(FriendRequest friendRequest);

    @Mapping(target = "sender", source = "senderDTO")
    @Mapping(target = "receiver", source = "receiverDTO")
    FriendRequest toEntity(FriendRequestDTO friendRequestDTO);
}
