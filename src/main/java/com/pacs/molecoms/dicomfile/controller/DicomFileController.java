package com.pacs.molecoms.dicomfile.controller;

import com.pacs.molecoms.dicomfile.service.DicomFileFetchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/dicom")
@RequiredArgsConstructor
public class DicomFileController {

    private final DicomFileFetchService fetchService;

    @Operation(summary = "DICOM 스트리밍")
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(
                    mediaType = "application/dicom",
                    schema = @Schema(type = "string", format = "binary")
            )
    )
    @GetMapping("/dicom/studies/{studyKey}/series/{seriesKey}/images/{imageKey}/stream")
    public ResponseEntity<StreamingResponseBody> stream(
            @PathVariable Long studyKey,
            @PathVariable Long seriesKey,
            @PathVariable Long imageKey) throws IOException {

        // 1) 미리 연다 → 여기서 실패하면 예외 처리기로 감
        final var in = fetchService.openDicomStream(studyKey, seriesKey, imageKey);
        final var fname = fetchService.getFileName(studyKey, seriesKey, imageKey);

        StreamingResponseBody body = out -> {
            try (var is = in) {
                is.transferTo(out);
            } catch (org.apache.catalina.connector.ClientAbortException e) {
                // 클라이언트 취소는 정상 흐름으로 보고 조용히 무시
                // (로그 레벨 낮게)
            } catch (Exception e) {
                // 스트리밍 중 예외는 여기서 끝내고 밖으로 던지지 말 것
                // 밖으로 나가면 응답은 이미 200/디콤으로 커밋된 상태라 500 로그가 또 뜸
                // 필요 시 내부 로깅만
            }
        };

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/dicom"))
                .headers(h -> h.setContentDisposition(
                        ContentDisposition.inline().filename(fname, java.nio.charset.StandardCharsets.UTF_8).build()))
                .body(body);
    }

}
