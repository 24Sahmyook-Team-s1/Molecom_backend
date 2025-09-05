package com.pacs.molecoms.user.controller;

import com.pacs.molecoms.exception.ErrorCode;
import com.pacs.molecoms.exception.MolecomsException;
import com.pacs.molecoms.log.service.LogService;
import com.pacs.molecoms.mysql.entity.*;
import com.pacs.molecoms.mysql.repository.UserRepository;
import com.pacs.molecoms.security.CookieUtil;
import com.pacs.molecoms.security.JwtUtil;
import com.pacs.molecoms.user.dto.*;
import com.pacs.molecoms.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService service;
    private final UserRepository userRepository;
    private final LogService logService;
    private final CookieUtil cookieUtil;
    private final JwtUtil jwtUtil;

    /** 공통: 요청 쿠키의 accessToken에서 uid 추출 후 Actor(User) 로드 */
    private User getActor(HttpServletRequest request) {
        String token = cookieUtil.getTokenFromCookie(request, "accessToken");
        if (token == null) {
            throw new MolecomsException(ErrorCode.UNAUTHORIZED, "accessToken이 없습니다.");
        }
        // 토큰에서 uid 추출 (email이 아니라 uid 클레임이라고 가정)
        String uidStr = jwtUtil.getUserIdFromToken(token);
        if (uidStr == null) {
            throw new MolecomsException(ErrorCode.UNAUTHORIZED, "토큰에 uid가 없습니다.");
        }
        return userRepository.findByEmail(uidStr)
                .orElseThrow(() -> new MolecomsException(ErrorCode.USER_NOT_FOUND, "유저를 찾을 수 없습니다."));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "유저 생성")
    @PostMapping
    public ResponseEntity<UserRes> create(@Valid @RequestBody UserCreateReq req, HttpServletRequest request) {
        UserRes created = service.create(req);
        // actor: 관리자 / target: 방금 생성된 사용자
        User actor = getActor(request);
        logService.saveLog(actor.getId(), created.id(), DBlist.USERS, UserLogAction.CREATE);
        return ResponseEntity.ok(created);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "유저 목록")
    @GetMapping
    public ResponseEntity<Page<UserRes>> list(
            @RequestParam(required = false) UserStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id,desc") String sort,
            HttpServletRequest request) {

        Sort sortObj = Sort.by(sort.split(",")[0]).descending();
        Pageable pageable = PageRequest.of(page, size, sortObj);
        Page<UserRes> result = service.list(status, pageable);

        // 리스트 조회의 target은 "목록"이라 특정 사용자 없음 → targetId를 null로 두고 액션만 남김(원하면 별도 objectId 사용)
        User actor = getActor(request);
        logService.saveLog(actor.getId(), DBlist.USERS, UserLogAction.READ_LIST);
        return ResponseEntity.ok(result);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "유저 단건 조회")
    @GetMapping("/{email}")
    public ResponseEntity<UserRes> get(@PathVariable String email, HttpServletRequest request) {
        User target = userRepository.findByEmail(email).orElseThrow(() ->
                new MolecomsException(ErrorCode.USER_NOT_FOUND, "해당 이메일이 존재하지 않습니다."));

        UserRes res = service.get(email);

        User actor = getActor(request);
        logService.saveLog(actor.getId(), target.getId(), DBlist.USERS, UserLogAction.READ);
        return ResponseEntity.ok(res);
    }

    @PreAuthorize("hasRole('ADMIN') or @self.isSelf(#id, authentication)")
    @Operation(summary = "유저 수정")
    @PutMapping("/{id}")
    public ResponseEntity<UserRes> update(@PathVariable Long id,
                                          @Valid @RequestBody UserUpdateReq req,
                                          HttpServletRequest request) {
        UserRes res = service.update(id, req);

        User actor = getActor(request);
        logService.saveLog(actor.getId(), id, DBlist.USERS, UserLogAction.UPDATE);
        return ResponseEntity.ok(res);
    }

//    @PreAuthorize("hasRole('ADMIN')")
//    @Operation(summary = "유저 삭제(소프트)")
//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> deleteSoft(@PathVariable Long id, HttpServletRequest request) {
//        service.deleteSoft(id);
//
//        User actor = getActor(request);
//        logService.saveLog(actor.getId(), id, DBlist.USERS, UserLogAction.DELETE);
//        return ResponseEntity.noContent().build();
//    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "유저 삭제(하드)")
    @DeleteMapping("/{id}/hard")
    public ResponseEntity<Void> deleteHard(@PathVariable String email, HttpServletRequest request) {
        Long deletedid = service.deleteHard(email);

        User actor = getActor(request);
        logService.saveLog(actor.getId(), deletedid, DBlist.USERS, UserLogAction.HARD_DELETE);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "로그인")
    @PostMapping("/login")
    public ResponseEntity<AuthRes> login(@RequestBody LoginReq req, HttpServletResponse response) {
        // 로그인 성공 시점에서 user가 확정되므로, 반환값에서 actor/target id를 얻는다.
        AuthRes authRes = service.login(req, response);
        String useremail = jwtUtil.getUserIdFromToken(authRes.getAccessToken());
        User user = userRepository.findByEmail(useremail).orElseThrow(() ->new MolecomsException(ErrorCode.USER_NOT_FOUND, "해당 이메일이 존재하지 않습니다."));
        logService.saveLog(user, DBlist.USERS, UserLogAction.LOGIN);
        return ResponseEntity.ok(authRes);
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "본인 정보 조회(me) - accessToken 기반")
    @GetMapping("/me")
    public ResponseEntity<UserRes> me(HttpServletRequest request) {
        UserRes me = service.meFromRequest(request);
        // 자기 자신 조회: actor = target = me.id
        logService.saveLog(me.id(), DBlist.USERS, UserLogAction.READ);
        return ResponseEntity.ok(me);
    }

    @Operation(summary = "로그아웃")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        User actor = getActor(request);
        // 서비스 로직이 세션/쿠키 정리
        service.logout(request, response);
        // 자기 자신 로그아웃: actor = target = actor.id
        logService.saveLog(actor.getId(), DBlist.USERS, UserLogAction.LOGOUT);
        return ResponseEntity.noContent().build();
    }
}
