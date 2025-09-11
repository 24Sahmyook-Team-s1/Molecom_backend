package com.pacs.molecoms.log.dto;

import com.pacs.molecoms.mysql.entity.DicomLogAction;

public record DicomLogRes (
    String actor,
    DicomLogAction logAction,
    String content,
    java.time.LocalDateTime createdAt
)
{}
