package com.selfstudyclub.service;

import com.selfstudyclub.dao.UserProfileDao;
import com.selfstudyclub.model.UserProfile;

import java.util.Optional;

public class ProfileService {
    private final UserProfileDao dao;
    public ProfileService(UserProfileDao dao) { this.dao = dao; }

    public Optional<UserProfile> get(long accountId) { return dao.findByAccountId(accountId); }
    public void save(UserProfile profile) { dao.upsert(profile); }
}
