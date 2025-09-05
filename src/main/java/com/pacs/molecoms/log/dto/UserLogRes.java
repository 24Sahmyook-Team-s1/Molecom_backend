package com.pacs.molecoms.log.dto;

import com.pacs.molecoms.mysql.entity.DBlist;
import com.pacs.molecoms.mysql.entity.UserLogAction;

public record UserLogRes(
        String actor,
        String target,
        UserLogAction logAction,
        java.time.LocalDateTime createdAt
) {}