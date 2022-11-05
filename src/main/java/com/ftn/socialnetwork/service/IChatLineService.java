package com.ftn.socialnetwork.service;

import com.ftn.socialnetwork.model.ChatLine;
import com.ftn.socialnetwork.model.dto.ChatLineDTO;
import org.springframework.data.domain.Page;

public interface IChatLineService {

//    Chat findOne(Long id);

    Page<ChatLine> findAllByChatId(String token, Long chatId, int page);

    ChatLine save(String token, ChatLineDTO chatLineDTO);

//    ChatLine update(String token, ChatLineDTO chatLineDTO);

//    void delete(String token, Long id);
}
