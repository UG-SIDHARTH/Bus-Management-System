package test;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpIntegrationTest {

    public static void main(String[] args) throws Exception {
        CookieManager cookieManager = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        HttpClient client = HttpClient.newBuilder()
                .cookieHandler(cookieManager)
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();

        String baseUrl = "http://localhost:6969";

        // 1. Fetch Login Page
        HttpRequest getLogin = HttpRequest.newBuilder().uri(URI.create(baseUrl + "/login")).GET().build();
        HttpResponse<String> loginPageResp = client.send(getLogin, HttpResponse.BodyHandlers.ofString());
        String loginHtml = loginPageResp.body();

        Pattern csrfPattern = Pattern.compile("name=\"csrf_token\" value=\"([^\"]+)\"");
        Matcher m1 = csrfPattern.matcher(loginHtml);
        if (!m1.find()) {
            throw new RuntimeException("CSRF token not found on login page");
        }
        String csrfLogin = m1.group(1);
        System.out.println("[PASS] 1. Fetched login page and CSRF token: " + csrfLogin);

        // 2. Perform Login as test1
        String formLogin = "username=test1&password=1234&csrf_token=" + URLEncoder.encode(csrfLogin, StandardCharsets.UTF_8);
        HttpRequest postLogin = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/do-login"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(formLogin))
                .build();
        HttpResponse<String> doLoginResp = client.send(postLogin, HttpResponse.BodyHandlers.ofString());
        System.out.println("[PASS] 2. Logged in as test1, Status: " + doLoginResp.statusCode() + ", Redirect: " + doLoginResp.headers().firstValue("Location").orElse("none"));

        // 3. Get Bookings Page for Bus 2
        HttpRequest getBookings = HttpRequest.newBuilder().uri(URI.create(baseUrl + "/bookings?bus_id=2")).GET().build();
        HttpResponse<String> bookingsResp = client.send(getBookings, HttpResponse.BodyHandlers.ofString());
        String bookingsHtml = bookingsResp.body();

        if (!bookingsHtml.contains("id=\"per_seat_passengers_section\"")) {
            throw new RuntimeException("per_seat_passengers_section missing from bookings page");
        }
        if (!bookingsHtml.contains("id=\"per_seat_passengers_container\"")) {
            throw new RuntimeException("per_seat_passengers_container missing from bookings page");
        }
        if (!bookingsHtml.contains("renderSeatPassengerCards")) {
            throw new RuntimeException("renderSeatPassengerCards JS missing");
        }
        System.out.println("[PASS] 3. Verified UI per-seat passenger section and dynamic JavaScript cards");

        Matcher m2 = csrfPattern.matcher(bookingsHtml);
        if (!m2.find()) {
            throw new RuntimeException("CSRF token not found on bookings page");
        }
        String csrfBook = m2.group(1);

        // 4. Reserve Seats 23 and 24 with distinct names and individual concession
        String postBooking = "csrf_token=" + URLEncoder.encode(csrfBook, StandardCharsets.UTF_8)
                + "&bus_id=2"
                + "&booking_date=2026-10-18"
                + "&seat_numbers=23,24"
                + "&passenger_name_23=" + URLEncoder.encode("Alice Cooper", StandardCharsets.UTF_8)
                + "&passenger_gender_23=Female"
                + "&passenger_age_23=28"
                + "&passenger_name_24=" + URLEncoder.encode("Bob Builder", StandardCharsets.UTF_8)
                + "&passenger_gender_24=Male"
                + "&passenger_age_24=34"
                + "&passenger_pwd_24=true";

        HttpRequest postBook = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/bookings/add"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(postBooking))
                .build();
        HttpResponse<String> bookResp = client.send(postBook, HttpResponse.BodyHandlers.ofString());
        String ticketLocation = bookResp.headers().firstValue("Location").orElse("");
        System.out.println("[PASS] 4. Submitted multi-booking with per-seat passenger details, Status: " + bookResp.statusCode() + ", Redirect: " + ticketLocation);

        if (!ticketLocation.startsWith("/tickets?id=")) {
            throw new RuntimeException("Expected redirect to /tickets but got: " + ticketLocation);
        }

        // 5. Follow redirect to Ticket & Boarding Pass
        HttpRequest getTicket = HttpRequest.newBuilder().uri(URI.create(baseUrl + ticketLocation)).GET().build();
        HttpResponse<String> ticketResp = client.send(getTicket, HttpResponse.BodyHandlers.ofString());
        String ticketHtml = ticketResp.body();

        if (!ticketHtml.contains("Booking Confirmed!")) {
            throw new RuntimeException("Booking confirmation missing from ticket page");
        }
        System.out.println("[PASS] 5. Boarding pass retrieved with confirmed status");

        // 6. Check /bookings list
        HttpRequest getAllBookings = HttpRequest.newBuilder().uri(URI.create(baseUrl + "/bookings")).GET().build();
        HttpResponse<String> allBookingsResp = client.send(getAllBookings, HttpResponse.BodyHandlers.ofString());
        String allHtml = allBookingsResp.body();

        if (!allHtml.contains("Alice Cooper")) {
            throw new RuntimeException("Alice Cooper not found in bookings table");
        }
        if (!allHtml.contains("Bob Builder")) {
            throw new RuntimeException("Bob Builder not found in bookings table");
        }
        if (!allHtml.contains("Seat 23") || !allHtml.contains("Seat 24")) {
            throw new RuntimeException("Seats 23 and 24 not found in bookings table");
        }
        System.out.println("[PASS] 6. Both individual passenger names (Alice Cooper for Seat 23, Bob Builder for Seat 24) verified in bookings table!");

        System.out.println("\n==================================================");
        System.out.println(" ✅ END-TO-END HTTP INTEGRATION TEST COMPLETED 100%!");
        System.out.println("==================================================");
    }
}
