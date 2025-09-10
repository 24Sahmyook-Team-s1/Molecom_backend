package com.pacs.molecoms.log.dto;

import java.time.LocalDateTime;

public record CombinedLogRes(
        String type,          // "REPORT", "DICOM", "USER"
        String actor,
        String target,        // Report/Dicom 은 null 가능
        String action,
        String content,
        LocalDateTime createdAt
) {}

