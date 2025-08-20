package com.pacs.molecoms.exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.lang.NonNull;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.*;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ====== @Valid 바디 검증 실패 ======
    @Override
    @NonNull
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status, @NonNull WebRequest request) {

        List<Map<String,String>> details = ex.getBindingResult().getFieldErrors().stream()
                .map(this::toDetail).toList();

        var body = ErrorResponse.of(ErrorCode.VALIDATION_ERROR, ErrorCode.VALIDATION_ERROR.getDefaultMessage(), details, traceId());
        return ResponseEntity.status(ErrorCode.VALIDATION_ERROR.getStatus()).body(body);
    }

    private Map<String,String> toDetail(FieldError fe) {
        return Map.of(
                "field", fe.getField(),
                "rejected", String.valueOf(fe.getRejectedValue()),
                "reason", Optional.ofNullable(fe.getDefaultMessage()).orElse("invalid")
        );
    }

    // ====== @RequestParam 등 누락 ======
    @Override
    @NonNull
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex, @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status, @NonNull WebRequest request) {

        var body = ErrorResponse.of(ErrorCode.BAD_REQUEST, ex.getMessage(), List.of(), traceId());
        return ResponseEntity.status(ErrorCode.BAD_REQUEST.getStatus()).body(body);
    }

    // ====== 잘못된 JSON, 파싱 불가 ======
    @Override
    @NonNull
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            org.springframework.http.converter.HttpMessageNotReadableException ex, @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status, @NonNull WebRequest request) {

        var body = ErrorResponse.of(ErrorCode.BAD_REQUEST, "본문을 읽을 수 없습니다.", List.of(), traceId());
        return ResponseEntity.status(ErrorCode.BAD_REQUEST.getStatus()).body(body);
    }

    // ====== 404 (설정 필요, 아래 yml 참고) ======
    @Override
    @NonNull
    protected ResponseEntity<Object> handleNoHandlerFoundException(
            NoHandlerFoundException ex, @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status, @NonNull WebRequest request) {

        var body = ErrorResponse.of(ErrorCode.NOT_FOUND, "요청 경로를 찾을 수 없습니다.", List.of(), traceId());
        return ResponseEntity.status(ErrorCode.NOT_FOUND.getStatus()).body(body);
    }

    // ====== 405 ======
    @Override
    @NonNull
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status, @NonNull WebRequest request) {

        var body = ErrorResponse.of(ErrorCode.METHOD_NOT_ALLOWED, ex.getMessage(), List.of(), traceId());
        return ResponseEntity.status(ErrorCode.METHOD_NOT_ALLOWED.getStatus()).body(body);
    }

    // ====== 415 ======
    @Override
    @NonNull
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex, @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status, @NonNull WebRequest request) {

        var body = ErrorResponse.of(ErrorCode.UNSUPPORTED_MEDIA_TYPE, ex.getMessage(), List.of(), traceId());
        return ResponseEntity.status(ErrorCode.UNSUPPORTED_MEDIA_TYPE.getStatus()).body(body);
    }

    // ====== JSR-303 파라미터 검증(@Validated) ======
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        List<Map<String,String>> details = ex.getConstraintViolations().stream()
                .map(v -> Map.of("field", v.getPropertyPath().toString(), "reason", v.getMessage()))
                .toList();
        var body = ErrorResponse.of(ErrorCode.VALIDATION_ERROR, ErrorCode.VALIDATION_ERROR.getDefaultMessage(), details, traceId());
        return ResponseEntity.status(ErrorCode.VALIDATION_ERROR.getStatus()).body(body);
    }

    // ====== 데이터 무결성 위반 ======
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex) {
        log.warn("Data integrity violation", ex);
        var body = ErrorResponse.of(ErrorCode.DATA_INTEGRITY_VIOLATION, ErrorCode.DATA_INTEGRITY_VIOLATION.getDefaultMessage(), List.of(), traceId());
        return ResponseEntity.status(ErrorCode.DATA_INTEGRITY_VIOLATION.getStatus()).body(body);
    }

    // ====== 엔티티 없음 ======
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFound(EntityNotFoundException ex) {
        var body = ErrorResponse.of(ErrorCode.NOT_FOUND, ex.getMessage(), List.of(), traceId());
        return ResponseEntity.status(ErrorCode.NOT_FOUND.getStatus()).body(body);
    }

    // ====== 도메인 커스텀 예외 ======
    @ExceptionHandler(MolecomsException.class)
    public ResponseEntity<ErrorResponse> handleMolecoms(MolecomsException ex) {
        var code = ex.getErrorCode();
        var body = ErrorResponse.of(code, ex.getMessage(), List.of(), traceId());
        return ResponseEntity.status(code.getStatus()).body(body);
    }

    // ====== 마지막 안전망 ======
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleEtc(Exception ex) {
        log.error("Unhandled exception", ex);
        var body = ErrorResponse.of(ErrorCode.INTERNAL_ERROR, ErrorCode.INTERNAL_ERROR.getDefaultMessage(), List.of(), traceId());
        return ResponseEntity.status(ErrorCode.INTERNAL_ERROR.getStatus()).body(body);
    }

    private String traceId() {
        // 필요하면 MDC에서 가져오기 (예: Sleuth/Logback MDC 연동)
        // return MDC.get("traceId");
        return null;
    }
}
