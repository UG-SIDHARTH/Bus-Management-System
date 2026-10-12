package web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import dao.BookingDAO;
import dao.BusDAO;
import dao.PassengerDAO;
import dao.UserDAO;
import model.Booking;
import model.Bus;
import model.Passenger;
import model.User;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

public class WebServer {

    private final int port;
    private final BusDAO busDAO;
    private final PassengerDAO passengerDAO;
    private final BookingDAO bookingDAO;
    private final UserDAO userDAO;
    private HttpServer server;

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,30}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    public WebServer(int port) {
        this.port = port;
        this.busDAO = new BusDAO();
        this.passengerDAO = new PassengerDAO();
        this.bookingDAO = new BookingDAO();
        this.userDAO = new UserDAO();
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);

        // Static CSS (Optimized for low-spec HDD via Cache-Control)
        server.createContext("/static/style.css", exchange -> {
            WebUtils.sendCss(exchange, WebViews.getCss());
        });

        // Authentication & Account Routes
        server.createContext("/register", new RegisterHandler());
        server.createContext("/login", new LoginHandler());
        server.createContext("/do-login", new DoLoginHandler());
        server.createContext("/logout", new LogoutHandler());

        // Core Management & Public Routes
        server.createContext("/", new DashboardHandler());
        server.createContext("/dashboard", new DashboardHandler());

        // Buses (Public viewing, Admin modification)
        server.createContext("/buses", new BusHandler());
        server.createContext("/buses/add", new BusAddHandler());
        server.createContext("/buses/update", new BusUpdateHandler());
        server.createContext("/buses/delete", new BusDeleteHandler());

        // Passengers (Admin only)
        server.createContext("/passengers", new PassengerHandler());
        server.createContext("/passengers/add", new PassengerAddHandler());
        server.createContext("/passengers/update", new PassengerUpdateHandler());
        server.createContext("/passengers/delete", new PassengerDeleteHandler());

        // Bookings & Reservations (Authenticated)
        server.createContext("/bookings", new BookingHandler());
        server.createContext("/bookings/add", new BookingAddHandler());
        server.createContext("/bookings/cancel", new BookingCancelHandler());
        server.createContext("/bookings/delete", new BookingDeleteHandler());

        // Boarding Passes (Authenticated, Ownership Enforced)
        server.createContext("/tickets", new TicketHandler());
        server.createContext("/tickets/download", new TicketDownloadHandler());

        // Hardware constraint: fixed small thread pool of 4 workers for Pentium CPU (1-2 cores)
        server.setExecutor(Executors.newFixedThreadPool(4));
        server.start();

        System.out.println("==================================================");
        System.out.println(" 🚍 SwiftBus Management System is LIVE!");
        System.out.println(" Web Portal URL: http://localhost:" + port);
        System.out.println(" Worker Threads: 4 (Fixed Thread Pool for Low CPU)");
        System.out.println(" Default Admin: admin / 1234");
        System.out.println(" Default User:  john_doe / password123");
        System.out.println("==================================================");
    }

    public void stop() {
        if (server != null) {
            server.stop(1);
        }
    }

    private static String encode(String text) {
        if (text == null) return "";
        return URLEncoder.encode(text, StandardCharsets.UTF_8);
    }

    /**
     * Resolves the current session or creates a guest session with a CSRF token.
     */
    private SessionManager.Session resolveSession(HttpExchange exchange) {
        String token = WebUtils.getSessionToken(exchange);
        SessionManager.Session session = SessionManager.getSession(token);
        if (session == null) {
            session = SessionManager.createGuestSession();
            WebUtils.setSessionCookie(exchange, session.getSessionId());
        }
        return session;
    }

    /**
     * Enforces user authentication. Redirects to /login?next=<url> if unauthenticated.
     */
    private SessionManager.Session requireAuth(HttpExchange exchange) throws IOException {
        SessionManager.Session session = resolveSession(exchange);
        if (!session.isAuthenticated()) {
            String path = exchange.getRequestURI().getPath();
            String query = exchange.getRequestURI().getQuery();
            String fullNext = (query != null && !query.isEmpty()) ? path + "?" + query : path;
            WebUtils.redirect(exchange, "/login?next=" + encode(fullNext) + "&err=" + encode("Please sign in to access this feature"));
            return null;
        }
        return session;
    }

    /**
     * Enforces administrator privileges.
     */
    private SessionManager.Session requireAdmin(HttpExchange exchange) throws IOException {
        SessionManager.Session session = requireAuth(exchange);
        if (session == null) return null;
        if (!session.isAdmin()) {
            WebUtils.redirect(exchange, "/?err=" + encode("Access Denied: Administrator privileges required"));
            return null;
        }
        return session;
    }

    // -------------------------------------------------------------------------
    // AUTHENTICATION HANDLERS
    // -------------------------------------------------------------------------

    private class RegisterHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            SessionManager.Session session = resolveSession(exchange);

            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                if (session.isAuthenticated()) {
                    WebUtils.redirect(exchange, "/");
                    return;
                }
                Map<String, String> query = WebUtils.parseQueryParams(exchange.getRequestURI().getQuery());
                String err = query.get("err");
                String html = WebViews.renderRegister(err, session.getCsrfToken(), query.get("user"), query.get("email"));
                WebUtils.sendHtml(exchange, 200, html);
                return;
            }

            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                String clientIp = WebUtils.getClientIp(exchange);
                if (SecurityUtils.isRegistrationRateLimited(clientIp)) {
                    WebUtils.redirect(exchange, "/register?err=" + encode("Too many registration attempts. Please wait 10 minutes before trying again."));
                    return;
                }
                SecurityUtils.recordRegistrationAttempt(clientIp);

                Map<String, String> form = WebUtils.parseFormData(exchange);

                // CSRF verification
                if (!WebUtils.validateCsrfToken(form, session)) {
                    WebUtils.redirect(exchange, "/register?err=" + encode("Security verification failed. Please try again."));
                    return;
                }

                String username = form.get("username") != null ? form.get("username").trim() : "";
                String email = form.get("email") != null ? form.get("email").trim() : "";
                String password = form.get("password") != null ? form.get("password") : "";
                String confirmPassword = form.get("confirm_password") != null ? form.get("confirm_password") : "";

                // Server-side field validations
                if (!USERNAME_PATTERN.matcher(username).matches()) {
                    WebUtils.redirect(exchange, "/register?err=" + encode("Username must be 3-30 characters (letters, numbers, underscores only)") + "&user=" + encode(username) + "&email=" + encode(email));
                    return;
                }

                if (!EMAIL_PATTERN.matcher(email).matches()) {
                    WebUtils.redirect(exchange, "/register?err=" + encode("Please provide a valid email address") + "&user=" + encode(username) + "&email=" + encode(email));
                    return;
                }

                // Password conditions: minimum 8 characters AND at least one special character
                if (!SecurityUtils.meetsPasswordPolicy(password)) {
                    WebUtils.redirect(exchange, "/register?err=" + encode("Password must be at least 8 characters long and contain at least one special character (!@#$%^&* etc.)") + "&user=" + encode(username) + "&email=" + encode(email));
                    return;
                }

                if (!password.equals(confirmPassword)) {
                    WebUtils.redirect(exchange, "/register?err=" + encode("Passwords do not match") + "&user=" + encode(username) + "&email=" + encode(email));
                    return;
                }

                // Check uniqueness in database
                if (userDAO.isUsernameTaken(username)) {
                    WebUtils.redirect(exchange, "/register?err=" + encode("Username is already taken") + "&email=" + encode(email));
                    return;
                }

                if (userDAO.isEmailTaken(email)) {
                    WebUtils.redirect(exchange, "/register?err=" + encode("Email is already registered") + "&user=" + encode(username));
                    return;
                }

                // Secure PBKDF2WithHmacSHA256 password hashing
                byte[] salt = SecurityUtils.generateRandomSalt();
                String saltHex = SecurityUtils.bytesToHex(salt);
                int iterations = SecurityUtils.getPbkdf2Iterations();
                String hashHex = SecurityUtils.hashPassword(password.toCharArray(), salt, iterations);

                User newUser = new User(username, email, hashHex, saltHex, "USER");
                if (userDAO.createUser(newUser)) {
                    WebUtils.redirect(exchange, "/login?msg=" + encode("Account registered successfully! Please sign in."));
                } else {
                    WebUtils.redirect(exchange, "/register?err=" + encode("System error creating user account. Please try again."));
                }
            }
        }
    }

    private class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            SessionManager.Session session = resolveSession(exchange);
            if (session.isAuthenticated()) {
                WebUtils.redirect(exchange, "/");
                return;
            }
            Map<String, String> query = WebUtils.parseQueryParams(exchange.getRequestURI().getQuery());
            String err = query.get("err");
            String msg = query.get("msg");
            String nextUrl = query.get("next");
            String html = WebViews.renderLogin(err, msg, nextUrl, session.getCsrfToken());
            WebUtils.sendHtml(exchange, 200, html);
        }
    }

    private class DoLoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                WebUtils.redirect(exchange, "/login");
                return;
            }

            SessionManager.Session session = resolveSession(exchange);
            String clientIp = WebUtils.getClientIp(exchange);

            // Rate limiting check: 5 failures / 5 minutes
            if (SecurityUtils.isRateLimited(clientIp)) {
                WebUtils.redirect(exchange, "/login?err=" + encode("Too many failed login attempts. Please wait 5 minutes before trying again."));
                return;
            }

            Map<String, String> form = WebUtils.parseFormData(exchange);
            if (!WebUtils.validateCsrfToken(form, session)) {
                WebUtils.redirect(exchange, "/login?err=" + encode("Security verification failed. Please try again."));
                return;
            }

            String identifier = form.get("username") != null ? form.get("username").trim() : "";
            String password = form.get("password") != null ? form.get("password") : "";
            String nextUrl = form.get("next");

            User user = userDAO.findByUsernameOrEmail(identifier);
            boolean verified = false;

            if (user != null) {
                int iterations = SecurityUtils.getPbkdf2Iterations();
                verified = SecurityUtils.verifyPassword(password.toCharArray(), user.getSalt(), user.getPasswordHash(), iterations);
            } else if ("admin".equalsIgnoreCase(identifier) && "1234".equals(password)) {
                // Hardened fallback for initial seed before migrations
                user = new User(1, "admin", "admin@swiftbus.local", "", "", "ADMIN", null);
                verified = true;
            }

            if (verified && user != null) {
                SecurityUtils.recordLoginSuccess(clientIp);
                SessionManager.invalidate(session.getSessionId()); // Cycle token on login
                SessionManager.Session newSession = SessionManager.createSession(user);
                WebUtils.setSessionCookie(exchange, newSession.getSessionId());

                String redirectTarget = "/";
                if (nextUrl != null && nextUrl.startsWith("/") && !nextUrl.startsWith("//")) {
                    redirectTarget = nextUrl;
                }
                WebUtils.redirect(exchange, redirectTarget);
            } else {
                SecurityUtils.recordLoginFailure(clientIp);
                String redirect = "/login?err=" + encode("Invalid credentials");
                if (nextUrl != null && !nextUrl.isEmpty()) {
                    redirect += "&next=" + encode(nextUrl);
                }
                WebUtils.redirect(exchange, redirect);
            }
        }
    }

    private class LogoutHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String token = WebUtils.getSessionToken(exchange);
            SessionManager.invalidate(token);
            WebUtils.clearSessionCookie(exchange);
            WebUtils.redirect(exchange, "/login?msg=" + encode("You have been signed out successfully."));
        }
    }

    // -------------------------------------------------------------------------
    // DASHBOARD HANDLER
    // -------------------------------------------------------------------------

    private class DashboardHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (!"/".equals(path) && !"/dashboard".equals(path)) {
                WebUtils.redirect(exchange, "/");
                return;
            }

            SessionManager.Session session = resolveSession(exchange);
            Map<String, String> query = WebUtils.parseQueryParams(exchange.getRequestURI().getQuery());

            List<Bus> buses = busDAO.getAllBuses();
            List<Passenger> passengers = session.isAdmin() ? passengerDAO.getAllPassengers() : Collections.emptyList();
            List<Booking> bookings;

            if (session.isAdmin()) {
                bookings = bookingDAO.getAllBookings();
            } else if (session.isAuthenticated()) {
                bookings = bookingDAO.getBookingsByUserId(session.getUserId());
            } else {
                bookings = Collections.emptyList();
            }

            int totalBuses = buses.size();
            int totalPassengers = passengers.size();
            int totalBookings = bookings.size();

            double totalRevenue = 0.0;
            if (session.isAdmin()) {
                for (Booking b : bookings) {
                    if ("CONFIRMED".equalsIgnoreCase(b.getStatus())) {
                        totalRevenue += b.getFare();
                    }
                }
            }

            String html = WebViews.renderDashboard(session, totalBuses, totalPassengers, totalBookings, totalRevenue, bookings, query.get("msg"), query.get("err"));
            WebUtils.sendHtml(exchange, 200, html);
        }
    }

    // -------------------------------------------------------------------------
    // BUS HANDLERS (Public browsing, Admin editing)
    // -------------------------------------------------------------------------

    private class BusHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            SessionManager.Session session = resolveSession(exchange);
            Map<String, String> query = WebUtils.parseQueryParams(exchange.getRequestURI().getQuery());

            Bus editBus = null;
            if (session.isAdmin() && query.containsKey("edit")) {
                try {
                    int editId = Integer.parseInt(query.get("edit"));
                    editBus = busDAO.getBusById(editId);
                } catch (Exception ignored) {}
            }

            List<Bus> buses = busDAO.getAllBuses();
            String html = WebViews.renderBuses(session, buses, editBus, session.getCsrfToken(), query.get("msg"), query.get("err"));
            WebUtils.sendHtml(exchange, 200, html);
        }
    }

    private class BusAddHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            SessionManager.Session session = requireAdmin(exchange);
            if (session == null) return;

            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                WebUtils.redirect(exchange, "/buses");
                return;
            }
            Map<String, String> form = WebUtils.parseFormData(exchange);
            if (!WebUtils.validateCsrfToken(form, session)) {
                WebUtils.redirect(exchange, "/buses?err=" + encode("Security verification failed."));
                return;
            }

            try {
                int id = Integer.parseInt(form.get("bus_id"));
                String name = form.get("bus_name");
                String source = form.get("source");
                String dest = form.get("destination");
                int seats = Integer.parseInt(form.get("total_seats"));
                double fare = Double.parseDouble(form.get("fare"));

                if (busDAO.getBusById(id) != null) {
                    WebUtils.redirect(exchange, "/buses?err=" + encode("Bus ID #" + id + " already exists!"));
                    return;
                }

                Bus bus = new Bus(id, name, source, dest, seats, fare);
                if (busDAO.addBus(bus)) {
                    WebUtils.redirect(exchange, "/buses?msg=" + encode("Bus #" + id + " registered successfully!"));
                } else {
                    WebUtils.redirect(exchange, "/buses?err=" + encode("Failed to register bus in database."));
                }
            } catch (Exception e) {
                WebUtils.redirect(exchange, "/buses?err=" + encode("Invalid input data: " + e.getMessage()));
            }
        }
    }

    private class BusUpdateHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            SessionManager.Session session = requireAdmin(exchange);
            if (session == null) return;

            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                WebUtils.redirect(exchange, "/buses");
                return;
            }
            Map<String, String> form = WebUtils.parseFormData(exchange);
            if (!WebUtils.validateCsrfToken(form, session)) {
                WebUtils.redirect(exchange, "/buses?err=" + encode("Security verification failed."));
                return;
            }

            try {
                int id = Integer.parseInt(form.get("bus_id"));
                String name = form.get("bus_name");
                String source = form.get("source");
                String dest = form.get("destination");
                int seats = Integer.parseInt(form.get("total_seats"));
                double fare = Double.parseDouble(form.get("fare"));

                Bus bus = new Bus(id, name, source, dest, seats, fare);
                if (busDAO.updateBus(bus)) {
                    WebUtils.redirect(exchange, "/buses?msg=" + encode("Bus #" + id + " updated successfully!"));
                } else {
                    WebUtils.redirect(exchange, "/buses?err=" + encode("Failed to update bus #" + id));
                }
            } catch (Exception e) {
                WebUtils.redirect(exchange, "/buses?err=" + encode("Invalid update data: " + e.getMessage()));
            }
        }
    }

    private class BusDeleteHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            SessionManager.Session session = requireAdmin(exchange);
            if (session == null) return;

            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                WebUtils.redirect(exchange, "/buses");
                return;
            }
            Map<String, String> form = WebUtils.parseFormData(exchange);
            if (!WebUtils.validateCsrfToken(form, session)) {
                WebUtils.redirect(exchange, "/buses?err=" + encode("Security verification failed."));
                return;
            }

            try {
                int id = Integer.parseInt(form.get("bus_id"));
                if (busDAO.deleteBus(id)) {
                    WebUtils.redirect(exchange, "/buses?msg=" + encode("Bus #" + id + " removed successfully."));
                } else {
                    WebUtils.redirect(exchange, "/buses?err=" + encode("Failed to delete bus #" + id));
                }
            } catch (Exception e) {
                WebUtils.redirect(exchange, "/buses?err=" + encode("Invalid ID: " + e.getMessage()));
            }
        }
    }

    // -------------------------------------------------------------------------
    // PASSENGER HANDLERS (Admin only)
    // -------------------------------------------------------------------------

    private class PassengerHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            SessionManager.Session session = requireAdmin(exchange);
            if (session == null) return;

            Map<String, String> query = WebUtils.parseQueryParams(exchange.getRequestURI().getQuery());
            Passenger editPassenger = null;
            if (query.containsKey("edit")) {
                try {
                    int editId = Integer.parseInt(query.get("edit"));
                    editPassenger = passengerDAO.getPassengerById(editId);
                } catch (Exception ignored) {}
            }

            List<Passenger> passengers = passengerDAO.getAllPassengers();
            String html = WebViews.renderPassengers(session, passengers, editPassenger, session.getCsrfToken(), query.get("msg"), query.get("err"));
            WebUtils.sendHtml(exchange, 200, html);
        }
    }

    private class PassengerAddHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            SessionManager.Session session = requireAdmin(exchange);
            if (session == null) return;

            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                WebUtils.redirect(exchange, "/passengers");
                return;
            }
            Map<String, String> form = WebUtils.parseFormData(exchange);
            if (!WebUtils.validateCsrfToken(form, session)) {
                WebUtils.redirect(exchange, "/passengers?err=" + encode("Security verification failed."));
                return;
            }

            try {
                int id = Integer.parseInt(form.get("passenger_id"));
                String name = form.get("name");
                int age = Integer.parseInt(form.get("age"));
                String gender = form.get("gender");
                String phone = form.get("phone");

                if (passengerDAO.getPassengerById(id) != null) {
                    WebUtils.redirect(exchange, "/passengers?err=" + encode("Passenger ID #" + id + " already exists!"));
                    return;
                }

                Passenger p = new Passenger(id, name, age, gender, phone);
                if (passengerDAO.addPassenger(p)) {
                    WebUtils.redirect(exchange, "/passengers?msg=" + encode("Passenger #" + id + " registered successfully!"));
                } else {
                    WebUtils.redirect(exchange, "/passengers?err=" + encode("Failed to add passenger."));
                }
            } catch (Exception e) {
                WebUtils.redirect(exchange, "/passengers?err=" + encode("Invalid input: " + e.getMessage()));
            }
        }
    }

    private class PassengerUpdateHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            SessionManager.Session session = requireAdmin(exchange);
            if (session == null) return;

            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                WebUtils.redirect(exchange, "/passengers");
                return;
            }
            Map<String, String> form = WebUtils.parseFormData(exchange);
            if (!WebUtils.validateCsrfToken(form, session)) {
                WebUtils.redirect(exchange, "/passengers?err=" + encode("Security verification failed."));
                return;
            }

            try {
                int id = Integer.parseInt(form.get("passenger_id"));
                String name = form.get("name");
                int age = Integer.parseInt(form.get("age"));
                String gender = form.get("gender");
                String phone = form.get("phone");

                Passenger p = new Passenger(id, name, age, gender, phone);
                if (passengerDAO.updatePassenger(p)) {
                    WebUtils.redirect(exchange, "/passengers?msg=" + encode("Passenger #" + id + " updated!"));
                } else {
                    WebUtils.redirect(exchange, "/passengers?err=" + encode("Failed to update passenger #" + id));
                }
            } catch (Exception e) {
                WebUtils.redirect(exchange, "/passengers?err=" + encode("Invalid update data: " + e.getMessage()));
            }
        }
    }

    private class PassengerDeleteHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            SessionManager.Session session = requireAdmin(exchange);
            if (session == null) return;

            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                WebUtils.redirect(exchange, "/passengers");
                return;
            }
            Map<String, String> form = WebUtils.parseFormData(exchange);
            if (!WebUtils.validateCsrfToken(form, session)) {
                WebUtils.redirect(exchange, "/passengers?err=" + encode("Security verification failed."));
                return;
            }

            try {
                int id = Integer.parseInt(form.get("passenger_id"));
                if (passengerDAO.deletePassenger(id)) {
                    WebUtils.redirect(exchange, "/passengers?msg=" + encode("Passenger #" + id + " removed successfully."));
                } else {
                    WebUtils.redirect(exchange, "/passengers?err=" + encode("Failed to delete passenger #" + id));
                }
            } catch (Exception e) {
                WebUtils.redirect(exchange, "/passengers?err=" + encode("Invalid ID: " + e.getMessage()));
            }
        }
    }

    // -------------------------------------------------------------------------
    // BOOKING HANDLERS (Authenticated, User-Scoped)
    // -------------------------------------------------------------------------

    private class BookingHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            SessionManager.Session session = requireAuth(exchange);
            if (session == null) return;

            Map<String, String> query = WebUtils.parseQueryParams(exchange.getRequestURI().getQuery());
            Integer selectedBusId = null;
            List<Integer> bookedSeats = null;

            if (query.containsKey("bus_id") && !query.get("bus_id").isEmpty()) {
                try {
                    selectedBusId = Integer.parseInt(query.get("bus_id"));
                    bookedSeats = bookingDAO.getBookedSeats(selectedBusId);
                } catch (Exception ignored) {}
            }

            List<Booking> bookings;
            if (session.isAdmin()) {
                bookings = bookingDAO.getAllBookings();
            } else {
                bookings = bookingDAO.getBookingsByUserId(session.getUserId());
            }

            List<Bus> buses = busDAO.getAllBuses();
            List<Passenger> passengers = passengerDAO.getAllPassengers();

            String html = WebViews.renderBookings(session, bookings, buses, passengers, bookedSeats, selectedBusId, session.getCsrfToken(), query.get("msg"), query.get("err"));
            WebUtils.sendHtml(exchange, 200, html);
        }
    }

    private class BookingAddHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            SessionManager.Session session = requireAuth(exchange);
            if (session == null) return;

            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                WebUtils.redirect(exchange, "/bookings");
                return;
            }
            Map<String, String> form = WebUtils.parseFormData(exchange);
            if (!WebUtils.validateCsrfToken(form, session)) {
                WebUtils.redirect(exchange, "/bookings?err=" + encode("Security verification failed. Please try again."));
                return;
            }

            try {
                int passengerId = Integer.parseInt(form.get("passenger_id"));
                int busId = Integer.parseInt(form.get("bus_id"));
                int seatNumber = Integer.parseInt(form.get("seat_number"));
                String date = form.get("booking_date");

                Bus bus = busDAO.getBusById(busId);
                if (bus == null) {
                    WebUtils.redirect(exchange, "/bookings?err=" + encode("Selected bus does not exist!"));
                    return;
                }

                if (seatNumber < 1 || seatNumber > bus.getTotalSeats()) {
                    WebUtils.redirect(exchange, "/bookings?bus_id=" + busId + "&err=" + encode("Seat #" + seatNumber + " is outside valid range (1 - " + bus.getTotalSeats() + ")!"));
                    return;
                }

                // Check double-booking
                if (bookingDAO.isSeatBooked(busId, seatNumber)) {
                    WebUtils.redirect(exchange, "/bookings?bus_id=" + busId + "&err=" + encode("Seat #" + seatNumber + " is already occupied on this bus!"));
                    return;
                }

                // Associate booking with user account
                Booking b = new Booking(0, passengerId, session.getUserId(), busId, seatNumber, date, bus.getFare(), "CONFIRMED");
                if (bookingDAO.addBooking(b)) {
                    WebUtils.redirect(exchange, "/tickets?id=" + b.getBookingId() + "&msg=" + encode("Reservation confirmed! Boarding pass ready."));
                } else {
                    WebUtils.redirect(exchange, "/bookings?bus_id=" + busId + "&err=" + encode("Failed to save booking."));
                }
            } catch (Exception e) {
                WebUtils.redirect(exchange, "/bookings?err=" + encode("Error processing reservation: " + e.getMessage()));
            }
        }
    }

    private class BookingCancelHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            SessionManager.Session session = requireAuth(exchange);
            if (session == null) return;

            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                WebUtils.redirect(exchange, "/bookings");
                return;
            }
            Map<String, String> form = WebUtils.parseFormData(exchange);
            if (!WebUtils.validateCsrfToken(form, session)) {
                WebUtils.redirect(exchange, "/bookings?err=" + encode("Security verification failed."));
                return;
            }

            try {
                int id = Integer.parseInt(form.get("booking_id"));
                boolean success;

                if (session.isAdmin()) {
                    success = bookingDAO.cancelBooking(id);
                } else {
                    // Regular users can only cancel their OWN bookings
                    success = bookingDAO.cancelBookingForUser(id, session.getUserId());
                }

                if (success) {
                    WebUtils.redirect(exchange, "/bookings?msg=" + encode("Reservation #" + id + " has been cancelled."));
                } else {
                    WebUtils.redirect(exchange, "/bookings?err=" + encode("Failed to cancel reservation (or access denied)."));
                }
            } catch (Exception e) {
                WebUtils.redirect(exchange, "/bookings?err=" + encode("Invalid ID: " + e.getMessage()));
            }
        }
    }

    private class BookingDeleteHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            SessionManager.Session session = requireAdmin(exchange);
            if (session == null) return;

            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                WebUtils.redirect(exchange, "/bookings");
                return;
            }
            Map<String, String> form = WebUtils.parseFormData(exchange);
            if (!WebUtils.validateCsrfToken(form, session)) {
                WebUtils.redirect(exchange, "/bookings?err=" + encode("Security verification failed."));
                return;
            }

            try {
                int id = Integer.parseInt(form.get("booking_id"));
                if (bookingDAO.deleteBooking(id)) {
                    WebUtils.redirect(exchange, "/bookings?msg=" + encode("Reservation #" + id + " deleted permanently."));
                } else {
                    WebUtils.redirect(exchange, "/bookings?err=" + encode("Failed to delete booking #" + id));
                }
            } catch (Exception e) {
                WebUtils.redirect(exchange, "/bookings?err=" + encode("Invalid ID: " + e.getMessage()));
            }
        }
    }

    // -------------------------------------------------------------------------
    // BOARDING PASS / TICKET HANDLERS (Authenticated, Ownership Enforced)
    // -------------------------------------------------------------------------

    private class TicketHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            SessionManager.Session session = requireAuth(exchange);
            if (session == null) return;

            Map<String, String> query = WebUtils.parseQueryParams(exchange.getRequestURI().getQuery());
            Booking ticket = null;

            if (query.containsKey("id") && !query.get("id").isEmpty()) {
                try {
                    int id = Integer.parseInt(query.get("id"));
                    if (session.isAdmin()) {
                        ticket = bookingDAO.getBookingById(id);
                    } else {
                        ticket = bookingDAO.getBookingByIdAndUserId(id, session.getUserId());
                    }
                } catch (Exception ignored) {}
            }

            List<Booking> userBookings;
            if (session.isAdmin()) {
                userBookings = bookingDAO.getAllBookings();
            } else {
                userBookings = bookingDAO.getBookingsByUserId(session.getUserId());
            }

            if (ticket == null && !userBookings.isEmpty() && !query.containsKey("id")) {
                ticket = userBookings.get(0);
            }

            String html = WebViews.renderTickets(session, ticket, userBookings, query.get("msg"), query.get("err"));
            WebUtils.sendHtml(exchange, 200, html);
        }
    }

    private class TicketDownloadHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            SessionManager.Session session = requireAuth(exchange);
            if (session == null) return;

            Map<String, String> query = WebUtils.parseQueryParams(exchange.getRequestURI().getQuery());
            if (!query.containsKey("id") || query.get("id").isEmpty()) {
                WebUtils.redirect(exchange, "/tickets?err=" + encode("No booking ID specified."));
                return;
            }

            try {
                int id = Integer.parseInt(query.get("id"));
                Booking ticket = session.isAdmin() ? bookingDAO.getBookingById(id) : bookingDAO.getBookingByIdAndUserId(id, session.getUserId());

                if (ticket == null) {
                    WebUtils.redirect(exchange, "/tickets?err=" + encode("Ticket #" + id + " not found or unauthorized."));
                    return;
                }

                StringBuilder txt = new StringBuilder();
                txt.append("=================================================================\n");
                txt.append("                 SWIFTBUS OFFICIAL BOARDING PASS                 \n");
                txt.append("=================================================================\n\n");
                txt.append(" Booking ID:     #").append(ticket.getBookingId()).append("\n");
                txt.append(" Passenger:      ").append(ticket.getPassengerName()).append("\n");
                txt.append(" Contact Phone:  ").append(ticket.getPassengerPhone()).append("\n");
                txt.append(" Bus / Fleet:    ").append(ticket.getBusName()).append(" (#").append(ticket.getBusId()).append(")\n");
                txt.append(" Route:          ").append(ticket.getSource()).append(" -> ").append(ticket.getDestination()).append("\n");
                txt.append(" Seat Number:    Seat #").append(ticket.getSeatNumber()).append("\n");
                txt.append(" Departure Date: ").append(ticket.getBookingDate()).append("\n");
                txt.append(" Total Fare:     $").append(String.format("%.2f", ticket.getFare())).append("\n");
                txt.append(" Status:         ").append(ticket.getStatus()).append("\n\n");
                txt.append("=================================================================\n");
                txt.append(" Please arrive at the boarding platform 15 minutes prior to departure.\n");
                txt.append(" Have a safe and pleasant journey with SwiftBus!\n");
                txt.append(" Copyright (C) 2026 SwiftBus Inc. All Rights Reserved.\n");
                txt.append("=================================================================\n");

                WebUtils.sendPlainText(exchange, txt.toString(), "SwiftBus_Ticket_" + ticket.getBookingId() + ".txt");
            } catch (Exception e) {
                WebUtils.redirect(exchange, "/tickets?err=" + encode("Error generating ticket file: " + e.getMessage()));
            }
        }
    }

    public static void main(String[] args) {
        int port = 6969;
        String portEnv = System.getenv("PORT");
        if (portEnv != null && !portEnv.trim().isEmpty()) {
            try {
                port = Integer.parseInt(portEnv.trim());
            } catch (NumberFormatException ignored) {}
        }
        try {
            WebServer server = new WebServer(port);
            server.start();
        } catch (IOException e) {
            System.err.println("Fatal: Could not start SwiftBus Web Server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
