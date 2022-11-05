package com.ftn.socialnetwork.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
public class ChatLineDTO {

    private Long id;

    @NotNull
    private Long chatId;

    @NotNull
    private Long userId;

    @NotBlank
    private String text;

    @NotBlank
    private String dateCreated;
}
