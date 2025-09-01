package com.pacs.molecoms.mysql.repository;

import com.pacs.molecoms.mysql.entity.Log;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LogRepository extends JpaRepository<Log, Long> {
//    boolean existsByEmail(String email);
//        boolean existsByEmailAndProvider(String email, String provider);
//    Optional<User> findById(Long id);
//    Optional<User> findByEmail(String email);
//        Optional<User> findByEmailAndProvider(String email, String provider); // ✅ 추가됨
//    List<User> findByDeptOrderByDisplayNameAsc(String dept);
//    Page<User> findAllByStatus(UserStatus status, Pageable pageable);
}
