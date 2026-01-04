package com.selfstudyclub.security;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/** In-memory session store. */
public final class SessionManager {
    private static final AtomicReference<Session> CURRENT = new AtomicReference<>();

    private SessionManager() {}

    public static Optional<Session> current() { return Optional.ofNullable(CURRENT.get()); }
    public static void set(Session s) { CURRENT.set(s); }
    public static void clear() { CURRENT.set(null); }
}
