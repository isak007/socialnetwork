package com.ftn.socialnetwork.util.mapper;

import com.ftn.socialnetwork.model.ChatLine;
import com.ftn.socialnetwork.model.dto.ChatLineDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {UserMapper.class, ChatMapper.class})
public interface ChatLineMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "chatId", source = "chat.id")
    ChatLineDTO toDto(ChatLine chatLine);

    @Mapping(target = "user.id", source = "userId")
    @Mapping(target = "chat.id", source = "chatId")
    ChatLine toEntity(ChatLineDTO chatLineDTO);
}
