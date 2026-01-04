package com.selfstudyclub.service;

import com.selfstudyclub.dao.AccountDao;
import com.selfstudyclub.dao.RoleDao;
import com.selfstudyclub.dao.UserRoleDao;
import com.selfstudyclub.model.Account;
import com.selfstudyclub.security.AccountStatus;
import com.selfstudyclub.security.PasswordHasher;
import com.selfstudyclub.security.RoleCode;
import com.selfstudyclub.security.Session;
import com.selfstudyclub.security.SessionManager;

import java.util.Set;

/** Authentication (login/signup). */
public class AuthService {
    private final AccountDao accountDao;
    private final RoleDao roleDao;
    private final UserRoleDao userRoleDao;

    public AuthService(AccountDao accountDao, RoleDao roleDao, UserRoleDao userRoleDao) {
        this.accountDao = accountDao;
        this.roleDao = roleDao;
        this.userRoleDao = userRoleDao;
    }

    public Session login(String email, String passwordPlain) {
        Account acc = accountDao.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password."));

        if (acc.status() != AccountStatus.ACTIVE) {
            throw new IllegalStateException("Account is not ACTIVE.");
        }
        if (!PasswordHasher.verify(passwordPlain, acc.passwordHash())) {
            throw new IllegalArgumentException("Invalid email or password.");
        }

        Set<RoleCode> roles = userRoleDao.getRoles(acc.accountId());
        Session session = new Session(acc.accountId(), acc.email(), roles);
        SessionManager.set(session);
        return session;
    }

    public long signupMember(String email, String passwordPlain) {
        if (accountDao.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email already exists.");
        }
        long accountId = accountDao.createAccount(email, PasswordHasher.hash(passwordPlain));
        int memberRoleId = roleDao.findRoleId(RoleCode.MEMBER)
                .orElseThrow(() -> new IllegalStateException("Missing MEMBER role in DB."));
        userRoleDao.assignRole(accountId, memberRoleId);
        return accountId;
    }

    public void logout() {
        SessionManager.clear();
    }
}
