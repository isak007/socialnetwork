package com.ftn.socialnetwork.util.mapper;

import com.ftn.socialnetwork.model.PostLike;
import com.ftn.socialnetwork.model.dto.PostLikeDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {UserMapper.class, PostMapper.class})
public interface PostLikeMapper {

    @Mapping(target = "userDTO", source = "user")
    @Mapping(target = "postDTO", source = "post")
    PostLikeDTO toDto(PostLike postLike);

    @Mapping(target = "user", source = "userDTO")
    @Mapping(target = "post", source = "postDTO")
    PostLike toEntity(PostLikeDTO postLikeDTO);
}
