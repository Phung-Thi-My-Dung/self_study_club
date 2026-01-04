package com.selfstudyclub.security;

import java.util.Set;

/** Desktop runtime session. */
public record Session(long accountId, String email, Set<RoleCode> roles) {
    public boolean isAdmin() { return roles.contains(RoleCode.ADMIN); }
}
