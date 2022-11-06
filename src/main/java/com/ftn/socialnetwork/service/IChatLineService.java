package com.ftn.socialnetwork.service;

import com.ftn.socialnetwork.model.ChatLine;
import com.ftn.socialnetwork.model.dto.ChatLineDTO;
import org.springframework.data.domain.Page;

public interface IChatLineService {

    Page<ChatLine> findAllByChatId(String token, Long chatId, int page);

    ChatLine save(String token, ChatLineDTO chatLineDTO);

}
