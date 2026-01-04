package com.selfstudyclub.security;

public enum RoleCode {
    ADMIN, MEMBER;

    public static RoleCode fromDb(String s) {
        return RoleCode.valueOf(s.trim().toUpperCase());
    }
}
