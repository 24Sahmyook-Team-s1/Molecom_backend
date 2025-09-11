package com.pacs.molecoms.user.service;

import com.pacs.molecoms.exception.ErrorCode;
import com.pacs.molecoms.exception.MolecomsException;
import com.pacs.molecoms.mysql.entity.*;
import com.pacs.molecoms.mysql.repository.AuthSessionRepository;
import com.pacs.molecoms.mysql.repository.UserRepository;
import com.pacs.molecoms.security.CookieUtil;
import com.pacs.molecoms.security.JwtUtil;
import com.pacs.molecoms.user.dto.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final AuthSessionRepository sessionRepository;
    private final JwtUtil jwtUtil;
    private final CookieUtil cookieUtil;
    private final PasswordEncoder passwordEncoder;

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
                .password(passwordEncoder.encode(req.passWord()))
                .build();
        userRepository.save(u);
        return toRes(u);
    }

    @Transactional(readOnly = true)
    public Page<UserRes> list(UserStatus status, Pageable pageable) {
//        Page<User> page = (status == null)
//                ? userRepository.findAll(pageable)
//                : userRepository.findAllByStatus(status, pageable);
        Page<User> page = (status == null)
                ? page = userRepository.findAllByStatusIn(List.of(UserStatus.ACTIVE, UserStatus.INACTIVE), pageable)
                : userRepository.findAllByStatus(status, pageable);
        return page.map(this::toRes);
    }

    @Transactional(readOnly = true)
    public UserRes get(Long id) {
        return userRepository.findById(id).map(this::toRes)
                .orElseThrow(() -> new EntityNotFoundException("유저를 찾을 수 없습니다."));
    }

    @Transactional(readOnly = true)
    public UserRes get(String email) {
        return userRepository.findByEmail(email).map(this::toRes)
                .orElseThrow(() -> new EntityNotFoundException("유저를 찾을 수 없습니다."));
    }

    @Transactional
    public UserRes update(String email, UserUpdateReq req) {
        User u = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("유저를 찾을 수 없습니다."));
        u.setDisplayName(req.displayName());
        u.setDept(req.dept());
        u.setRole(req.role());
        u.setStatus(req.status());
        return toRes(u);
    }

    @Transactional
    public UserRes updatePassword(LoginReq req) {
        User u = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new EntityNotFoundException("유저를 찾을 수 없습니다."));
        u.setPassword(passwordEncoder.encode(req.getPassword()));
        return toRes(u);
    }

    @Transactional
    public Long deleteSoft(String email) {
        User u = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("유저를 찾을 수 없습니다."));
        u.setStatus(UserStatus.DELETED);
        u.setEmail(email + "_" + u.getId());
        return u.getId();
    }

//    @Transactional
//    public Long deleteHard(String email) {
//        User u = userRepository.findByEmail(email)
//                .orElseThrow(() -> new EntityNotFoundException("유저를 찾을 수 없습니다."));
//        userRepository.delete(u);
//        return u.getId();
//    }

    private UserRes toRes(User u) {
        return new UserRes( u.getEmail(), u.getDisplayName(), u.getDept(),
                u.getRole(), u.getStatus(), u.getCreatedAt()
        );
    }

    public UserRes meFromRequest(HttpServletRequest request) {
        String token = cookieUtil.getTokenFromCookie(request, "accessToken");
        if (token == null) {
            throw new MolecomsException(ErrorCode.UNAUTHORIZED, "accessToken이 없습니다.");
        }

        String uidStr = jwtUtil.getEmail(token);
        if (uidStr == null || uidStr.isBlank()) {
            throw new MolecomsException(ErrorCode.UNAUTHORIZED, "토큰에 email가 없습니다.");
        }

        var user = userRepository.findByEmail(uidStr)
                .orElseThrow(() -> new MolecomsException(ErrorCode.USER_NOT_FOUND, "유저를 찾을 수 없습니다."));
        return toRes(user);
    }



    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Transactional
    public void createDeleteEvent() {
        String sql = """
            CREATE EVENT IF NOT EXISTS delete_expired_rows
            ON SCHEDULE EVERY 1 MINUTE
            DO
              DELETE FROM auth_session
              WHERE expires_at < NOW();
            """;
        jdbcTemplate.execute(sql);
    }
}
