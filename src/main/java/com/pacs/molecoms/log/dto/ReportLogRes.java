package com.pacs.molecoms.log.dto;

import com.pacs.molecoms.mysql.entity.ReportAction;
import java.time.LocalDateTime;

public record ReportLogRes(
        Long id,
        String actorEmail,
        Long reportId,
        ReportAction action,
        String detail,
        LocalDateTime createdAt
) {}
