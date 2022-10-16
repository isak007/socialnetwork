package com.ftn.socialnetwork.util.mapper;

import com.ftn.socialnetwork.model.PostWithData;
import com.ftn.socialnetwork.model.dto.PostWithDataDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {PostMapper.class, CommentMapper.class, UserMapper.class})
public interface PostWithDataMapper {


    @Mapping(target = "postDTO", source = "post")
    @Mapping(target = "postLikesDTO", source = "postLikes")
    PostWithDataDTO toDto(PostWithData postWithData);

    @Mapping(target = "post", source = "postDTO")
    @Mapping(target = "postLikes", source = "postLikesDTO")
    PostWithData toEntity(PostWithDataDTO postWithDataDTO);
}
