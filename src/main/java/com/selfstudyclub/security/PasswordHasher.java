package com.selfstudyclub.security;

import org.mindrot.jbcrypt.BCrypt;

/** BCrypt password hashing. */
public final class PasswordHasher {
    private PasswordHasher() {}

    public static String hash(String plain) {
        return BCrypt.hashpw(plain, BCrypt.gensalt(12));
    }

    public static boolean verify(String plain, String hash) {
        return plain != null && hash != null && BCrypt.checkpw(plain, hash);
    }
}
