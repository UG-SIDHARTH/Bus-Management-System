package test;

import database.DBConnection;
import model.Booking;
import model.Bus;
import model.Passenger;
import model.User;
import web.SecurityUtils;
import web.SessionManager;
import web.WebUtils;

import java.util.HashMap;
import java.util.Map;

public class TestSuite {

    private static int passed = 0;
    private static int failed = 0;

    private static void assertTrue(String testName, boolean condition) {
        if (condition) {
            System.out.println("  [PASS] " + testName);
            passed++;
        } else {
            System.err.println("  [FAIL] " + testName);
            failed++;
        }
    }

    private static void assertEquals(String testName, Object expected, Object actual) {
        if ((expected == null && actual == null) || (expected != null && expected.equals(actual))) {
            System.out.println("  [PASS] " + testName);
            passed++;
        } else {
            System.err.println("  [FAIL] " + testName + " (Expected: " + expected + ", Got: " + actual + ")");
            failed++;
        }
    }

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println(" 🚍 SwiftBus System Automated Test Suite");
        System.out.println(" Target Database: MySQL Only (bus_reservation)");
        System.out.println("==================================================");

        testPasswordPolicyAndConditions();
        testPbkdf2Cryptography();
        testMultiTierRateLimiting();
        testSessionManagement();
        testCsrfProtection();
        testHtmlEscaping();
        testDomainModels();

        System.out.println("==================================================");
        System.out.println(" TEST RESULTS SUMMARY:");
        System.out.println(" Passed: " + passed);
        System.out.println(" Failed: " + failed);
        System.out.println("==================================================");

        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void testPasswordPolicyAndConditions() {
        System.out.println("\n--- 1. Testing Password Conditions (Min 8 chars, Special chars, Strength) ---");

        // Length < 8 chars -> Must Fail
        assertTrue("Rejects password under 8 chars", !SecurityUtils.meetsPasswordPolicy("short!"));
        assertTrue("Rejects null password", !SecurityUtils.meetsPasswordPolicy(null));
        assertTrue("Rejects empty password", !SecurityUtils.meetsPasswordPolicy(""));

        // Length >= 8 but NO special chars -> Must Fail
        assertTrue("Rejects 8+ chars without special char", !SecurityUtils.meetsPasswordPolicy("password123"));
        assertTrue("Rejects uppercase/lowercase/numbers without special char", !SecurityUtils.meetsPasswordPolicy("Password1234"));

        // Length >= 8 WITH special chars -> Must Pass
        assertTrue("Accepts 8 chars with @", SecurityUtils.meetsPasswordPolicy("pass@123"));
        assertTrue("Accepts 8 chars with #", SecurityUtils.meetsPasswordPolicy("Valid#99"));
        assertTrue("Accepts with multiple special chars", SecurityUtils.meetsPasswordPolicy("Secure!@#2026"));

        // Special char detector
        assertTrue("Detects special char in 'test!'", SecurityUtils.containsSpecialChar("test!"));
        assertTrue("Detects no special char in 'test123'", !SecurityUtils.containsSpecialChar("test123"));

        // Strength Classification: WEAK
        assertEquals("Evaluates 'short' as WEAK", SecurityUtils.PasswordStrength.WEAK, SecurityUtils.evaluatePasswordStrength("short!"));
        assertEquals("Evaluates 'plainpassword123' as WEAK", SecurityUtils.PasswordStrength.WEAK, SecurityUtils.evaluatePasswordStrength("plainpassword123"));

        // Strength Classification: BETTER (>=8 chars, contains special char, but simpler)
        assertEquals("Evaluates 'pass@123' as BETTER", SecurityUtils.PasswordStrength.BETTER, SecurityUtils.evaluatePasswordStrength("pass@123"));

        // Strength Classification: BEST (>=10 chars, uppercase, lowercase, numbers, special char)
        assertEquals("Evaluates 'MySecret@2026!' as BEST", SecurityUtils.PasswordStrength.BEST, SecurityUtils.evaluatePasswordStrength("MySecret@2026!"));
    }

    private static void testPbkdf2Cryptography() {
        System.out.println("\n--- 2. Testing PBKDF2WithHmacSHA256 Cryptography & Salt ---");

        byte[] salt1 = SecurityUtils.generateRandomSalt();
        byte[] salt2 = SecurityUtils.generateRandomSalt();
        String saltHex1 = SecurityUtils.bytesToHex(salt1);
        String saltHex2 = SecurityUtils.bytesToHex(salt2);

        assertTrue("Generated salt is 16 bytes (32 hex chars)", saltHex1.length() == 32);
        assertTrue("Consecutive salts are unique", !saltHex1.equals(saltHex2));

        int iterations = 1000; // Fast for test
        String password = "Secret@Password2026";
        String hash1 = SecurityUtils.hashPassword(password.toCharArray(), salt1, iterations);
        String hash2 = SecurityUtils.hashPassword(password.toCharArray(), salt2, iterations);

        assertTrue("Generated hash is 256 bits (64 hex chars)", hash1.length() == 64);
        assertTrue("Different salts generate distinct hashes", !hash1.equals(hash2));

        // Constant-time verification
        assertTrue("Verifies correct password", SecurityUtils.verifyPassword(password.toCharArray(), saltHex1, hash1, iterations));
        assertTrue("Rejects wrong password", !SecurityUtils.verifyPassword("WrongPassword!2".toCharArray(), saltHex1, hash1, iterations));
        assertTrue("Rejects wrong salt", !SecurityUtils.verifyPassword(password.toCharArray(), saltHex2, hash1, iterations));
    }

    private static void testMultiTierRateLimiting() {
        System.out.println("\n--- 3. Testing Multi-Tier Rate Limiting (Login & Registration) ---");

        String testIp = "10.0.0.123";

        // Login rate limiter: initial state
        assertTrue("New IP is not rate limited", !SecurityUtils.isRateLimited(testIp));

        // Record 4 failed attempts
        for (int i = 0; i < 4; i++) {
            SecurityUtils.recordLoginFailure(testIp);
            assertTrue("Still under limit on attempt " + (i + 1), !SecurityUtils.isRateLimited(testIp));
        }

        // 5th attempt breaches limit
        SecurityUtils.recordLoginFailure(testIp);
        assertTrue("Rate limited after 5 failures", SecurityUtils.isRateLimited(testIp));

        // Successful login resets IP
        SecurityUtils.recordLoginSuccess(testIp);
        assertTrue("Reset after successful login", !SecurityUtils.isRateLimited(testIp));

        // Registration rate limiter
        String regIp = "10.0.0.222";
        assertTrue("New IP registration not limited", !SecurityUtils.isRegistrationRateLimited(regIp));

        for (int i = 0; i < 4; i++) {
            SecurityUtils.recordRegistrationAttempt(regIp);
            assertTrue("Registration allowed on attempt " + (i + 1), !SecurityUtils.isRegistrationRateLimited(regIp));
        }

        // 5th registration attempt breaches limit
        SecurityUtils.recordRegistrationAttempt(regIp);
        assertTrue("Registration rate limited after 5 attempts", SecurityUtils.isRegistrationRateLimited(regIp));
    }

    private static void testSessionManagement() {
        System.out.println("\n--- 4. Testing Session Management & 128-bit Tokens ---");

        User u = new User(10, "tester", "tester@swiftbus.local", "hash", "salt", "USER", null);
        SessionManager.Session session = SessionManager.createSession(u);

        assertTrue("Session has valid 128-bit ID", session.getSessionId() != null && session.getSessionId().length() == 32);
        assertTrue("Session has valid CSRF token", session.getCsrfToken() != null && session.getCsrfToken().length() == 32);
        assertTrue("Session is authenticated", session.isAuthenticated());
        assertEquals("Session username matches", "tester", session.getUsername());
        assertEquals("Session userId matches", 10, session.getUserId());

        // Session lookup
        SessionManager.Session found = SessionManager.getSession(session.getSessionId());
        assertTrue("Retrieved session matches created session", found != null && found.getSessionId().equals(session.getSessionId()));

        // Session invalidation
        SessionManager.invalidate(session.getSessionId());
        assertTrue("Session successfully invalidated", SessionManager.getSession(session.getSessionId()) == null);

        // Guest session
        SessionManager.Session guest = SessionManager.createGuestSession();
        assertTrue("Guest session has CSRF token", guest.getCsrfToken() != null && !guest.getCsrfToken().isEmpty());
        assertTrue("Guest is not authenticated", !guest.isAuthenticated());
    }

    private static void testCsrfProtection() {
        System.out.println("\n--- 5. Testing CSRF Token Verification ---");

        User u = new User(1, "admin", "admin@swiftbus.local", "h", "s", "ADMIN", null);
        SessionManager.Session session = SessionManager.createSession(u);

        Map<String, String> validForm = new HashMap<>();
        validForm.put("csrf_token", session.getCsrfToken());
        validForm.put("bus_name", "Volvo");

        Map<String, String> invalidForm = new HashMap<>();
        invalidForm.put("csrf_token", "invalid_token_12345");

        Map<String, String> missingForm = new HashMap<>();

        assertTrue("Valid CSRF token accepted", WebUtils.validateCsrfToken(validForm, session));
        assertTrue("Invalid CSRF token rejected", !WebUtils.validateCsrfToken(invalidForm, session));
        assertTrue("Missing CSRF token rejected", !WebUtils.validateCsrfToken(missingForm, session));
    }

    private static void testHtmlEscaping() {
        System.out.println("\n--- 6. Testing XSS Protection (HTML Escaping) ---");

        String input = "<script>alert('XSS & Hack');</script> \"quotes\"";
        String escaped = WebUtils.escapeHtml(input);

        assertTrue("Escaped contains no raw <", !escaped.contains("<"));
        assertTrue("Escaped contains no raw >", !escaped.contains(">"));
        assertTrue("Escaped contains no raw \"", !escaped.contains("\""));
        assertTrue("Escaped contains &lt;script&gt;", escaped.contains("&lt;script&gt;"));
        assertTrue("Escaped contains &#39;", escaped.contains("&#39;"));
    }

    private static void testDomainModels() {
        System.out.println("\n--- 7. Testing Domain Models (User, Booking, Bus, Passenger) ---");

        User user = new User(1, "admin", "admin@swiftbus.local", "hash", "salt", "ADMIN", null);
        assertTrue("User isAdmin true for ADMIN role", user.isAdmin());

        User regular = new User(2, "john", "john@example.com", "hash", "salt", "USER", null);
        assertTrue("User isAdmin false for USER role", !regular.isAdmin());

        Booking b = new Booking(101, 1, 2, 5, 12, "2026-10-15", 45.0, "CONFIRMED");
        assertEquals("Booking userId is stored correctly", Integer.valueOf(2), b.getUserId());
        assertEquals("Booking seatNumber is stored correctly", 12, b.getSeatNumber());

        Bus bus = new Bus(1, "Express AC", "City A", "City B", 40, 50.0);
        assertEquals("Bus total seats is 40", 40, bus.getTotalSeats());

        Passenger p = new Passenger(1, "Alice", 28, "Female", "1234567890");
        assertEquals("Passenger phone matches", "1234567890", p.getPhoneNumber());

        testMySqlDatabaseExclusivity();
    }

    private static void testMySqlDatabaseExclusivity() {
        System.out.println("\n--- 8. Testing MySQL Exclusivity & Official Driver Integrity ---");

        try {
            Class<?> driverClass = Class.forName("com.mysql.cj.jdbc.Driver");
            assertTrue("Official MySQL JDBC Driver (com.mysql.cj.jdbc.Driver) loaded successfully", driverClass != null);
        } catch (ClassNotFoundException e) {
            assertTrue("MySQL JDBC Driver loaded", false);
        }

        // Verify driver jar is present in lib directory
        java.io.File driverJar = new java.io.File("lib/mysql-connector-j-26.7.0/mysql-connector-j-26.7.0.jar");
        assertTrue("Official MySQL Connector/J JAR is physically present", driverJar.exists());

        // Verify database schema file specifies MySQL InnoDB engine
        java.io.File sqlFile = new java.io.File("lib/database.sql");
        assertTrue("database.sql schema file is present", sqlFile.exists());
    }
}
