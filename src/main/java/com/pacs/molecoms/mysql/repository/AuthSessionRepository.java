package com.pacs.molecoms.mysql.repository;

import com.pacs.molecoms.mysql.entity.AuthSession;
import com.pacs.molecoms.mysql.entity.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AuthSessionRepository extends JpaRepository<AuthSession, Long>{
    Optional<AuthSession> findByUser(User user);

    @Query("select s from AuthSession s where s.user.id = :userId")
    Optional<AuthSession> findByUserId(@Param("userId") String userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from AuthSession s where s.user = :user")
    Optional<AuthSession> findByUserForUpdate(@Param("user") User user);

    List<AuthSession> findAllByActiveTrueAndRefreshExpireAtBefore(LocalDateTime t);
}