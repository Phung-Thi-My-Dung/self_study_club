package com.selfstudyclub.dao;

import com.selfstudyclub.security.RoleCode;
import java.util.Set;

public interface UserRoleDao {
    void assignRole(long accountId, int roleId);
    Set<RoleCode> getRoles(long accountId);
}
