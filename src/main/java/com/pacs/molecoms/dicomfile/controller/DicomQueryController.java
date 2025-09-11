// com/pacs/molecoms/oracle/controller/DicomQueryController.java
package com.pacs.molecoms.dicomfile.controller;

import com.pacs.molecoms.dicomfile.dto.SeriesDto;
import com.pacs.molecoms.dicomfile.dto.StudySummaryDto;
import com.pacs.molecoms.dicomfile.dto.ImageDto;
import com.pacs.molecoms.dicomfile.service.DicomQueryService;
import com.pacs.molecoms.log.service.DicomLogService;
import com.pacs.molecoms.mysql.entity.DicomLogAction;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/dicom")
@RequiredArgsConstructor
public class DicomQueryController {

    private final DicomQueryService dicomQueryService;
    private final DicomLogService dicomLogService;

    // GET /api/studies?patientId&dateFrom&dateTo&modality
    @Operation(
            summary = "환자 UID LIKE 검색으로 STUDY 목록 조회",
            description = "Oracle PACS 메타 DB에서 환자 UID(pid) LIKE 검색을 수행하여 Study 목록을 반환합니다. " +
                    "dateFrom/dateTo는 YYYY-MM-DD 포맷(LocalDate)로 전달하며, Oracle studydate(YYYYMMDD) 범위를 필터링합니다. " +
                    "modality 파라미터를 지정하면 해당 모달리티(CT, MR, CR 등)만 조회됩니다. " +
                    "삭제되지 않은(delflag=0 또는 NULL) Study만 최신순으로 정렬해 반환합니다."
    )
    @GetMapping("/studies")
    public ResponseEntity<List<StudySummaryDto>> searchStudies(
            @RequestParam(required = false) String patientId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) String modality,
            HttpServletRequest request
    ) {
        List<StudySummaryDto> result = dicomQueryService.searchStudies(patientId, dateFrom, dateTo, modality);
        // 여러 건이 나올 수 있으므로 patientId 자체를 target으로 로그 남김
        if (patientId != null) {
            String query = String.format("query = '%s'", patientId);
            dicomLogService.saveLog(request, query, DicomLogAction.OPEN_STUDY);
        }
        return ResponseEntity.ok(result);
    }

    // GET /api/studies/{studyinsuid}
    @Operation(
            summary = "Study UID로 Series 리스트 조회",
            description = "seriestab을 조회하여 특정 Study(=studyinsuid)를 가진 series를 조회합니다. " +
                    "studyinsuid를 PathVariable로 전달하면 seriestab에서 studyinsuid를 포함한 시리즈를 조회하고, " +
                    "해당 Series를 SeriesDto 형태로 반환합니다."
    )
    @GetMapping("/studies/{studyInsUid}")
    public ResponseEntity<List<SeriesDto>> getStudy(@PathVariable String studyInsUid, HttpServletRequest request) {
        List<SeriesDto> result = dicomQueryService.getSeriesByStudyUid(studyInsUid);
        dicomLogService.saveLog(request, studyInsUid, DicomLogAction.OPEN_STUDY);
        return ResponseEntity.ok(result);
    }

    // GET /api/series/{seriesinsuid}/images
    @Operation(
            summary = "Series UID로 이미지 목록 조회",
            description = "Oracle imagetab에서 seriesinsuid에 해당하는 Series를 찾고, " +
                    "연결된 이미지들(sopInstanceUid, sopClassUid 등)을 ImageDto 목록으로 반환합니다. " +
                    "삭제되지 않은(delflag=0 또는 NULL) 이미지들만 imageKey 오름차순으로 정렬해 제공합니다."
    )
    @GetMapping("/series/{seriesInsUid}/images")
    public ResponseEntity<List<ImageDto>> getSeriesImages(@PathVariable String seriesInsUid, HttpServletRequest request) {
        List<ImageDto> result = dicomQueryService.listImagesBySeriesUid(seriesInsUid);
        dicomLogService.saveLog(request, seriesInsUid, DicomLogAction.OPEN_SERIES);
        return ResponseEntity.ok(result);
    }


}
