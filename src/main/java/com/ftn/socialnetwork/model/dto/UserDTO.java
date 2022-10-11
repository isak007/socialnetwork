package com.ftn.socialnetwork.model.dto;

import lombok.Data;

@Data
public class UserDTO {

    private Long id;

    private String username;

    private String email;

    private String firstName;

    private String lastName;

    private String dateOfBirth;

    private String city;

    private String profilePicture;

    private String profileDescription;

}
