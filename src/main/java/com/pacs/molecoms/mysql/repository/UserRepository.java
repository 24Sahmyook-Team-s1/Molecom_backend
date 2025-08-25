package com.pacs.molecoms.mysql.repository;

import com.pacs.molecoms.mysql.entity.User;
import com.pacs.molecoms.mysql.entity.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
//    boolean existsByEmailAndProvider(String email, String provider);
    Optional<User> findById(Long id);
    Optional<User> findByEmail(String email);
//    Optional<User> findByEmailAndProvider(String email, String provider); // ✅ 추가됨
    List<User> findByDeptOrderByDisplayNameAsc(String dept);
    Page<User> findAllByStatus(UserStatus status, Pageable pageable);
}
