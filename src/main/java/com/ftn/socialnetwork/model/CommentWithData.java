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

public class CommentWithData {

    private Comment comment;
    private List<User> commentLikes;
    private int totalLikes;
    private boolean liked;

}
