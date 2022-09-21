package com.ftn.socialnetwork.model.dto;

import lombok.Data;

@Data
public class CommentLikeDTO {

    private Long id;

    private Long commentId;

    private Long userId;

}
