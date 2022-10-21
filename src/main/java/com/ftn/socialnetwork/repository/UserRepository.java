package com.ftn.socialnetwork.repository;

import com.ftn.socialnetwork.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u WHERE CONCAT(LOWER(u.firstName),' ',LOWER(u.lastName)) LIKE CONCAT('%',LOWER(?1),'%')" +
            " OR LOWER(u.username) LIKE CONCAT('%',?1,'%')")
    Page<User> searchByFirstNameLastNameUsername(String searchTerm, Pageable pageable);

}
