package com.pacs.molecoms.log.dto;

import com.pacs.molecoms.mysql.entity.Report;
import com.pacs.molecoms.mysql.entity.ReportAction;
import com.pacs.molecoms.mysql.entity.User;

public record ReportLogReq(
        User actor,         // 누가
        Report report,      // 어떤 Report에 대해
        ReportAction action,// CREATE / VIEW
        String detail       // 상세 메시지
) {}
