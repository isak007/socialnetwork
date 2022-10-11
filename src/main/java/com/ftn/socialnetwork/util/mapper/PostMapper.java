package com.ftn.socialnetwork.util.mapper;

import com.ftn.socialnetwork.model.Post;
import com.ftn.socialnetwork.model.dto.PostDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface PostMapper {

    @Mapping(target = "userId", source = "user.id")
    PostDTO toDto(Post post);

    @Mapping(target = "user.id", source = "userId")
    Post toEntity(PostDTO postDTO);
}
