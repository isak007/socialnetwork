package com.ftn.socialnetwork.service.implementation;

import com.ftn.socialnetwork.model.*;
import com.ftn.socialnetwork.model.dto.ChatDTO;
import com.ftn.socialnetwork.repository.ChatRepository;
import com.ftn.socialnetwork.security.jwt.JwtTokenUtil;
import com.ftn.socialnetwork.service.IChatService;
import com.ftn.socialnetwork.util.exception.EntityExistsException;
import com.ftn.socialnetwork.util.exception.EntityNotFoundException;
import com.ftn.socialnetwork.util.exception.UnauthorizedException;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ChatService implements IChatService {

    private final UserService userService;
    private final JwtTokenUtil jwtTokenUtil;
    private final ChatRepository chatRepository;

    public ChatService(UserService userService, JwtTokenUtil jwtTokenUtil, ChatRepository chatRepository) {
        this.userService = userService;
        this.jwtTokenUtil = jwtTokenUtil;
        this.chatRepository = chatRepository;
    }


    @Override
    public Chat findByUser1IdAndUser2Id(String token, Long user1Id, Long user2Id) {
        Long userId = jwtTokenUtil.getUserId(token);
        // if user is trying to access chat for other two users
        if (!userId.equals(user1Id) && !userId.equals(user2Id)){
            throw new UnauthorizedException("You are not authorized for this action.");
        }
        Chat chat = chatRepository.findByUser1IdAndUser2Id(user1Id,user2Id);
        if(chat == null) {
            throw new EntityNotFoundException("Chat not found!");
        }
        return chat;
    }

    @Override
    public Chat save(String token, ChatDTO chatDTO) {
        Long userId = jwtTokenUtil.getUserId(token);
        // if user is trying to create chat for other two users
        if (!userId.equals(chatDTO.getUser1Id()) && !userId.equals(chatDTO.getUser2Id())){
            throw new UnauthorizedException("You are not authorized for this action.");
        }
        // if user is trying to message someone who he's not friends with
        if (!userService.areFriends(chatDTO.getUser1Id(), chatDTO.getUser2Id())){
            throw new UnauthorizedException("You are not authorized for this action.");
        }
        if (chatRepository.findByUser1IdAndUser2Id(chatDTO.getUser1Id(),chatDTO.getUser2Id()) != null){
            throw new EntityExistsException("The chat already exists.");
        }
        User user1 = userService.findOne(chatDTO.getUser1Id());
        User user2 = userService.findOne(chatDTO.getUser2Id());
        if (user1 == null || user2 == null){
            throw new EntityNotFoundException("One or both users not found.");
        }

        Chat chat = new Chat();
        chat.setUser1(user1);
        chat.setUser2(user2);

        return chatRepository.save(chat);
    }


    @Override
    public void delete(String token, Long id) {
        Long userId = jwtTokenUtil.getUserId(token);

        Optional<Chat> chatOpt = chatRepository.findById(id);
        if (chatOpt.isEmpty()){
            throw new EntityNotFoundException("The chat you are trying to delete does not exist.");
        }

        Chat chat = chatOpt.get();
        // validate if user is participant in the chat he is trying to delete
        if (!chat.getUser1().getId().equals(userId) && !chat.getUser2().getId().equals(userId)){
            throw new UnauthorizedException("You are not authorized for this action.");
        }
        chatRepository.deleteById(id);
    }
}
