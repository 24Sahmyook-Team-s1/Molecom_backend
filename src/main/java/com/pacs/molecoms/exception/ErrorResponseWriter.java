package com.pacs.molecoms.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public final class ErrorResponseWriter {
    private static final ObjectMapper om = new ObjectMapper();
    private ErrorResponseWriter() {}

    public static void write(HttpServletResponse res, ErrorCode code, String message) throws IOException {
        var body = ErrorResponse.of(code, message, List.of(), null);
        res.setStatus(code.getStatus().value());
        res.setContentType("application/json;charset=UTF-8");
        om.writeValue(res.getWriter(), body);
    }

    public static void writeWithDetails(HttpServletResponse res, ErrorCode code, String message, List<Map<String,String>> details) throws IOException {
        var body = ErrorResponse.of(code, message, details, null);
        res.setStatus(code.getStatus().value());
        res.setContentType("application/json;charset=UTF-8");
        om.writeValue(res.getWriter(), body);
    }
}
