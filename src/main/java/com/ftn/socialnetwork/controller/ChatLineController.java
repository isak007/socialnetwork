package com.ftn.socialnetwork.controller;

import com.ftn.socialnetwork.model.ChatLine;
import com.ftn.socialnetwork.model.dto.ChatLineDTO;
import com.ftn.socialnetwork.model.dto.ChatLinesDTO;
import com.ftn.socialnetwork.service.implementation.ChatLineService;
import com.ftn.socialnetwork.util.mapper.ChatLineMapper;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.websocket.server.PathParam;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "chat-lines")
@CrossOrigin("http://localhost:8081/")
public class ChatLineController {

    private final ChatLineService chatLineService;
    private final ChatLineMapper chatLineMapper;

    public ChatLineController(ChatLineService chatLineService, ChatLineMapper chatLineMapper) {
        this.chatLineService = chatLineService;
        this.chatLineMapper = chatLineMapper;
    }


    @GetMapping(value = "get")
    public ResponseEntity<ChatLinesDTO> findAllByChatId(HttpServletRequest request,
                                               @PathParam(value = "chatId") Long chatId,
                                               @PathParam(value = "page") Integer page) {
        String header = request.getHeader("Authorization");
        String token = header.substring(7);

        Page<ChatLine> chatLinesPage = chatLineService.findAllByChatId(token, chatId, page);

        ChatLinesDTO chatLinesDTO = new ChatLinesDTO();
        chatLinesDTO.setChatLinesDTO(chatLinesPage.getContent().stream().map(chatLineMapper::toDto).collect(Collectors.toList()));
        chatLinesDTO.setTotalChatLines((int)chatLinesPage.getTotalElements());

        return new ResponseEntity<ChatLinesDTO>(chatLinesDTO,HttpStatus.OK);
    }

    @GetMapping(value = "get-last")
    public ResponseEntity<ChatLineDTO> findAllByChatId(HttpServletRequest request,
                                                        @PathParam(value = "chatId") Long chatId) {
        String header = request.getHeader("Authorization");
        String token = header.substring(7);

        Page<ChatLine> chatLinesPage = chatLineService.findAllByChatId(token, chatId, 0);
        ChatLineDTO chatLineDTO = chatLineMapper.toDto(chatLinesPage.getContent().get(0));

        return new ResponseEntity<ChatLineDTO>(chatLineDTO,HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<ChatLineDTO> createChatLine(HttpServletRequest request, @RequestBody ChatLineDTO chatLineDTO) {
        String header = request.getHeader("Authorization");
        String token = header.substring(7);

        return new ResponseEntity<ChatLineDTO>(chatLineMapper.toDto(chatLineService.save(token,chatLineDTO)), HttpStatus.OK);
    }

}
