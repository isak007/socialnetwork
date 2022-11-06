package com.ftn.socialnetwork.controller;

import com.ftn.socialnetwork.model.Notification;
import com.ftn.socialnetwork.model.dto.*;
import com.ftn.socialnetwork.service.implementation.NotificationService;
import com.ftn.socialnetwork.util.mapper.NotificationMapper;
import com.ftn.socialnetwork.util.validators.OnCreate;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.websocket.server.PathParam;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "notifications")
@CrossOrigin("http://localhost:8081/")
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationMapper notificationMapper;

    public NotificationController(NotificationService notificationService, NotificationMapper notificationMapper) {
        this.notificationService = notificationService;
        this.notificationMapper = notificationMapper;
    }


    @GetMapping
    public ResponseEntity<NotificationsDTO> findAllForUser(HttpServletRequest request,
                                                           @PathParam(value = "page") Integer page) {
        String header = request.getHeader("Authorization");
        String token = header.substring(7);

        Page<Notification> notificationsPage = notificationService.findAllForUser(token,page);


        NotificationsDTO notificationsDTO = new NotificationsDTO();
        notificationsDTO.setNotifications(notificationsPage.getContent().stream().map(notificationMapper::toDto).collect(Collectors.toList()));
        notificationsDTO.setTotalNotifications((int)notificationsPage.getTotalElements());

        return new ResponseEntity<NotificationsDTO>(notificationsDTO,HttpStatus.OK);
    }

    @GetMapping(value="last-chat-line")
    public ResponseEntity<NotificationDTO> findForLastChatLine(HttpServletRequest request,
                                                               @PathParam(value = "senderId") Long senderId,
                                                               @PathParam(value = "receiverId") Long receiverId,
                                                               @PathParam(value = "objectId") Long objectId) {
        String header = request.getHeader("Authorization");
        String token = header.substring(7);

        return new ResponseEntity<NotificationDTO>(
                notificationMapper.toDto(notificationService.findBySenderIdAndReceiverIdAndObjectId(token,senderId,receiverId,objectId)),HttpStatus.OK);
    }

    @Validated(OnCreate.class)
    @PostMapping
    public ResponseEntity<NotificationDTO> createNotification(HttpServletRequest request, @Valid @RequestBody NotificationDTO notificationDTO) {
        String header = request.getHeader("Authorization");
        String token = header.substring(7);

        return new ResponseEntity<NotificationDTO>(notificationMapper.toDto(notificationService.save(token,notificationDTO)), HttpStatus.OK);
    }

    @PutMapping
    public ResponseEntity<NotificationDTO> editNotification(HttpServletRequest request, @RequestBody NotificationDTO notificationDTO) {
        String header = request.getHeader("Authorization");
        String token = header.substring(7);

        return new ResponseEntity<NotificationDTO>(notificationMapper.toDto(notificationService.update(token, notificationDTO)), HttpStatus.OK);
    }

}
