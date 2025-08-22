package com.pacs.molecoms.mysql.repository;

import com.pacs.molecoms.mysql.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    boolean existsByEmailAndProvider(String email, String provider);
    Optional<User> findByEmail(String email);
    Optional<User> findByEmailAndProvider(String email, String provider); // ✅ 추가됨
}
