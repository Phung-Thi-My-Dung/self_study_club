package com.selfstudyclub.dao;

import com.selfstudyclub.model.Account;
import java.util.Optional;

public interface AccountDao {
    Optional<Account> findByEmail(String email);
    Optional<Account> findById(long accountId);
    long createAccount(String email, String passwordHash);
    void updateStatus(long accountId, String status);
}
