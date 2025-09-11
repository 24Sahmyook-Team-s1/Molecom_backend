package com.pacs.molecoms.report.controller;

import com.pacs.molecoms.report.dto.ReportRequest;
import com.pacs.molecoms.report.dto.ReportResponse;
import com.pacs.molecoms.report.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @Operation(
            summary = "리포트 업서트(스터디키 기준)",
            description = """
            스터디키 기준으로 리포트를 생성하거나 수정합니다.
            - 요청 본문은 content만 필요합니다.
            - 작성자는 JWT에서 자동 추출됩니다.
            """,
            responses = {
                    @ApiResponse(responseCode = "200", description = "리포트 생성/수정 성공",
                            content = @Content(schema = @Schema(implementation = ReportResponse.class))),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "409", description = "스터디키 중복 충돌(동시성 상황)")
            }
    )
    @PutMapping("/studies/{studyKey}")
    public ResponseEntity<ReportResponse> upsertByStudyKey(
            @Parameter(description = "리포트 대상 스터디키", required = true)
            @PathVariable Long studyKey,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "리포트 본문(content)만 전달",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ReportRequest.class))
            )
            @RequestBody ReportRequest request
    ) {
        request.setStudyKey(studyKey); // 경로 파라미터에 권위 부여
        return ResponseEntity.ok(reportService.upsertByStudyKey(request));
    }

    @Operation(
            summary = "리포트 조회(스터디키 기준)",
            description = "스터디키 기준으로 단일 리포트를 조회합니다. 메타데이터(modality, bodyPart 등)는 Oracle에서 조회되어 함께 반환됩니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공",
                            content = @Content(schema = @Schema(implementation = ReportResponse.class))),
                    @ApiResponse(responseCode = "404", description = "리포트 없음")
            }
    )
    @GetMapping("/studies/{studyKey}")
    public ResponseEntity<ReportResponse> getByStudyKey(
            @Parameter(description = "리포트 대상 스터디키", required = true)
            @PathVariable Long studyKey
    ) {
        return ResponseEntity.ok(reportService.getByStudyKey(studyKey));
    }

//    @Operation(
//            summary = "리포트 삭제(스터디키 기준)",
//            description = """
//            스터디키 기준으로 단일 리포트를 삭제합니다.
//            - 삭제 로그의 행위자(actor)는 JWT에서 추출됩니다.
//            """,
//            responses = {
//                    @ApiResponse(responseCode = "204", description = "삭제 성공"),
//                    @ApiResponse(responseCode = "404", description = "리포트 없음"),
//                    @ApiResponse(responseCode = "401", description = "인증 실패")
//            }
//    )
//    @DeleteMapping("/studies/{studyKey}")
//    public ResponseEntity<Void> deleteByStudyKey(
//            @Parameter(description = "리포트 대상 스터디키", required = true)
//            @PathVariable Long studyKey
//    ) {
//        // NOTE: 서비스 메서드를 `deleteByStudyKey(Long studyKey)`로 변경해 JWT에서 사용자 읽도록 맞춰주세요.
//        reportService.deleteByStudyKey(studyKey);
//        return ResponseEntity.noContent().build();
//    }
}
