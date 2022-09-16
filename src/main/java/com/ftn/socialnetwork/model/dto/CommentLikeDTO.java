package com.ftn.socialnetwork.model.dto;

import lombok.Data;

@Data
public class CommentLikeDTO {

    private Long id;

    private CommentDTO commentDTO;

    private UserDTO userDTO;

}
