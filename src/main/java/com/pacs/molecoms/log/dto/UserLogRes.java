package com.pacs.molecoms.log.dto;

import com.pacs.molecoms.mysql.entity.DBlist;
import com.pacs.molecoms.mysql.entity.UserLogAction;

public record UserLogRes(
        Long id,
        Long actorId,
        Long targetId,
        DBlist db,
        UserLogAction logAction,
        java.time.LocalDateTime createdAt
) {}