package com.pacs.molecoms.log.dto;

import com.pacs.molecoms.mysql.entity.*;

public record LogRes (
    Long id,
//    User user,
    Long userId,
    DBlist db,
    LogAction logAction,
    java.time.LocalDateTime createdAt
) {}