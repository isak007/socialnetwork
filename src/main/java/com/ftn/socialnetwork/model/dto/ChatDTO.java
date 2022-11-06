package com.ftn.socialnetwork.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
public class ChatDTO {

    private Long id;

    @NotNull
    private Long user1Id;

    @NotNull
    private Long user2Id;
}
