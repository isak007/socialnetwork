package com.ftn.socialnetwork.repository;

import com.ftn.socialnetwork.model.ChatLine;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChatLineRepository extends JpaRepository<ChatLine, Long> {

    List<ChatLine> findAllByChatId(Long chatId);

}
