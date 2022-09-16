package com.ftn.socialnetwork.model.dto;

import lombok.Data;

@Data
public class PostDTO {

    private Long id;

    private String picture;

    private String text;

    private String datePosted;

    private String visibility;

    private UserDTO userDTO;

}
