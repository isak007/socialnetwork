package com.ftn.socialnetwork.util.mapper;

import com.ftn.socialnetwork.model.CommentLike;
import com.ftn.socialnetwork.model.dto.CommentLikeDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {UserMapper.class, CommentMapper.class})
public interface CommentLikeMapper {

    @Mapping(target = "userDTO", source = "user")
    @Mapping(target = "commentDTO", source = "comment")
    CommentLikeDTO toDto(CommentLike commentLike);

    @Mapping(target = "user", source = "userDTO")
    @Mapping(target = "comment", source = "commentDTO")
    CommentLike toEntity(CommentLikeDTO commentLikeDTO);
}
