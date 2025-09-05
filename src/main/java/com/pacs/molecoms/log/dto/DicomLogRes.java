package com.pacs.molecoms.log.dto;

import com.pacs.molecoms.mysql.entity.DBlist;
import com.pacs.molecoms.mysql.entity.DicomLogAction;
import com.pacs.molecoms.mysql.entity.User;
import com.pacs.molecoms.mysql.entity.UserLogAction;

public record DicomLogRes (
    String actor,
    String targetUid,
    DicomLogAction dicomLogAction,
    java.time.LocalDateTime createdAt
)
{}
