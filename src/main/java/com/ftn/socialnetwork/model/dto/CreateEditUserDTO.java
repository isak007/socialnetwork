package com.ftn.socialnetwork.model.dto;

import com.ftn.socialnetwork.util.validators.custom.City;
import com.ftn.socialnetwork.util.validators.custom.Date;
import com.ftn.socialnetwork.util.validators.OnCreate;
import com.ftn.socialnetwork.util.validators.OnUpdate;
import lombok.Data;

import javax.validation.constraints.*;

@Data
public class CreateEditUserDTO {

    @Null(groups = OnCreate.class)
    @NotNull(groups = OnUpdate.class)
    private Long id;

    @Size(min=3,max=20)
    @NotBlank
    @NotNull
    private String username;

    @Size(min=6,max=40)
    @NotBlank
    @NotNull
    private String password;

    @Size(min=6,max=40)
    @Null(groups = OnCreate.class)
    private String newPassword;

    @Size(min=7,max=50)
    @Email
    @NotBlank
    @NotNull
    private String email;

    @Size(min=3,max=20)
    @NotBlank
    @NotNull
    private String firstName;

    @Size(min=3,max=20)
    @NotBlank
    @NotNull
    private String lastName;

    @Date
    @NotBlank
    @NotNull
    private String dateOfBirth;

    @Size(min=3,max=40)
    @City
    @NotBlank
    @NotNull
    private String city;

    @Size(min=5,max=100)
    private String profilePicture;

    @Null(groups = OnCreate.class)
    @Size(max=100)
    private String profileDescription;

    private String pictureBase64;

    @Null(groups = OnCreate.class)
    private String emailVerificationCode;
}
