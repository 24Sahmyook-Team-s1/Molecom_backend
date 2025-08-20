package com.pacs.molecoms.exception;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

public record ErrorResponse(
        boolean success,
        Map<String, Object> error
) {
    public static ErrorResponse of(ErrorCode code, String message, List<Map<String, String>> details, String traceId) {
        return new ErrorResponse(false, Map.of(
                "status", code.getStatus().value(),
                "code", code.getCode(),
                "message", message != null ? message : code.getDefaultMessage(),
                "details", details,
                "traceId", traceId,
                "timestamp", OffsetDateTime.now().toString()
        ));
    }
}
