package web;

import model.User;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe In-Memory Session Manager.
 * Stores 128-bit random tokens with 30-minute idle timeout.
 * Purges expired sessions lazily on lookups/creations (no background thread).
 */
public class SessionManager {

    public static final String SESSION_COOKIE_NAME = "swiftbus_session";
    public static final long IDLE_TIMEOUT_MS = 30 * 60 * 1000L; // 30 minutes

    public static class Session {
        private final String sessionId;
        private final Integer userId;
        private final String username;
        private final String email;
        private final String role;
        private final String csrfToken;
        private volatile long lastAccessedTime;

        public Session(String sessionId, Integer userId, String username, String email, String role) {
            this.sessionId = sessionId;
            this.userId = userId;
            this.username = username;
            this.email = email;
            this.role = role != null ? role : "GUEST";
            this.csrfToken = SecurityUtils.generate128BitToken();
            this.lastAccessedTime = System.currentTimeMillis();
        }

        public String getSessionId() {
            return sessionId;
        }

        public Integer getUserId() {
            return userId;
        }

        public String getUsername() {
            return username;
        }

        public String getEmail() {
            return email;
        }

        public String getRole() {
            return role;
        }

        public String getCsrfToken() {
            return csrfToken;
        }

        public boolean isAuthenticated() {
            return userId != null && !"GUEST".equalsIgnoreCase(role);
        }

        public boolean isAdmin() {
            return "ADMIN".equalsIgnoreCase(role);
        }

        public void touch() {
            this.lastAccessedTime = System.currentTimeMillis();
        }

        public boolean isExpired(long now) {
            return (now - lastAccessedTime) > IDLE_TIMEOUT_MS;
        }
    }

    private static final ConcurrentHashMap<String, Session> sessions = new ConcurrentHashMap<>();

    /**
     * Looks up an existing session. Lazily evicts expired sessions.
     */
    public static Session getSession(String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) {
            return null;
        }

        Session session = sessions.get(sessionId);
        if (session == null) {
            return null;
        }

        long now = System.currentTimeMillis();
        if (session.isExpired(now)) {
            sessions.remove(sessionId);
            return null;
        }

        session.touch();
        return session;
    }

    /**
     * Creates an authenticated user session.
     */
    public static Session createSession(User user) {
        lazyCleanup();
        String sessionId = SecurityUtils.generate128BitToken();
        Session session = new Session(sessionId, user.getId(), user.getUsername(), user.getEmail(), user.getRole());
        sessions.put(sessionId, session);
        return session;
    }

    /**
     * Creates a lightweight guest session (for CSRF protection on public forms).
     */
    public static Session createGuestSession() {
        lazyCleanup();
        String sessionId = SecurityUtils.generate128BitToken();
        Session session = new Session(sessionId, null, "Guest", null, "GUEST");
        sessions.put(sessionId, session);
        return session;
    }

    /**
     * Invalidates a session (e.g., on logout).
     */
    public static void invalidate(String sessionId) {
        if (sessionId != null) {
            sessions.remove(sessionId);
        }
    }

    /**
     * Lazily sweeps expired sessions when sessions map grows.
     */
    private static void lazyCleanup() {
        if (sessions.size() > 100) {
            long now = System.currentTimeMillis();
            for (Map.Entry<String, Session> entry : sessions.entrySet()) {
                if (entry.getValue().isExpired(now)) {
                    sessions.remove(entry.getKey());
                }
            }
        }
    }
}
