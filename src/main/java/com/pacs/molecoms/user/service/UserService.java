package com.pacs.molecoms.user.service;

import com.pacs.molecoms.mysql.entity.*;
import com.pacs.molecoms.mysql.repository.UserRepository;
import com.pacs.molecoms.user.dto.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    @Transactional
    public UserRes create(UserCreateReq req) {
        if (userRepository.existsByEmail(req.email()))
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");

        User u = User.builder()
                .email(req.email())
                .displayName(req.displayName())
                .dept(req.dept())
                .role(req.role())
                .status(req.status())
                .build();
        userRepository.save(u);
        return toRes(u);
    }

    @Transactional(readOnly = true)
    public Page<UserRes> list(UserStatus status, Pageable pageable) {
        Page<User> page = (status == null)
                ? userRepository.findAll(pageable)
                : userRepository.findAllByStatus(status, pageable);
        return page.map(this::toRes);
    }

    @Transactional(readOnly = true)
    public UserRes get(Long id) {
        return userRepository.findById(id).map(this::toRes)
                .orElseThrow(() -> new EntityNotFoundException("유저를 찾을 수 없습니다."));
    }

    @Transactional
    public UserRes update(Long id, UserUpdateReq req) {
        User u = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("유저를 찾을 수 없습니다."));
        u.setDisplayName(req.displayName());
        u.setDept(req.dept());
        u.setRole(req.role());
        u.setStatus(req.status());
        return toRes(u);
    }

    @Transactional
    public void deleteSoft(Long id) {
        User u = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("유저를 찾을 수 없습니다."));
        u.setStatus(UserStatus.DELETED);
    }

    private UserRes toRes(User u) {
        return new UserRes(
                u.getId(), u.getEmail(), u.getDisplayName(), u.getDept(),
                u.getRole(), u.getStatus(), u.getCreatedAt()
        );
    }
}
