package com.pacs.molecoms.log.service;

import com.pacs.molecoms.log.dto.CombinedLogRes;
import com.pacs.molecoms.log.dto.DicomLogRes;
import com.pacs.molecoms.log.dto.ReportLogRes;
import com.pacs.molecoms.log.dto.UserLogRes;
import com.pacs.molecoms.mysql.entity.DicomLog;
import com.pacs.molecoms.mysql.entity.ReportLog;
import com.pacs.molecoms.mysql.entity.UserLog;
import com.pacs.molecoms.mysql.repository.DicomLogRepository;
import com.pacs.molecoms.mysql.repository.ReportLogRepository;
import com.pacs.molecoms.mysql.repository.UserLogRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CombinedLogService {

    private final ReportLogRepository reportLogRepository;
    private final DicomLogRepository dicomLogRepository;
    private final UserLogRepository userLogRepository;
    private final LogService logService;
    private final DicomLogService dicomLogService;

    // 기존 매퍼 메서드가 있다면 재사용하거나 아래처럼 간단 매퍼 작성
    private CombinedLogRes map(ReportLogRes r) {
        return new CombinedLogRes("REPORT", r.actor(), null, r.logAction().name(), r.content(), r.createdAt());
    }
    private CombinedLogRes map(DicomLogRes d) {
        return new CombinedLogRes("DICOM", d.actor(), null, d.logAction().name(), d.content(), d.createdAt());
    }
    private CombinedLogRes map(UserLogRes u) {
        return new CombinedLogRes("USER", u.actor(), u.target(), u.logAction().name(), null, u.createdAt());
    }

    public Page<CombinedLogRes> list(Pageable pageable) {
        // 정렬 기본값: createdAt DESC
        Sort sort = pageable.getSort().isSorted()
                ? pageable.getSort()
                : Sort.by(Sort.Direction.DESC, "createdAt");

        int page = pageable.getPageNumber();
        int size = pageable.getPageSize();
        int fetchLimit = Math.max(size, (page + 1) * size);

        PageRequest pr = PageRequest.of(0, fetchLimit, sort);

        // 각 소스에서 필요한 만큼만 가져오기
        List<CombinedLogRes> report = reportLogRepository.findAll(pr)
                .map(this::toReportRes)     // 당신의 기존 매퍼
                .map(this::map)
                .getContent();

        List<CombinedLogRes> dicom = dicomLogRepository.findAll(pr)
                .map(this::toRes)           // 당신의 기존 매퍼 (DicomLog -> DicomLogRes)
                .map(this::map)
                .getContent();

        List<CombinedLogRes> user = userLogRepository.findAll(pr)
                .map(this::toUserRes)       // 당신의 기존 매퍼
                .map(this::map)
                .getContent();

        // 합치고 createdAt 기준 정렬
        List<CombinedLogRes> merged = new ArrayList<>(report.size() + dicom.size() + user.size());
        merged.addAll(report);
        merged.addAll(dicom);
        merged.addAll(user);

        merged.sort((a, b) -> b.createdAt().compareTo(a.createdAt())); // DESC

        // 요청한 페이지 구간만 슬라이스
        int from = Math.min(page * size, merged.size());
        int to = Math.min(from + size, merged.size());
        List<CombinedLogRes> pageContent = merged.subList(from, to);

        // 총 건수는 각 테이블 count 합
        long total = reportLogRepository.count() + dicomLogRepository.count() + userLogRepository.count();

        return new PageImpl<>(pageContent, PageRequest.of(page, size, sort), total);
    }

    private ReportLogRes toReportRes(ReportLog entity) {
        ReportLogRes reportRes = logService.toReportRes(entity);
        if (reportRes == null) {
            throw new UnsupportedOperationException();
        }
        return reportRes;
    }
    private DicomLogRes toRes(DicomLog entity) {
        DicomLogRes dicomLogRes = dicomLogService.toRes(entity);
        if (dicomLogRes == null) {
            throw new UnsupportedOperationException();
        }
        return dicomLogRes;
    }
    private UserLogRes toUserRes(UserLog entity) {
        UserLogRes userLogRes = logService.toUserRes(entity);
        if (userLogRes == null) {
            throw new UnsupportedOperationException();
        }
        return userLogRes;
    }
}

