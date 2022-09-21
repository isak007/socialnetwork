package com.ftn.socialnetwork.util.mapper;

import com.ftn.socialnetwork.model.PostLike;
import com.ftn.socialnetwork.model.dto.PostLikeDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {UserMapper.class, PostMapper.class})
public interface PostLikeMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "postId", source = "post.id")
    PostLikeDTO toDto(PostLike postLike);

    @Mapping(target = "user.id", source = "userId")
    @Mapping(target = "post.id", source = "postId")
    PostLike toEntity(PostLikeDTO postLikeDTO);
}
