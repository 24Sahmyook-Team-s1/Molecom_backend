package com.pacs.molecoms.exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.apache.catalina.connector.ClientAbortException;
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
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private <T> ResponseEntity<T> json(HttpStatusCode status, T body) {
        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body);
    }

    private Map<String, String> toDetail(FieldError fe) {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("field", fe.getField());
        m.put("rejected", String.valueOf(fe.getRejectedValue()));
        m.put("reason", Optional.ofNullable(fe.getDefaultMessage()).orElse("invalid"));
        return m;
    }

    private String traceId() {
        // MDC 연동 시 MDC.get("traceId") 사용 가능
        return UUID.randomUUID().toString(); // 절대 null 반환하지 않기
    }

    // ====== @Valid 바디 검증 실패 ======
    @Override
    @NonNull
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status, @NonNull WebRequest request) {

        var details = ex.getBindingResult().getFieldErrors().stream()
                .map(this::toDetail).collect(Collectors.toList());

        var body = ErrorResponse.of(ErrorCode.VALIDATION_ERROR,
                ErrorCode.VALIDATION_ERROR.getDefaultMessage(), details, traceId());
        return json(ErrorCode.VALIDATION_ERROR.getStatus(), body);
    }

    // ====== @RequestParam 등 누락 ======
    @Override
    @NonNull
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex, @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status, @NonNull WebRequest request) {

        var body = ErrorResponse.of(ErrorCode.BAD_REQUEST, ex.getMessage(), List.of(), traceId());
        return json(ErrorCode.BAD_REQUEST.getStatus(), body);
    }

    // ====== 잘못된 JSON, 파싱 불가 ======
    @Override
    @NonNull
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            org.springframework.http.converter.HttpMessageNotReadableException ex, @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status, @NonNull WebRequest request) {

        var body = ErrorResponse.of(ErrorCode.BAD_REQUEST, "본문을 읽을 수 없습니다.", List.of(), traceId());
        return json(ErrorCode.BAD_REQUEST.getStatus(), body);
    }

    // 403: @PreAuthorize 거부 (Spring Security 6)
    @ExceptionHandler(org.springframework.security.authorization.AuthorizationDeniedException.class)
    public ResponseEntity<Object> handleAuthorizationDenied(
            org.springframework.security.authorization.AuthorizationDeniedException ex,
            WebRequest request) {

        var body = ErrorResponse.of(
                ErrorCode.FORBIDDEN,
                "접근이 거부되었습니다.",
                List.of(),         // 필요시 FieldError 리스트 채우기
                traceId()          // 네가 쓰는 traceId() 유틸 그대로
        );
        return json(ErrorCode.FORBIDDEN.getStatus(), body);
    }

    // (호환) 구버전 AccessDeniedException 케이스도 커버
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDenied(
            org.springframework.security.access.AccessDeniedException ex,
            WebRequest request) {

        var body = ErrorResponse.of(
                ErrorCode.FORBIDDEN,
                "권한이 없습니다. 관리자에게 문의하세요.",
                List.of(),
                traceId()
        );
        return json(ErrorCode.FORBIDDEN.getStatus(), body);
    }

    // 401: 인증 실패/미인증 (필터/EntryPoint에서 던져져 들어오는 경우 대비)
    @ExceptionHandler(org.springframework.security.core.AuthenticationException.class)
    public ResponseEntity<Object> handleAuthenticationException(
            org.springframework.security.core.AuthenticationException ex,
            WebRequest request) {

        var body = ErrorResponse.of(
                ErrorCode.UNAUTHORIZED,
                "인증이 필요합니다.",
                List.of(),
                traceId()
        );
        return json(ErrorCode.UNAUTHORIZED.getStatus(), body);
    }

    // ====== 404 (yml 설정 필요 시 하단 참고) ======
    @Override
    @NonNull
    protected ResponseEntity<Object> handleNoHandlerFoundException(
            NoHandlerFoundException ex, @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status, @NonNull WebRequest request) {

        var body = ErrorResponse.of(ErrorCode.NOT_FOUND, "요청 경로를 찾을 수 없습니다.", List.of(), traceId());
        return json(ErrorCode.NOT_FOUND.getStatus(), body);
    }

    // ====== 405 ======
    @Override
    @NonNull
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status, @NonNull WebRequest request) {

        var body = ErrorResponse.of(ErrorCode.METHOD_NOT_ALLOWED, ex.getMessage(), List.of(), traceId());
        return json(ErrorCode.METHOD_NOT_ALLOWED.getStatus(), body);
    }

    // ====== 415 ======
    @Override
    @NonNull
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex, @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status, @NonNull WebRequest request) {

        var body = ErrorResponse.of(ErrorCode.UNSUPPORTED_MEDIA_TYPE, ex.getMessage(), List.of(), traceId());
        return json(ErrorCode.UNSUPPORTED_MEDIA_TYPE.getStatus(), body);
    }

    // ====== JSR-303 파라미터 검증(@Validated) ======
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        List<Map<String,String>> details = ex.getConstraintViolations().stream()
                .map(v -> {
                    Map<String,String> m = new LinkedHashMap<>();
                    m.put("field", v.getPropertyPath().toString());
                    m.put("reason", v.getMessage());
                    return m;
                }).toList();
        var body = ErrorResponse.of(ErrorCode.VALIDATION_ERROR,
                ErrorCode.VALIDATION_ERROR.getDefaultMessage(), details, traceId());
        return json(ErrorCode.VALIDATION_ERROR.getStatus(), body);
    }

    // ====== 데이터 무결성 위반 ======
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex) {
        log.warn("Data integrity violation", ex);
        var body = ErrorResponse.of(ErrorCode.DATA_INTEGRITY_VIOLATION,
                ErrorCode.DATA_INTEGRITY_VIOLATION.getDefaultMessage(), List.of(), traceId());
        return json(ErrorCode.DATA_INTEGRITY_VIOLATION.getStatus(), body);
    }

    // ====== 엔티티 없음 ======
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFound(EntityNotFoundException ex) {
        var body = ErrorResponse.of(ErrorCode.NOT_FOUND, ex.getMessage(), List.of(), traceId());
        return json(ErrorCode.NOT_FOUND.getStatus(), body);
    }

    // ====== 도메인 커스텀 예외 ======
    @ExceptionHandler(MolecomsException.class)
    public ResponseEntity<ErrorResponse> handleMolecoms(MolecomsException ex) {
        var code = ex.getErrorCode();
        var body = ErrorResponse.of(code, ex.getMessage(), List.of(), traceId());
        return json(code.getStatus(), body);
    }

    // ====== 클라이언트가 스트리밍 중단 ======
    @ExceptionHandler(ClientAbortException.class)
    public ResponseEntity<Void> handleClientAbort(ClientAbortException ex) {
        return ResponseEntity.noContent().build(); // 조용히 종료
    }

    // ====== 마지막 안전망 ======
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleEtc(Exception ex) {
        log.error("Unhandled exception", ex);
        var body = ErrorResponse.of(ErrorCode.INTERNAL_ERROR,
                ErrorCode.INTERNAL_ERROR.getDefaultMessage(), List.of(), traceId());
        return json(ErrorCode.INTERNAL_ERROR.getStatus(), body);
    }
}
