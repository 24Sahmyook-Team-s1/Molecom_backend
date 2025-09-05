package com.pacs.molecoms.dicomfile.controller;

import com.pacs.molecoms.dicomfile.service.DicomFileFetchService;
import com.pacs.molecoms.log.service.DicomLogService;
import com.pacs.molecoms.mysql.entity.DicomLogAction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;

@RestController
@RequestMapping("/api/dicom")
@RequiredArgsConstructor
public class DicomFileController {

    private final DicomFileFetchService fetchService;
    private final DicomLogService dicomLogService;

    @Operation(summary = "DICOM 스트리밍")
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(
                    mediaType = "application/dicom",
                    schema = @Schema(type = "string", format = "binary")
            )
    )
    @GetMapping("/studies/{studyKey}/series/{seriesKey}/images/{imageKey}/stream")
    public ResponseEntity<StreamingResponseBody> stream(
            @PathVariable Long studyKey,
            @PathVariable Long seriesKey,
            @PathVariable Long imageKey,
            HttpServletRequest request) throws IOException {

        // 1) DICOM 파일 스트림 열기
        final var in = fetchService.openDicomStream(studyKey, seriesKey, imageKey);
        final var fname = fetchService.getFileName(studyKey, seriesKey, imageKey);

        String targetUid = String.format("study=%d,series=%d,image=%d", studyKey, seriesKey, imageKey);
        dicomLogService.saveLog(request, targetUid, DicomLogAction.OPEN_FILE);

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
