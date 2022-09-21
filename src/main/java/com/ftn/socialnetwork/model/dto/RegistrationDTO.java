package com.ftn.socialnetwork.model.dto;

import lombok.Data;

@Data
public class RegistrationDTO {

    private String username;

    private String password;

    private String email;

    private String firstName;

    private String lastName;

    private String dateOfBirth;

    private String city;

}
