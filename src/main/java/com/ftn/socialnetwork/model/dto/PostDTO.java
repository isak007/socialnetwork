package com.ftn.socialnetwork.model.dto;

import com.ftn.socialnetwork.util.validators.OnCreate;
import com.ftn.socialnetwork.util.validators.OnUpdate;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

@Data
public class PostDTO {

    @Null(groups = OnCreate.class)
    @NotNull(groups = OnUpdate.class)
    private Long id;

    @NotNull
    private String picture;

    @NotNull
    private String pictureBase64;

    private String text;

    @Null
    private String datePosted;

    @NotNull
    private String visibility;

    private boolean edited;

    @NotNull
    private Long userId;
}
