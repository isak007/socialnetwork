package com.ftn.socialnetwork.util.mapper;

import com.ftn.socialnetwork.model.Chat;
import com.ftn.socialnetwork.model.dto.ChatDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface ChatMapper {

    @Mapping(target = "user1Id", source = "user1.id")
    @Mapping(target = "user2Id", source = "user2.id")
    ChatDTO toDto(Chat chat);

    @Mapping(target = "user1.id", source = "user1Id")
    @Mapping(target = "user2.id", source = "user2Id")
    Chat toEntity(ChatDTO chatDTO);
}
