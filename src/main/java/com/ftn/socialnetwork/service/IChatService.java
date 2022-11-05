package com.ftn.socialnetwork.service;

import com.ftn.socialnetwork.model.Chat;
import com.ftn.socialnetwork.model.dto.ChatDTO;

public interface IChatService {

//    Chat findOne(Long id);

    Chat findByUser1IdAndUser2Id(String token, Long user1Id, Long user2Id);

    Chat save(String token, ChatDTO chatDTO);

//    Chat update(String token, ChatDTO chatDTO);

    void delete(String token, Long id);
}
