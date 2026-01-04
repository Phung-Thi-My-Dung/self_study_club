package com.selfstudyclub.security;

public enum AccountStatus {
    ACTIVE, LOCKED, DELETED;

    public static AccountStatus fromDb(String s) {
        return AccountStatus.valueOf(s.trim().toUpperCase());
    }
}
