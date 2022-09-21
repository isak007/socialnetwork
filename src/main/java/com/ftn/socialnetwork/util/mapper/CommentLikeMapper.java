package com.ftn.socialnetwork.util.mapper;

import com.ftn.socialnetwork.model.CommentLike;
import com.ftn.socialnetwork.model.dto.CommentLikeDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {UserMapper.class, CommentMapper.class})
public interface CommentLikeMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "commentId", source = "comment.id")
    CommentLikeDTO toDto(CommentLike commentLike);

    @Mapping(target = "user.id", source = "userId")
    @Mapping(target = "comment.id", source = "commentId")
    CommentLike toEntity(CommentLikeDTO commentLikeDTO);
}
