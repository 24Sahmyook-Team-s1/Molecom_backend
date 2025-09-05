package com.pacs.molecoms.log.dto;

import com.pacs.molecoms.mysql.entity.ReportAction;
import java.time.LocalDateTime;

public record ReportLogRes(
        String actorEmail,
        ReportAction action,
        String detail,
        LocalDateTime createdAt
) {}
