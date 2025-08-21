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
    @GetMapping(
            value = "/studies/{studyKey}/series/{seriesKey}/images/{imageKey}/stream",
            produces = "application/dicom"
    )
    public ResponseEntity<StreamingResponseBody> stream(
            @PathVariable Long studyKey,
            @PathVariable Long seriesKey,
            @PathVariable Long imageKey) {

        String fname = fetchService.getFileName(studyKey, seriesKey, imageKey);

        StreamingResponseBody body = out -> {
            try (var in = fetchService.openDicomStream(studyKey, seriesKey, imageKey)) {
                in.transferTo(out);
            }
        };

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/dicom"))
                .headers(h -> h.setContentDisposition(
                        ContentDisposition.inline().filename(fname, StandardCharsets.UTF_8).build()
                ))
                // 다운로드 링크 걸기
                // .headers(h -> h.setContentDisposition(
                //        ContentDisposition.attachment().filename(fname, StandardCharsets.UTF_8).build()
                //))
                .body(body);

    }
}
