package com.ftn.socialnetwork.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationsDTO {

    private List<NotificationDTO> notifications;

    private int totalNotifications;

}
