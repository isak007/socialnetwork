package com.ftn.socialnetwork.model.dto;

import lombok.Data;

@Data
public class CommentDTO {

    private Long id;

    private String datePosted;

    private String text;

    private UserDTO userDTO;

    private PostDTO postDTO;

}
