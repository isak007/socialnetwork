package com.ftn.socialnetwork.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatLinesDTO {

    private List<ChatLineDTO> chatLinesDTO;

    private int totalChatLines;

}
