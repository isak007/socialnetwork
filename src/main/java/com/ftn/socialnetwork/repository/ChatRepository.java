package com.ftn.socialnetwork.repository;

import com.ftn.socialnetwork.model.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ChatRepository extends JpaRepository<Chat, Long> {

    Optional<Chat> findById(Long id);

    @Query("SELECT c FROM Chat c WHERE (c.user1.id = ?1 AND c.user2.id = ?2) OR (c.user1.id = ?2 AND c.user2.id = ?1)")
    Chat findByUser1IdAndUser2Id(Long user1Id, Long user2Id);

}
