package com.ftn.socialnetwork.model.dto;

import com.ftn.socialnetwork.util.validators.OnCreate;
import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

@Data
@AllArgsConstructor
public class NotificationDTO {

    @Null(groups = OnCreate.class)
    private Long id;

    @NotNull
    private Long senderId;

    @NotNull
    private Long receiverId;

    @NotEmpty
    private String activityType;

    // POST, COMMENT, FRIEND_REQUEST, MESSAGE
    @NotEmpty
    private String objectType;

    @NotNull
    private Long objectId;

    private String dateCreated;

    private Boolean seen;
}
