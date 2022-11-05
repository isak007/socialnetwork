package com.ftn.socialnetwork.util.mapper;

import com.ftn.socialnetwork.model.Notification;
import com.ftn.socialnetwork.model.dto.NotificationDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface NotificationMapper {

    @Mapping(target = "senderId", source = "sender.id")
    @Mapping(target = "receiverId", source = "receiver.id")
    NotificationDTO toDto(Notification notification);

    @Mapping(target = "sender.id", source = "senderId")
    @Mapping(target = "receiver.id", source = "receiverId")
    Notification toEntity(NotificationDTO notificationDTO);
}
