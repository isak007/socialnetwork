package com.ftn.socialnetwork.service.implementation;

import com.ftn.socialnetwork.model.*;
import com.ftn.socialnetwork.model.dto.NotificationDTO;
import com.ftn.socialnetwork.repository.*;
import com.ftn.socialnetwork.security.jwt.JwtTokenUtil;
import com.ftn.socialnetwork.service.INotificationService;
import com.ftn.socialnetwork.util.exception.EntityNotFoundException;
import com.ftn.socialnetwork.util.exception.UnauthorizedException;
import com.ftn.socialnetwork.util.mapper.NotificationMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class NotificationService implements INotificationService {

    private final NotificationRepository notificationRepository;
    final int notifications_per_page = 10;
    final List<String> objectTypes = Arrays.asList("POST","COMMENT","MESSAGE","FRIEND_REQUEST");
    final List<String> activityTypes = Arrays.asList("Liked post","Liked comment","Commented on a post","Sent friend request","Sent message");

    private final UserService userService;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final ChatLineRepository chatLineRepository;
    private final FriendRequestRepository friendRequestRepository;
    private final JwtTokenUtil jwtTokenUtil;
    private final NotificationMapper notificationMapper;

    public NotificationService(NotificationRepository notificationRepository, UserService userService, PostRepository postRepository,
                               CommentRepository commentRepository, ChatLineRepository chatLineRepository, FriendRequestRepository friendRequestRepository,
                               JwtTokenUtil jwtTokenUtil, NotificationMapper notificationMapper) {
        this.notificationRepository = notificationRepository;
        this.userService = userService;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.chatLineRepository = chatLineRepository;
        this.friendRequestRepository = friendRequestRepository;
        this.jwtTokenUtil = jwtTokenUtil;
        this.notificationMapper = notificationMapper;
    }

    @Override
    public Notification findBySenderIdAndReceiverIdAndObjectId(String token, Long senderId, Long receiverId, Long objectId) {
        Long userId = jwtTokenUtil.getUserId(token);

        if (!senderId.equals(userId) && !receiverId.equals(userId)){
            throw new UnauthorizedException("You are not authorized for this action.");
        }

        Notification notification = notificationRepository.findBySenderIdAndReceiverIdAndObjectId(senderId,receiverId,objectId);

        if (notification == null){
            throw new EntityNotFoundException("Notification not found.");
        }
        return notification;

    }

    @Override
    public Page<Notification> findAllForUser(String token, int page) {
        Long userId = jwtTokenUtil.getUserId(token);

        List<Notification> notifications = notificationRepository.findAllByReceiverIdAndObjectTypeNot(userId,"MESSAGE");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        notifications.sort(Comparator.comparing(n -> LocalDateTime.parse(n.getDateCreated(), formatter)));
        Collections.reverse(notifications);

        Pageable pageable = PageRequest.of(page,this.notifications_per_page);
        final int start = (int)pageable.getOffset();
        final int end = Math.min((start + pageable.getPageSize()), notifications.size());
        return new PageImpl<>(notifications.subList(start, end), pageable, notifications.size());
    }

    @Override
    public Notification save(String token, NotificationDTO notificationDTO) {
        Long userId = jwtTokenUtil.getUserId(token);
        // if user is trying to create chat for other two users
        if (!userId.equals(notificationDTO.getSenderId())){
            throw new UnauthorizedException("You are not authorized for this action.");
        }
        if (notificationDTO.getSenderId().equals(notificationDTO.getReceiverId())){
            throw new UnauthorizedException("You are not authorized for this action.");
        }
        User sender = userService.findOne(notificationDTO.getSenderId());
        User receiver = userService.findOne(notificationDTO.getReceiverId());
        if (sender == null || receiver == null){
            throw new EntityNotFoundException("One or both users not found.");
        }
        if (!objectTypes.contains(notificationDTO.getObjectType())){
            throw new EntityNotFoundException("Invalid object type.");
        }
        if (!activityTypes.contains(notificationDTO.getActivityType())){
            throw new EntityNotFoundException("Invalid activity type.");
        }
        switch (notificationDTO.getObjectType()) {
            case "POST":
                Optional<Post> postOpt = postRepository.findById(notificationDTO.getObjectId());
                if (postOpt.isEmpty())
                    throw new EntityNotFoundException("Invalid object id.");
                // if post owner does not match receiver of notification and activity type is liking post
                if (!postOpt.get().getUser().getId().equals(notificationDTO.getReceiverId()))
                    throw new UnauthorizedException("You are not authorized for this action.");
                // if user is trying to create a post notification with someone who he's not friends with
                if (postOpt.get().getVisibility().equals("FRIENDS") && !userService.areFriends(notificationDTO.getSenderId(),notificationDTO.getReceiverId()))
                    throw new UnauthorizedException("You are not authorized for this action.");
                if (postOpt.get().getVisibility().equals("ME"))
                    throw new UnauthorizedException("You are not authorized for this action.");
                break;
            case "COMMENT":
                Optional<Comment> commentOpt =  commentRepository.findById(notificationDTO.getObjectId());
                if (commentOpt.isEmpty())
                    throw new EntityNotFoundException("Invalid object id.");
                // if user does not own the comment and activity type is commenting on a post
                if (!Objects.equals(commentOpt.get().getUser().getId(), userId) && notificationDTO.getActivityType().equals("Commented on a post"))
                    throw new UnauthorizedException("You are not authorized for this action.");
                // if comment owner does not match receiver of notification and activity type is liking comment
                if (!commentOpt.get().getUser().getId().equals(notificationDTO.getReceiverId()) && notificationDTO.getActivityType().equals("Liked comment"))
                    throw new UnauthorizedException("You are not authorized for this action.");
                // if user is trying to create a comment notification with someone who he's not friends with
                if (commentOpt.get().getPost().getVisibility().equals("FRIENDS") && !userService.areFriends(notificationDTO.getSenderId(),notificationDTO.getReceiverId()))
                    throw new UnauthorizedException("You are not authorized for this action.");
                if (commentOpt.get().getPost().getVisibility().equals("ME"))
                    throw new UnauthorizedException("You are not authorized for this action.");
                break;
            case "MESSAGE":
                if (chatLineRepository.findById(notificationDTO.getObjectId()).isEmpty())
                    throw new EntityNotFoundException("Invalid object id.");
                // if user is trying to create message notification with someone who he's not friends with
                if (!userService.areFriends(notificationDTO.getSenderId(),notificationDTO.getReceiverId()))
                    throw new UnauthorizedException("You are not authorized for this action.");
                break;
            case "FRIEND_REQUEST":
                if (friendRequestRepository.findById(notificationDTO.getObjectId()).isEmpty())
                    throw new EntityNotFoundException("Invalid object id.");
                break;
        }
        Notification notification = notificationMapper.toEntity(notificationDTO);
        notification.setDateCreated(LocalDateTime.now().toString().substring(0,23).replace("T"," "));
        notification.setSeen(false);
        return notificationRepository.save(notification);
    }

    @Override
    public Notification update(String token, NotificationDTO notificationDTO) {
        Long userId = jwtTokenUtil.getUserId(token);

        Optional<Notification> notificationOpt = notificationRepository.findById(notificationDTO.getId());
        if (notificationOpt.isEmpty()){
            throw new EntityNotFoundException("The notification you are trying to update does not exist.");
        }
        Notification notification = notificationOpt.get();

        if (!notification.getReceiver().getId().equals(userId)){
            throw new UnauthorizedException("You are not authorized for this action.");
        }

        notification.setSeen(true);
        return notificationRepository.save(notification);
    }

}
