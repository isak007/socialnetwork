package com.ftn.socialnetwork.service.implementation;

import com.ftn.socialnetwork.model.*;
import com.ftn.socialnetwork.model.dto.ChatLineDTO;
import com.ftn.socialnetwork.repository.ChatLineRepository;
import com.ftn.socialnetwork.repository.ChatRepository;
import com.ftn.socialnetwork.repository.NotificationRepository;
import com.ftn.socialnetwork.security.jwt.JwtTokenUtil;
import com.ftn.socialnetwork.service.IChatLineService;
import com.ftn.socialnetwork.util.exception.EntityNotFoundException;
import com.ftn.socialnetwork.util.exception.UnauthorizedException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class ChatLineService implements IChatLineService {

    private final UserService userService;
    private final JwtTokenUtil jwtTokenUtil;
    private final ChatLineRepository chatLineRepository;
    private final ChatRepository chatRepository;
    private final NotificationRepository notificationRepository;
    private final int chatLinesPerPage = 10;

    public ChatLineService(UserService userService, JwtTokenUtil jwtTokenUtil, ChatLineRepository chatLineRepository,
                           ChatRepository chatRepository, NotificationRepository notificationRepository) {
        this.userService = userService;
        this.jwtTokenUtil = jwtTokenUtil;
        this.chatLineRepository = chatLineRepository;
        this.chatRepository = chatRepository;
        this.notificationRepository = notificationRepository;
    }


    @Override
    public Page<ChatLine> findAllByChatId(String token, Long chatId, int page) {
        Long userId = jwtTokenUtil.getUserId(token);

        Optional<Chat> chatOpt = chatRepository.findById(chatId);
        if (chatOpt.isEmpty()){
            throw new EntityNotFoundException("The chat whose content you are trying to access does not exist.");
        }
        if (!userId.equals(chatOpt.get().getUser1().getId()) && !userId.equals(chatOpt.get().getUser2().getId())){
            throw new UnauthorizedException("You are not authorized for this action.");
        }

        List<ChatLine> chatLines = chatLineRepository.findAllByChatId(chatId);
        int totalChatLines = chatLines.size();
        // sorting
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

        chatLines.sort(Comparator.comparing(cl -> LocalDateTime.parse(cl.getDateCreated(), formatter)));
        Collections.reverse(chatLines);

        Pageable pageable = PageRequest.of(page,this.chatLinesPerPage);
        final int start = (int)pageable.getOffset();
        final int end = Math.min((start + pageable.getPageSize()), chatLines.size());
        chatLines = chatLines.subList(start, end);
        return new PageImpl<>(chatLines, pageable, totalChatLines);
    }

    @Override
    public ChatLine save(String token, ChatLineDTO chatLineDTO) {
        Long userId = jwtTokenUtil.getUserId(token);
        // if user is trying to create chat line as someone else
        if (!chatLineDTO.getUserId().equals(userId)){
            throw new UnauthorizedException("You are not authorized for this action.");
        }
        Optional<Chat> chatOpt = chatRepository.findById(chatLineDTO.getChatId());
        if (chatOpt.isEmpty()){
            throw new EntityNotFoundException("The chat whose content you are trying to update does not exist.");
        }
        Chat chat = chatOpt.get();
        // if user is trying to access chat from other two users
        if (!userId.equals(chat.getUser1().getId()) && !userId.equals(chat.getUser2().getId())){
            throw new UnauthorizedException("You are not authorized for this action.");
        }
        // if user is trying to message someone who he's not friends with
        if (!userService.areFriends(chat.getUser1().getId(), chat.getUser2().getId())){
            throw new UnauthorizedException("You are not authorized for this action.");
        }

        User user1 = userService.findOne(chat.getUser1().getId());
        User user2 = userService.findOne(chat.getUser2().getId());
        if (user1 == null || user2 == null){
            throw new EntityNotFoundException("One or both users not found.");
        }

        ChatLine chatLine = new ChatLine();
        chatLine.setUser(user1.getId().equals(userId) ? user1 : user2);
        chatLine.setText(chatLineDTO.getText());
        chatLine.setChat(chat);
        chatLine.setDateCreated(LocalDateTime.now().toString().substring(0,23).replace("T"," "));

        ChatLine chatLineReturned = chatLineRepository.save(chatLine);

        Notification notification = new Notification();
        notification.setSender(user1.getId().equals(userId) ? user1 : user2);
        notification.setReceiver(user1.getId().equals(userId) ? user2 : user1);
        notification.setObjectType("MESSAGE");
        notification.setObjectId(chatLineReturned.getId());
        notification.setActivityType("Sent message");
        notification.setDateCreated(LocalDateTime.now().toString().substring(0,23).replace("T", " "));
        notification.setSeen(false);
        notificationRepository.save(notification);

        return chatLineReturned;
    }

}
