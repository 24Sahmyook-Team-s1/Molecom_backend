package com.pacs.molecoms.user.controller;

import com.pacs.molecoms.mysql.entity.UserStatus;
import com.pacs.molecoms.user.dto.*;
import com.pacs.molecoms.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
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

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "유저 생성")
    @PostMapping
    public ResponseEntity<UserRes> create(@Valid @RequestBody UserCreateReq req) {
        return ResponseEntity.ok(service.create(req));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "유저 목록")
    @GetMapping
    public ResponseEntity<Page<UserRes>> list(
            @RequestParam(required = false) UserStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id,desc") String sort) {

        Sort sortObj = Sort.by(sort.split(",")[0])
                .descending();
        Pageable pageable = PageRequest.of(page, size, sortObj);
        return ResponseEntity.ok(service.list(status, pageable));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "유저 단건 조회")
    @GetMapping("/{id}")
    public ResponseEntity<UserRes> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.get(id));
    }

    @PreAuthorize("hasRole('ADMIN') or @self.isSelf(#id, authentication)")
    @Operation(summary = "유저 수정")
    @PutMapping("/{id}")
    public ResponseEntity<UserRes> update(@PathVariable Long id,
                                          @Valid @RequestBody UserUpdateReq req) {
        return ResponseEntity.ok(service.update(id, req));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "유저 삭제(소프트)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteSoft(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "유저 삭제(하드)")
    @DeleteMapping("/{id}/Hard")
    public ResponseEntity<Void> deletedelete(@PathVariable Long id) {
        service.deleteHard(id);
        return ResponseEntity.noContent().build();
    }
}
