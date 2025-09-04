package com.pacs.molecoms.mysql.entity;

public enum LogAction {
    LOGIN,
    LOGOUT,
    CREATE,
    READ,
    READ_LIST,   // 목록 조회
    UPDATE,
    DELETE,      // 소프트 삭제
    HARD_DELETE
}
