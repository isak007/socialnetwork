package com.ftn.socialnetwork.util.mapper;

import com.ftn.socialnetwork.model.CommentWithData;
import com.ftn.socialnetwork.model.dto.CommentWithDataDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {CommentMapper.class, CommentLikeMapper.class})
public interface CommentWithDataMapper {


    @Mapping(target = "commentDTO", source = "comment")
    @Mapping(target = "commentLikesDTO", source = "commentLikes")
    CommentWithDataDTO toDto(CommentWithData commentWithData);

    @Mapping(target = "comment", source = "commentDTO")
    @Mapping(target = "commentLikes", source = "commentLikesDTO")
    CommentWithData toEntity(CommentWithDataDTO commentWithDataDTO);
}
