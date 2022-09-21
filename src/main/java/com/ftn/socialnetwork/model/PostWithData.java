package com.ftn.socialnetwork.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class PostWithData {

    private Post post;
    private List<Comment> comments;
    private List<PostLike> postLikes;
    private boolean liked;

}
