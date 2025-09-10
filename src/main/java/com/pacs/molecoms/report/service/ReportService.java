package com.pacs.molecoms.report.service;

import com.pacs.molecoms.report.dto.ReportRequest;
import com.pacs.molecoms.report.dto.ReportResponse;
import com.pacs.molecoms.mysql.entity.Report;
import com.pacs.molecoms.mysql.entity.ReportAction;
import com.pacs.molecoms.mysql.entity.ReportLog;
import com.pacs.molecoms.mysql.entity.User;
import com.pacs.molecoms.mysql.repository.ReportLogRepository;
import com.pacs.molecoms.mysql.repository.ReportRepository;
import com.pacs.molecoms.mysql.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final ReportLogRepository reportLogRepository;

    /**
     * 스터디키 기준 업서트(없으면 생성, 있으면 수정)
     * - 작성자는 JWT(SecurityContext)에서 조회
     * - content만 저장/수정
     */
    @Transactional
    public ReportResponse upsertByStudyKey(ReportRequest request) {
        // 작성자: JWT에서 가져오기
        User actor = currentUser();

        // 기존 리포트 조회
        Report existing = reportRepository.findByStudyKey(request.getStudyKey()).orElse(null);

        if (existing == null) {
            // 생성: 최소 필드만 저장 (studyKey, content, author)
            Report toSave = Report.builder()
                    .studyKey(request.getStudyKey())
                    .content(request.getContent())
                    .author(actor)
                    .build();
            try {
                Report saved = reportRepository.save(toSave);

                // 로그: 생성
                reportLogRepository.save(ReportLog.builder()
                        .user(actor)
                        .report(saved)
                        .action(ReportAction.CREATE)
                        .detail("Report created: studyKey=" + saved.getStudyKey())
                        .build()
                );
                return mapToResponse(saved);

            } catch (DataIntegrityViolationException e) {
                // DB 유니크 제약(uq_report_study_key) 충돌 대비
                throw new IllegalStateException("Report already exists for studyKey=" + request.getStudyKey(), e);
            }
        } else {
            // 수정: content만 업데이트, author는 유지(감사 추적을 위해)
            existing.setContent(request.getContent());

            // 로그: 수정 (행위자=현재 사용자)
            reportLogRepository.save(ReportLog.builder()
                    .user(actor)
                    .report(existing)
                    .action(ReportAction.UPDATE)
                    .detail("Report updated: studyKey=" + existing.getStudyKey())
                    .build()
            );

            return mapToResponse(existing);
        }
    }

    /**
     * 스터디키 단일 조회
     */
    @Transactional
    public ReportResponse getByStudyKey(Long studyKey) {
        Report report = reportRepository.findByStudyKey(studyKey)
                .orElseThrow(() -> new IllegalArgumentException("Report not found for studyKey: " + studyKey));

        // ⚠️ 실제로는 '현재 로그인 사용자' 기준으로 로그를 남기는 것이 바람직
        reportLogRepository.save(ReportLog.builder()
                .user(currentUser())
                .report(report)
                .action(ReportAction.VIEW)
                .detail("Report viewed: studyKey=" + studyKey)
                .build()
        );

        return mapToResponse(report);
    }

//    /**
//     * 스터디키 단일 삭제
//     */
//    @Transactional
//    public void deleteByStudyKey(Long studyKey) {
//        Report report = reportRepository.findByStudyKey(studyKey)
//                .orElseThrow(() -> new IllegalArgumentException("Report not found for studyKey: " + studyKey));
//
//        User actor = currentUser();
//        reportRepository.delete(report);
//
//        // 로그: 삭제
//        reportLogRepository.save(ReportLog.builder()
//                .user(actor)
//                .report(report)
//                .logAction(ReportAction.DELETE)
//                .detail("Report deleted: studyKey=" + studyKey)
//                .build()
//        );
//    }

    // ===== JWT → User 헬퍼 =====
    private User currentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new IllegalArgumentException("Unauthenticated: no SecurityContext");
        }
        String principal = auth.getName(); // email 또는 내부 uid(프로젝트 설정에 맞게 매핑)
        return userRepository.findByEmail(principal)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + principal));
    }

    // ===== 공통 DTO 변환 =====
    // (메타데이터(modality/bodyPart 등)는 Oracle에서 조회해 조립할 계획이면 여기서 추가 조회 후 세팅)
    private ReportResponse mapToResponse(Report report) {
        return ReportResponse.builder()
                .author(report.getAuthor().getEmail())
                .content(report.getContent())
                .createdAt(report.getCreatedAt())
                .build();
    }
}
