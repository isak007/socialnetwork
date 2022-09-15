package com.ftn.socialnetwork.model.dto;

import lombok.Data;
import lombok.Getter;

@Data
@Getter
public class UserDTO {

    private Long id;

    private String username;

    private String password;

    private String firstName;

    private String lastName;

    private String dateOfBirth;

    private String city;

    private String profilePicture;

}
