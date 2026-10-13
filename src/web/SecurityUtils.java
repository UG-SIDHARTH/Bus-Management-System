package web;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HexFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Pure JDK Security Utilities:
 * - PBKDF2WithHmacSHA256 hashing and constant-time verification.
 * - Password conditions & strength evaluation (Weak, Better, Best).
 * - In-memory IP-based rate limiting for logins and registrations.
 * - Secure random token generation for sessions and CSRF.
 */
public class SecurityUtils {

    // Tunable iteration count for slow CPUs (Pentium 1-2 cores)
    public static final int DEFAULT_PBKDF2_ITERATIONS = 65000;
    private static final int KEY_LENGTH_BITS = 256;
    private static final int SALT_BYTES = 16;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public static final String SPECIAL_CHARACTERS = "!@#$%^&*()_+-=[]{};':\"\\|,.<>/?~`";

    // Login rate-limiting: max 5 failures within 5 minutes per IP
    private static final int MAX_FAILED_LOGIN_ATTEMPTS = 5;
    private static final long LOGIN_RATE_LIMIT_WINDOW_MS = 5 * 60 * 1000L;
    private static final ConcurrentHashMap<String, Deque<Long>> failedAttemptsByIp = new ConcurrentHashMap<>();

    // Registration rate-limiting: max 5 registration attempts within 10 minutes per IP
    private static final int MAX_REGISTRATION_ATTEMPTS = 5;
    private static final long REG_RATE_LIMIT_WINDOW_MS = 10 * 60 * 1000L;
    private static final ConcurrentHashMap<String, Deque<Long>> registrationAttemptsByIp = new ConcurrentHashMap<>();

    public enum PasswordStrength {
        WEAK("Weak", "#ef4444"),
        BETTER("Better", "#f59e0b"),
        BEST("Best", "#10b981");

        private final String label;
        private final String color;

        PasswordStrength(String label, String color) {
            this.label = label;
            this.color = color;
        }

        public String getLabel() {
            return label;
        }

        public String getColor() {
            return color;
        }
    }

    public static int getPbkdf2Iterations() {
        String env = System.getenv("PBKDF2_ITERATIONS");
        if (env != null && !env.trim().isEmpty()) {
            try {
                int it = Integer.parseInt(env.trim());
                if (it >= 1000) return it;
            } catch (NumberFormatException ignored) {}
        }
        return DEFAULT_PBKDF2_ITERATIONS;
    }

    public static byte[] generateRandomSalt() {
        byte[] salt = new byte[SALT_BYTES];
        SECURE_RANDOM.nextBytes(salt);
        return salt;
    }

    public static String bytesToHex(byte[] bytes) {
        return HexFormat.of().formatHex(bytes);
    }

    public static byte[] hexToBytes(String hex) {
        return HexFormat.of().parseHex(hex);
    }

    public static String generateRandomToken(int bytesCount) {
        byte[] token = new byte[bytesCount];
        SECURE_RANDOM.nextBytes(token);
        return bytesToHex(token);
    }

    public static String generate128BitToken() {
        return generateRandomToken(16);
    }

    // -------------------------------------------------------------------------
    // PASSWORD STRENGTH & POLICY
    // -------------------------------------------------------------------------

    public static boolean containsSpecialChar(String password) {
        if (password == null) return false;
        for (char c : password.toCharArray()) {
            if (SPECIAL_CHARACTERS.indexOf(c) != -1) {
                return true;
            }
        }
        return false;
    }

    /**
     * Mandatory policy: minimum 8 characters and must contain at least 1 special character.
     */
    public static boolean meetsPasswordPolicy(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        return containsSpecialChar(password);
    }

    /**
     * Evaluates password strength:
     * - Weak: < 8 chars, or missing special characters.
     * - Better: >= 8 chars, contains at least one special character, and has letters/digits.
     * - Best: >= 10 chars, has uppercase, lowercase, numbers, and special characters.
     */
    public static PasswordStrength evaluatePasswordStrength(String password) {
        if (!meetsPasswordPolicy(password)) {
            return PasswordStrength.WEAK;
        }

        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        int specialCount = 0;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isDigit(c)) hasDigit = true;
            else if (SPECIAL_CHARACTERS.indexOf(c) != -1) specialCount++;
        }

        if (password.length() >= 10 && hasUpper && hasLower && hasDigit && specialCount >= 1) {
            return PasswordStrength.BEST;
        }

        return PasswordStrength.BETTER;
    }

    // -------------------------------------------------------------------------
    // CRYPTOGRAPHY
    // -------------------------------------------------------------------------

    public static String hashPassword(char[] password, byte[] salt, int iterations) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, KEY_LENGTH_BITS);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] hash = skf.generateSecret(spec).getEncoded();
            spec.clearPassword();
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("PBKDF2 algorithm unavailable", e);
        }
    }

    public static boolean verifyPassword(char[] password, String saltHex, String expectedHashHex, int iterations) {
        if (password == null || saltHex == null || expectedHashHex == null) {
            return false;
        }
        try {
            byte[] salt = hexToBytes(saltHex);
            byte[] expectedHash = hexToBytes(expectedHashHex);
            PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, KEY_LENGTH_BITS);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] actualHash = skf.generateSecret(spec).getEncoded();
            spec.clearPassword();
            return MessageDigest.isEqual(actualHash, expectedHash);
        } catch (Exception e) {
            return false;
        }
    }

    // -------------------------------------------------------------------------
    // RATE LIMITING (Login & Registration)
    // -------------------------------------------------------------------------

    public static boolean isRateLimited(String ip) {
        if (ip == null || ip.isEmpty()) return false;
        long now = System.currentTimeMillis();

        if (failedAttemptsByIp.size() > 500) {
            cleanupExpiredLimits(failedAttemptsByIp, now, LOGIN_RATE_LIMIT_WINDOW_MS);
        }

        Deque<Long> attempts = failedAttemptsByIp.get(ip);
        if (attempts == null) return false;

        synchronized (attempts) {
            purgeOldAttempts(attempts, now, LOGIN_RATE_LIMIT_WINDOW_MS);
            return attempts.size() >= MAX_FAILED_LOGIN_ATTEMPTS;
        }
    }

    public static boolean isLoginRateLimited(String ip) {
        return isRateLimited(ip);
    }

    public static void recordLoginFailure(String ip) {
        if (ip == null || ip.isEmpty()) return;
        long now = System.currentTimeMillis();

        failedAttemptsByIp.compute(ip, (k, attempts) -> {
            if (attempts == null) {
                attempts = new ArrayDeque<>();
            }
            synchronized (attempts) {
                purgeOldAttempts(attempts, now, LOGIN_RATE_LIMIT_WINDOW_MS);
                attempts.addLast(now);
            }
            return attempts;
        });
    }

    public static void recordLoginSuccess(String ip) {
        if (ip != null) {
            failedAttemptsByIp.remove(ip);
        }
    }

    public static boolean isRegistrationRateLimited(String ip) {
        if (ip == null || ip.isEmpty()) return false;
        long now = System.currentTimeMillis();

        if (registrationAttemptsByIp.size() > 500) {
            cleanupExpiredLimits(registrationAttemptsByIp, now, REG_RATE_LIMIT_WINDOW_MS);
        }

        Deque<Long> attempts = registrationAttemptsByIp.get(ip);
        if (attempts == null) return false;

        synchronized (attempts) {
            purgeOldAttempts(attempts, now, REG_RATE_LIMIT_WINDOW_MS);
            return attempts.size() >= MAX_REGISTRATION_ATTEMPTS;
        }
    }

    public static void recordRegistrationAttempt(String ip) {
        if (ip == null || ip.isEmpty()) return;
        long now = System.currentTimeMillis();

        registrationAttemptsByIp.compute(ip, (k, attempts) -> {
            if (attempts == null) {
                attempts = new ArrayDeque<>();
            }
            synchronized (attempts) {
                purgeOldAttempts(attempts, now, REG_RATE_LIMIT_WINDOW_MS);
                attempts.addLast(now);
            }
            return attempts;
        });
    }

    private static void purgeOldAttempts(Deque<Long> attempts, long now, long windowMs) {
        while (!attempts.isEmpty() && (now - attempts.peekFirst() > windowMs)) {
            attempts.pollFirst();
        }
    }

    private static void cleanupExpiredLimits(ConcurrentHashMap<String, Deque<Long>> map, long now, long windowMs) {
        for (Map.Entry<String, Deque<Long>> entry : map.entrySet()) {
            Deque<Long> deque = entry.getValue();
            synchronized (deque) {
                purgeOldAttempts(deque, now, windowMs);
                if (deque.isEmpty()) {
                    map.remove(entry.getKey(), deque);
                }
            }
        }
    }
}
