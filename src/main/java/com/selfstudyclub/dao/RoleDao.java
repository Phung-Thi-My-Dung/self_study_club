package com.selfstudyclub.dao;

import com.selfstudyclub.security.RoleCode;
import java.util.Optional;

public interface RoleDao {
    Optional<Integer> findRoleId(RoleCode roleCode);
}
