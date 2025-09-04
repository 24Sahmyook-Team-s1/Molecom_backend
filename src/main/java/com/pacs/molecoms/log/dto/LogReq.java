package com.pacs.molecoms.log.dto;

import com.pacs.molecoms.mysql.entity.DBlist;
import com.pacs.molecoms.mysql.entity.LogAction;
import com.pacs.molecoms.mysql.entity.User;

public record LogReq (
//    Long id,
    User actor,
    User target,
    DBlist db,
    LogAction logAction//,
//    java.time.LocalDateTime createdAt
) {}
