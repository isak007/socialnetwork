package com.ftn.socialnetwork.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
public class PasswordResetDTO {

    @NotNull
    private String passwordResetJwt;

    @NotNull
    @Size(min = 6)
    private String newPassword;
}
