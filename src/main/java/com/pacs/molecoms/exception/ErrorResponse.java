package com.pacs.molecoms.exception;

import lombok.*;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    private String code;                       // 예: "VALIDATION_ERROR"
    private int status;                        // 예: 400
    private String message;                    // 사람 읽을 수 있는 메시지
    private List<Map<String, String>> details; // 필드 단위 오류 등
    private String traceId;                    // 요청 추적용
    private Instant timestamp;                 // 발생 시각

    public static ErrorResponse of(ErrorCode codeEnum,
                                   String message,
                                   List<Map<String, String>> details,
                                   String traceId) {
        Objects.requireNonNull(codeEnum, "codeEnum must not be null");
        HttpStatus http = codeEnum.getStatus();
        return ErrorResponse.builder()
                .code(codeEnum.name())
                .status(http.value())
                .message(Objects.requireNonNullElse(message, codeEnum.getDefaultMessage()))
                .details(details == null ? List.of() : details)
                .traceId(traceId == null ? "" : traceId) // null 금지
                .timestamp(Instant.now())
                .build();
    }
}
