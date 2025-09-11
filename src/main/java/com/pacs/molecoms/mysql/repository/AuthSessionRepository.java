package com.pacs.molecoms.mysql.repository;

import com.pacs.molecoms.mysql.entity.AuthSession;
import com.pacs.molecoms.mysql.entity.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AuthSessionRepository extends JpaRepository<AuthSession, Long> {
    Optional<AuthSession> findByUser_Id(Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<AuthSession> findWithLockingByUser_Id(Long userId);

    List<AuthSession> findAllByActiveTrueAndRefreshExpireAtBefore(LocalDateTime t);
}
