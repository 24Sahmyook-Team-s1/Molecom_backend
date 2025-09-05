package com.pacs.molecoms.log.dto;

import com.pacs.molecoms.mysql.entity.DBlist;
import com.pacs.molecoms.mysql.entity.UserLogAction;
import com.pacs.molecoms.mysql.entity.User;

public record LogReq (
//    Long id,
        User actor,
        User target,
        DBlist db,
        UserLogAction userLogAction//,
//    java.time.LocalDateTime createdAt
) {}
