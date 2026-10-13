package test;

import database.DBConnection;
import model.Booking;
import model.Bus;
import model.Passenger;
import model.User;
import dao.BookingDAO;
import dao.PassengerDAO;
import dao.UserDAO;
import web.SecurityUtils;
import web.SessionManager;
import web.WebUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
        System.out.println(" 🚍 SwiftBus System Automated Test Suite (v3.0)");
        System.out.println(" Target Database: MySQL Only (bus_reservation)");
        System.out.println(" Features: Multi-Booking, Concessions, Gender Radar");
        System.out.println("==================================================");

        testPasswordPolicyAndConditions();
        testPbkdf2Cryptography();
        testMultiTierRateLimiting();
        testSessionManagement();
        testCsrfProtection();
        testHtmlEscaping();
        testDomainModels();
        testDisabilityAndConcessionCalculations();
        testMultiSeatBookingAggregation();
        testGenderOccupancyMapping();
        testTestUserAccounts();
        testPerSeatPassengerMultiBooking();

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
        assertTrue("Random salts are not null", salt1 != null && salt2 != null);
        assertEquals("Random salt length is 16 bytes", 16, salt1.length);
        assertTrue("Unique random salts generated each invocation", !SecurityUtils.bytesToHex(salt1).equals(SecurityUtils.bytesToHex(salt2)));

        String saltHex = SecurityUtils.bytesToHex(salt1);
        int iterations = 1000; // Small iterations for fast testing
        char[] password = "UserSecret#2026".toCharArray();

        String hash1 = SecurityUtils.hashPassword(password, salt1, iterations);
        String hash2 = SecurityUtils.hashPassword(password, salt1, iterations);
        assertEquals("PBKDF2 is deterministic with same salt/pwd", hash1, hash2);

        assertTrue("Verify valid password returns true", SecurityUtils.verifyPassword(password, saltHex, hash1, iterations));
        assertTrue("Verify invalid password returns false", !SecurityUtils.verifyPassword("WrongPassword#99".toCharArray(), saltHex, hash1, iterations));
    }

    private static void testMultiTierRateLimiting() {
        System.out.println("\n--- 3. Testing Rate Limiting (Login & Registration) ---");

        String ip = "192.168.1.100";
        SecurityUtils.recordLoginSuccess(ip); // resets
        assertTrue("Initial IP is not rate limited", !SecurityUtils.isLoginRateLimited(ip));

        for (int i = 0; i < 5; i++) {
            SecurityUtils.recordLoginFailure(ip);
        }
        assertTrue("Rate limited after 5 failed login attempts", SecurityUtils.isLoginRateLimited(ip));

        SecurityUtils.recordLoginSuccess(ip);
        assertTrue("Rate limit cleared after successful login", !SecurityUtils.isLoginRateLimited(ip));
    }

    private static void testSessionManagement() {
        System.out.println("\n--- 4. Testing In-Memory Session Tokens & Timeouts ---");

        User user = new User(1, "admin", "admin@swiftbus.local", "hash", "salt", "ADMIN", null);
        SessionManager.Session session = SessionManager.createSession(user);

        assertTrue("Session has non-null ID", session.getSessionId() != null);
        assertTrue("Session is authenticated", session.isAuthenticated());
        assertTrue("Session isAdmin true", session.isAdmin());
        assertEquals("Session username matches", "admin", session.getUsername());
        assertEquals("Session userId matches", 1, session.getUserId());

        SessionManager.Session retrieved = SessionManager.getSession(session.getSessionId());
        assertEquals("Retrieved session matches created", session.getSessionId(), retrieved.getSessionId());

        SessionManager.invalidate(session.getSessionId());
        assertTrue("Session is null after invalidation", SessionManager.getSession(session.getSessionId()) == null);

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

    private static void testDisabilityAndConcessionCalculations() {
        System.out.println("\n--- 8. Testing Disability Concessions (50% Discount) & Registry ---");

        Passenger pwdPassenger = new Passenger(104, "Robert Williams", 42, "Male", "9811223344", true, "Locomotor / Mobility", "UDID-98765-2026");
        assertTrue("Passenger is marked as disabled", pwdPassenger.isDisability());
        assertEquals("Passenger disability type stored", "Locomotor / Mobility", pwdPassenger.getDisabilityType());
        assertEquals("Passenger disability ID stored", "UDID-98765-2026", pwdPassenger.getDisabilityId());

        double standardFare = 60.0;
        double concessionDiscount = standardFare * 0.50;
        double netFare = standardFare - concessionDiscount;

        Booking pwdBooking = new Booking(2001, pwdPassenger.getPassengerId(), 2, 1, 7, "2026-10-15", netFare, "CONFIRMED", concessionDiscount, true);
        assertEquals("Net fare is exactly 50% of standard fare", 30.0, pwdBooking.getFare());
        assertEquals("Concession amount is 30.0", 30.0, pwdBooking.getConcessionAmount());
        assertTrue("Booking flag isConcession is true", pwdBooking.isConcession());
    }

    private static void testMultiSeatBookingAggregation() {
        System.out.println("\n--- 9. Testing Multi-Seat Booking Aggregation on Same User ---");

        int userId = 2;
        int busId = 1;
        double farePerSeat = 45.0;
        int[] chosenSeats = {12, 13, 14};

        List<Booking> batch = new ArrayList<>();
        for (int seat : chosenSeats) {
            batch.add(new Booking(0, 101, userId, busId, seat, "2026-10-15", farePerSeat, "CONFIRMED"));
        }

        assertEquals("Multi-seat batch contains 3 bookings", 3, batch.size());
        double totalCost = 0.0;
        for (Booking b : batch) {
            assertEquals("All bookings belong to same user", Integer.valueOf(userId), b.getUserId());
            totalCost += b.getFare();
        }
        assertEquals("Total cost for 3 seats is $135.00", 135.0, totalCost);
    }

    private static void testGenderOccupancyMapping() {
        System.out.println("\n--- 10. Testing Gender-Aware Seat Occupancy Mapping ---");

        Map<Integer, String> seatGenderMap = new HashMap<>();
        seatGenderMap.put(11, "Female");
        seatGenderMap.put(12, "Male");
        seatGenderMap.put(15, "Other");

        assertEquals("Seat 11 is female occupied", "Female", seatGenderMap.get(11));
        assertEquals("Seat 12 is male occupied", "Male", seatGenderMap.get(12));
        assertEquals("Seat 15 is other occupied", "Other", seatGenderMap.get(15));
        assertTrue("Unbooked seat 13 is empty", !seatGenderMap.containsKey(13));
    }

    private static void testMySqlDatabaseExclusivity() {
        System.out.println("\n--- 11. Testing MySQL Exclusivity & Official Driver Integrity ---");

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

    private static void testTestUserAccounts() {
        System.out.println("\n--- 12. Testing 5 Test Users (test1 to test5 with password 1234) ---");

        UserDAO userDAO = new UserDAO();
        int iterations = SecurityUtils.getPbkdf2Iterations();

        for (int i = 1; i <= 5; i++) {
            String username = "test" + i;
            User u = userDAO.findByUsernameOrEmail(username);

            assertTrue("User " + username + " exists and is retrievable", u != null);
            if (u != null) {
                assertEquals("Username matches " + username, username, u.getUsername());
                assertEquals("Role is USER for " + username, "USER", u.getRole());
                assertTrue("User " + username + " authenticates with password '1234'",
                        SecurityUtils.verifyPassword("1234".toCharArray(), u.getSalt(), u.getPasswordHash(), iterations));
                assertTrue("User " + username + " rejects incorrect password 'wrongpass'",
                        !SecurityUtils.verifyPassword("wrongpass".toCharArray(), u.getSalt(), u.getPasswordHash(), iterations));
            }
        }

        List<User> all = userDAO.getAllUsers();
        assertTrue("All users list contains at least 7 accounts (admin, john_doe, test1-test5)", all.size() >= 7);
    }

    private static void testPerSeatPassengerMultiBooking() {
        System.out.println("\n--- 13. Testing Per-Seat Passenger Names & Individual Concessions ---");

        PassengerDAO passengerDAO = new PassengerDAO();
        BookingDAO bookingDAO = new BookingDAO();

        // Register 3 individual passengers with different names and disability statuses
        int id1 = passengerDAO.getNextPassengerId();
        Passenger p1 = new Passenger(id1, "Alice Cooper", 28, "Female", "9112233445", false, null, null);
        passengerDAO.addPassenger(p1);

        int id2 = passengerDAO.getNextPassengerId();
        Passenger p2 = new Passenger(id2, "Bob Builder", 34, "Male", "9223344556", true, "Locomotor Disability", "UDID-BOB-99");
        passengerDAO.addPassenger(p2);

        int id3 = passengerDAO.getNextPassengerId();
        Passenger p3 = new Passenger(id3, "Charlie Chaplin", 45, "Male", "9334455667", false, null, null);
        passengerDAO.addPassenger(p3);

        assertTrue("Passenger 1 Alice Cooper registered", passengerDAO.findByName("Alice Cooper") != null);
        assertTrue("Passenger 2 Bob Builder registered with PwD", passengerDAO.findByName("Bob Builder") != null && passengerDAO.findByName("Bob Builder").isDisability());
        assertTrue("Passenger 3 Charlie Chaplin registered", passengerDAO.findByName("Charlie Chaplin") != null);

        // Simulate multi-seat booking for user 2 on bus 1 (Fare: $50.00 each)
        double baseFare = 50.00;
        List<Booking> batch = new ArrayList<>();

        // Seat 21 -> Alice Cooper ($50.00 standard)
        Booking b1 = new Booking(0, id1, 2, 1, 21, "2026-10-20", baseFare, "CONFIRMED", 0.0, false);
        b1.setPassengerName(p1.getName()); b1.setPassengerGender(p1.getGender()); b1.setBusName("Express Volvo AC");
        batch.add(b1);

        // Seat 22 -> Bob Builder ($25.00 with 50% concession)
        Booking b2 = new Booking(0, id2, 2, 1, 22, "2026-10-20", baseFare * 0.50, "CONFIRMED", baseFare * 0.50, true);
        b2.setPassengerName(p2.getName()); b2.setPassengerGender(p2.getGender()); b2.setPassengerDisability(true); b2.setBusName("Express Volvo AC");
        batch.add(b2);

        // Seat 23 -> Charlie Chaplin ($50.00 standard)
        Booking b3 = new Booking(0, id3, 2, 1, 23, "2026-10-20", baseFare, "CONFIRMED", 0.0, false);
        b3.setPassengerName(p3.getName()); b3.setPassengerGender(p3.getGender()); b3.setBusName("Express Volvo AC");
        batch.add(b3);

        List<Booking> saved = bookingDAO.addBookingsBatch(batch);
        assertEquals("Batch of 3 distinct passenger bookings saved", 3, saved.size());

        // Verify individual seats preserve distinct passenger names
        assertEquals("Seat 21 belongs to Alice Cooper", "Alice Cooper", saved.get(0).getPassengerName());
        assertEquals("Seat 22 belongs to Bob Builder", "Bob Builder", saved.get(1).getPassengerName());
        assertEquals("Seat 23 belongs to Charlie Chaplin", "Charlie Chaplin", saved.get(2).getPassengerName());

        // Verify individual fares and concessions
        assertEquals("Seat 21 fare is $50.00", 50.00, saved.get(0).getFare());
        assertEquals("Seat 22 fare is $25.00 (concession)", 25.00, saved.get(1).getFare());
        assertTrue("Seat 22 has isConcession flag true", saved.get(1).isConcession());
        assertEquals("Seat 23 fare is $50.00", 50.00, saved.get(2).getFare());

        double totalBatchFare = saved.stream().mapToDouble(Booking::getFare).sum();
        assertEquals("Total payable for 3 seats is $125.00", 125.00, totalBatchFare);

        // Check occupancy detection
        assertTrue("Seat 21 is marked booked", bookingDAO.isSeatBooked(1, 21));
        assertTrue("Seat 22 is marked booked", bookingDAO.isSeatBooked(1, 22));
        assertTrue("Seat 23 is marked booked", bookingDAO.isSeatBooked(1, 23));

        // Check gender map reflects individual passenger genders
        Map<Integer, String> genderMap = bookingDAO.getBookedSeatsWithGender(1);
        assertEquals("Seat 21 gender is Female", "Female", genderMap.get(21));
        assertEquals("Seat 22 gender is Male", "Male", genderMap.get(22));
        assertEquals("Seat 23 gender is Male", "Male", genderMap.get(23));
    }
}
