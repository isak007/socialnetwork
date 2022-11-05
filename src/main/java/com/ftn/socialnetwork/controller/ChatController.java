package com.ftn.socialnetwork.controller;

import com.ftn.socialnetwork.model.Chat;
import com.ftn.socialnetwork.model.dto.ChatDTO;
import com.ftn.socialnetwork.service.implementation.ChatService;
import com.ftn.socialnetwork.util.mapper.ChatMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.websocket.server.PathParam;

@RestController
@RequestMapping(value = "chats")
@CrossOrigin("http://localhost:8081/")
public class ChatController {

    private final ChatService chatService;
    private final ChatMapper chatMapper;

    public ChatController(ChatService chatService, ChatMapper chatMapper) {
        this.chatService = chatService;
        this.chatMapper = chatMapper;
    }

    @GetMapping(value = "get")
    public ResponseEntity<ChatDTO> findByUser1IdAndUser2Id(HttpServletRequest request,
                                               @PathParam(value = "user1Id") Long user1Id,
                                               @PathParam(value = "user2Id") Long user2Id) {
        String header = request.getHeader("Authorization");
        String token = header.substring(7);

        Chat chat = chatService.findByUser1IdAndUser2Id(token, user1Id, user2Id);

        return new ResponseEntity<ChatDTO>(chatMapper.toDto(chat),HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<ChatDTO> createChat(HttpServletRequest request, @RequestBody ChatDTO chatDTO) {
        String header = request.getHeader("Authorization");
        String token = header.substring(7);

        return new ResponseEntity<ChatDTO>(chatMapper.toDto(chatService.save(token,chatDTO)), HttpStatus.OK);
    }


    @DeleteMapping(value = "{id}")
    public ResponseEntity<Void> deleteChat(HttpServletRequest request, @PathVariable(value = "id") Long id){
        String header = request.getHeader("Authorization");
        String token = header.substring(7);

        chatService.delete(token, id);

        return new ResponseEntity<>(HttpStatus.OK);
    }
}
