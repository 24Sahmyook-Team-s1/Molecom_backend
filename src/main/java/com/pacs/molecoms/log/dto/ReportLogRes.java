package com.pacs.molecoms.log.dto;

import com.pacs.molecoms.mysql.entity.ReportAction;
import java.time.LocalDateTime;

public record ReportLogRes(
        String actor,
        ReportAction action,
        String content,
        LocalDateTime createdAt
) {}
