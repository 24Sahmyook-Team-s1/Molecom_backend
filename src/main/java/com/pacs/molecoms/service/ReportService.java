package com.pacs.molecoms.service;

import com.pacs.molecoms.dto.ReportRequest;
import com.pacs.molecoms.dto.ReportResponse;
import com.pacs.molecoms.mysql.entity.Report;
import com.pacs.molecoms.mysql.entity.User;
import com.pacs.molecoms.mysql.repository.ReportRepository;
import com.pacs.molecoms.mysql.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;   // ✅ 추가

    public ReportResponse saveReport(ReportRequest request) {
        // ✅ authorId로 User 엔티티 조회
        User author = userRepository.findById(request.getAuthorId())
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + request.getAuthorId()));

        // ✅ Report 엔티티 생성 (author 연결)
        Report report = Report.builder()
                .studyKey(request.getStudyKey())
                .seriesKey(request.getSeriesKey())
                .modality(request.getModality())
                .bodyPart(request.getBodyPart())
                .content(request.getContent())
                .studyUid(request.getStudyUid())
                .patientId(request.getPatientId())
                .author(author)   // ✅ authorId 대신 author 엔티티 설정
                .build();

        Report saved = reportRepository.save(report);

        // ✅ Response DTO 변환
        return ReportResponse.builder()
                .id(saved.getId())
                .studyKey(saved.getStudyKey())
                .seriesKey(saved.getSeriesKey())
                .modality(saved.getModality())
                .bodyPart(saved.getBodyPart())
                .content(saved.getContent())
                .studyUid(saved.getStudyUid())
                .patientId(saved.getPatientId())
                .authorId(saved.getAuthor().getId()) // ✅ authorId는 엔티티에서 꺼냄
                .build();
    }
}
