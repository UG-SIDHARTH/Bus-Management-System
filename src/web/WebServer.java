package web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import dao.BookingDAO;
import dao.BusDAO;
import dao.PassengerDAO;
import model.Booking;
import model.Bus;
import model.Passenger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

public class WebServer {

    private final int port;
    private final BusDAO busDAO;
    private final PassengerDAO passengerDAO;
    private final BookingDAO bookingDAO;
    private HttpServer server;

    public WebServer(int port) {
        this.port = port;
        this.busDAO = new BusDAO();
        this.passengerDAO = new PassengerDAO();
        this.bookingDAO = new BookingDAO();
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);

        // Static CSS
        server.createContext("/static/style.css", exchange -> {
            WebUtils.sendCss(exchange, WebViews.getCss());
        });

        // Auth
        server.createContext("/login", new LoginHandler());
        server.createContext("/do-login", new DoLoginHandler());
        server.createContext("/logout", exchange -> {
            WebUtils.setAuthCookie(exchange, false);
            WebUtils.redirect(exchange, "/login?msg=" + encode("Logged out successfully"));
        });

        // Core Management Routes
        server.createContext("/", new DashboardHandler());
        server.createContext("/buses", new BusHandler());
        server.createContext("/buses/add", new BusAddHandler());
        server.createContext("/buses/update", new BusUpdateHandler());
        server.createContext("/buses/delete", new BusDeleteHandler());

        server.createContext("/passengers", new PassengerHandler());
        server.createContext("/passengers/add", new PassengerAddHandler());
        server.createContext("/passengers/update", new PassengerUpdateHandler());
        server.createContext("/passengers/delete", new PassengerDeleteHandler());

        server.createContext("/bookings", new BookingHandler());
        server.createContext("/bookings/add", new BookingAddHandler());
        server.createContext("/bookings/cancel", new BookingCancelHandler());
        server.createContext("/bookings/delete", new BookingDeleteHandler());

        server.createContext("/tickets", new TicketHandler());
        server.createContext("/tickets/download", new TicketDownloadHandler());

        server.setExecutor(Executors.newFixedThreadPool(20));
        server.start();

        System.out.println("==================================================");
        System.out.println(" 🚍 SwiftBus Web Management System is LIVE!");
        System.out.println(" Web Portal URL: http://localhost:" + port);
        System.out.println(" Pure Java HTTP Server (Zero extra frameworks)");
        System.out.println(" Default Login: admin / 1234");
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

    private boolean checkAuth(HttpExchange exchange) throws IOException {
        if (!WebUtils.isAuthenticated(exchange)) {
            WebUtils.redirect(exchange, "/login?err=" + encode("Please sign in to access the system"));
            return false;
        }
        return true;
    }

    // -------------------------------------------------------------------------
    // HANDLERS
    // -------------------------------------------------------------------------

    private class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (WebUtils.isAuthenticated(exchange)) {
                WebUtils.redirect(exchange, "/");
                return;
            }
            Map<String, String> query = WebUtils.parseQueryParams(exchange.getRequestURI().getQuery());
            String err = query.get("err");
            WebUtils.sendHtml(exchange, 200, WebViews.renderLogin(err));
        }
    }

    private class DoLoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                WebUtils.redirect(exchange, "/login");
                return;
            }
            Map<String, String> form = WebUtils.parseFormData(exchange);
            String user = form.get("username");
            String pass = form.get("password");

            if ("admin".equals(user) && "1234".equals(pass)) {
                WebUtils.setAuthCookie(exchange, true);
                WebUtils.redirect(exchange, "/?msg=" + encode("Welcome back, Administrator!"));
            } else {
                WebUtils.redirect(exchange, "/login?err=" + encode("Invalid credentials! Use admin / 1234"));
            }
        }
    }

    private class DashboardHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (!"/".equals(path) && !"/dashboard".equals(path)) {
                WebUtils.redirect(exchange, "/");
                return;
            }
            if (!checkAuth(exchange)) return;

            Map<String, String> query = WebUtils.parseQueryParams(exchange.getRequestURI().getQuery());
            List<Bus> buses = busDAO.getAllBuses();
            List<Passenger> passengers = passengerDAO.getAllPassengers();
            List<Booking> bookings = bookingDAO.getAllBookings();

            int totalBuses = buses.size();
            int totalPassengers = passengers.size();
            int totalBookings = bookings.size();

            double totalRevenue = 0.0;
            for (Booking b : bookings) {
                if ("CONFIRMED".equalsIgnoreCase(b.getStatus())) {
                    totalRevenue += b.getFare();
                }
            }

            String html = WebViews.renderDashboard(totalBuses, totalPassengers, totalBookings, totalRevenue, bookings, query.get("msg"), query.get("err"));
            WebUtils.sendHtml(exchange, 200, html);
        }
    }

    // ------------------- BUS HANDLERS -------------------
    private class BusHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!checkAuth(exchange)) return;

            Map<String, String> query = WebUtils.parseQueryParams(exchange.getRequestURI().getQuery());
            Bus editBus = null;
            if (query.containsKey("edit")) {
                try {
                    int editId = Integer.parseInt(query.get("edit"));
                    editBus = busDAO.getBusById(editId);
                } catch (Exception ignored) {}
            }

            List<Bus> buses = busDAO.getAllBuses();
            String html = WebViews.renderBuses(buses, editBus, query.get("msg"), query.get("err"));
            WebUtils.sendHtml(exchange, 200, html);
        }
    }

    private class BusAddHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!checkAuth(exchange)) return;
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                WebUtils.redirect(exchange, "/buses");
                return;
            }
            Map<String, String> form = WebUtils.parseFormData(exchange);
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
                    WebUtils.redirect(exchange, "/buses?msg=" + encode("Bus #" + id + " created successfully!"));
                } else {
                    WebUtils.redirect(exchange, "/buses?err=" + encode("Failed to create bus in database."));
                }
            } catch (Exception e) {
                WebUtils.redirect(exchange, "/buses?err=" + encode("Invalid input data: " + e.getMessage()));
            }
        }
    }

    private class BusUpdateHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!checkAuth(exchange)) return;
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                WebUtils.redirect(exchange, "/buses");
                return;
            }
            Map<String, String> form = WebUtils.parseFormData(exchange);
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
            if (!checkAuth(exchange)) return;
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                WebUtils.redirect(exchange, "/buses");
                return;
            }
            Map<String, String> form = WebUtils.parseFormData(exchange);
            try {
                int id = Integer.parseInt(form.get("bus_id"));
                if (busDAO.deleteBus(id)) {
                    WebUtils.redirect(exchange, "/buses?msg=" + encode("Bus #" + id + " deleted successfully."));
                } else {
                    WebUtils.redirect(exchange, "/buses?err=" + encode("Failed to delete bus #" + id));
                }
            } catch (Exception e) {
                WebUtils.redirect(exchange, "/buses?err=" + encode("Invalid ID: " + e.getMessage()));
            }
        }
    }

    // ------------------- PASSENGER HANDLERS -------------------
    private class PassengerHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!checkAuth(exchange)) return;

            Map<String, String> query = WebUtils.parseQueryParams(exchange.getRequestURI().getQuery());
            Passenger editPassenger = null;
            if (query.containsKey("edit")) {
                try {
                    int editId = Integer.parseInt(query.get("edit"));
                    editPassenger = passengerDAO.getPassengerById(editId);
                } catch (Exception ignored) {}
            }

            List<Passenger> passengers = passengerDAO.getAllPassengers();
            String html = WebViews.renderPassengers(passengers, editPassenger, query.get("msg"), query.get("err"));
            WebUtils.sendHtml(exchange, 200, html);
        }
    }

    private class PassengerAddHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!checkAuth(exchange)) return;
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                WebUtils.redirect(exchange, "/passengers");
                return;
            }
            Map<String, String> form = WebUtils.parseFormData(exchange);
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
                    WebUtils.redirect(exchange, "/passengers?err=" + encode("Failed to register passenger."));
                }
            } catch (Exception e) {
                WebUtils.redirect(exchange, "/passengers?err=" + encode("Invalid input: " + e.getMessage()));
            }
        }
    }

    private class PassengerUpdateHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!checkAuth(exchange)) return;
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                WebUtils.redirect(exchange, "/passengers");
                return;
            }
            Map<String, String> form = WebUtils.parseFormData(exchange);
            try {
                int id = Integer.parseInt(form.get("passenger_id"));
                String name = form.get("name");
                int age = Integer.parseInt(form.get("age"));
                String gender = form.get("gender");
                String phone = form.get("phone");

                Passenger p = new Passenger(id, name, age, gender, phone);
                if (passengerDAO.updatePassenger(p)) {
                    WebUtils.redirect(exchange, "/passengers?msg=" + encode("Passenger #" + id + " updated successfully!"));
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
            if (!checkAuth(exchange)) return;
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                WebUtils.redirect(exchange, "/passengers");
                return;
            }
            Map<String, String> form = WebUtils.parseFormData(exchange);
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

    // ------------------- BOOKING HANDLERS -------------------
    private class BookingHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!checkAuth(exchange)) return;

            Map<String, String> query = WebUtils.parseQueryParams(exchange.getRequestURI().getQuery());
            Integer selectedBusId = null;
            List<Integer> bookedSeats = null;

            if (query.containsKey("bus_id") && !query.get("bus_id").isEmpty()) {
                try {
                    selectedBusId = Integer.parseInt(query.get("bus_id"));
                    bookedSeats = bookingDAO.getBookedSeats(selectedBusId);
                } catch (Exception ignored) {}
            }

            List<Booking> bookings = bookingDAO.getAllBookings();
            List<Bus> buses = busDAO.getAllBuses();
            List<Passenger> passengers = passengerDAO.getAllPassengers();

            String html = WebViews.renderBookings(bookings, buses, passengers, bookedSeats, selectedBusId, query.get("msg"), query.get("err"));
            WebUtils.sendHtml(exchange, 200, html);
        }
    }

    private class BookingAddHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!checkAuth(exchange)) return;
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                WebUtils.redirect(exchange, "/bookings");
                return;
            }
            Map<String, String> form = WebUtils.parseFormData(exchange);
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
                    WebUtils.redirect(exchange, "/bookings?err=" + encode("Seat #" + seatNumber + " is outside valid range (1 - " + bus.getTotalSeats() + ")!"));
                    return;
                }

                // Check double-booking
                if (bookingDAO.isSeatBooked(busId, seatNumber)) {
                    WebUtils.redirect(exchange, "/bookings?bus_id=" + busId + "&err=" + encode("Seat #" + seatNumber + " is already occupied on this bus!"));
                    return;
                }

                Booking b = new Booking(0, passengerId, busId, seatNumber, date, bus.getFare(), "CONFIRMED");
                if (bookingDAO.addBooking(b)) {
                    WebUtils.redirect(exchange, "/tickets?id=" + b.getBookingId() + "&msg=" + encode("Reservation confirmed! Boarding pass ready."));
                } else {
                    WebUtils.redirect(exchange, "/bookings?err=" + encode("Failed to save booking."));
                }
            } catch (Exception e) {
                WebUtils.redirect(exchange, "/bookings?err=" + encode("Error processing reservation: " + e.getMessage()));
            }
        }
    }

    private class BookingCancelHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!checkAuth(exchange)) return;
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                WebUtils.redirect(exchange, "/bookings");
                return;
            }
            Map<String, String> form = WebUtils.parseFormData(exchange);
            try {
                int id = Integer.parseInt(form.get("booking_id"));
                if (bookingDAO.cancelBooking(id)) {
                    WebUtils.redirect(exchange, "/bookings?msg=" + encode("Reservation #" + id + " has been cancelled."));
                } else {
                    WebUtils.redirect(exchange, "/bookings?err=" + encode("Failed to cancel booking #" + id));
                }
            } catch (Exception e) {
                WebUtils.redirect(exchange, "/bookings?err=" + encode("Invalid ID: " + e.getMessage()));
            }
        }
    }

    private class BookingDeleteHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!checkAuth(exchange)) return;
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                WebUtils.redirect(exchange, "/bookings");
                return;
            }
            Map<String, String> form = WebUtils.parseFormData(exchange);
            try {
                int id = Integer.parseInt(form.get("booking_id"));
                if (bookingDAO.deleteBooking(id)) {
                    WebUtils.redirect(exchange, "/bookings?msg=" + encode("Booking #" + id + " removed from records."));
                } else {
                    WebUtils.redirect(exchange, "/bookings?err=" + encode("Failed to delete booking #" + id));
                }
            } catch (Exception e) {
                WebUtils.redirect(exchange, "/bookings?err=" + encode("Invalid ID: " + e.getMessage()));
            }
        }
    }

    // ------------------- TICKET HANDLERS -------------------
    private class TicketHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!checkAuth(exchange)) return;

            Map<String, String> query = WebUtils.parseQueryParams(exchange.getRequestURI().getQuery());
            Booking ticket = null;
            if (query.containsKey("id") && !query.get("id").isEmpty()) {
                try {
                    int ticketId = Integer.parseInt(query.get("id"));
                    ticket = bookingDAO.getBookingById(ticketId);
                    if (ticket == null) {
                        query.put("err", "No booking found with ID #" + ticketId);
                    }
                } catch (Exception e) {
                    query.put("err", "Invalid booking reference ID.");
                }
            }

            List<Booking> allBookings = bookingDAO.getAllBookings();
            String html = WebViews.renderTickets(ticket, allBookings, query.get("msg"), query.get("err"));
            WebUtils.sendHtml(exchange, 200, html);
        }
    }

    private class TicketDownloadHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!checkAuth(exchange)) return;

            Map<String, String> query = WebUtils.parseQueryParams(exchange.getRequestURI().getQuery());
            int id = 0;
            try {
                id = Integer.parseInt(query.get("id"));
            } catch (Exception e) {
                WebUtils.redirect(exchange, "/tickets?err=" + encode("Invalid booking ID."));
                return;
            }

            Booking b = bookingDAO.getBookingById(id);
            if (b == null) {
                WebUtils.redirect(exchange, "/tickets?err=" + encode("Booking not found."));
                return;
            }

            String receipt =
                    "==========================================================\n" +
                    "             SWIFTBUS EXPRESS LINES - BOARDING PASS       \n" +
                    "==========================================================\n" +
                    " Booking ID   : #" + b.getBookingId() + "\n" +
                    " Status       : " + b.getStatus() + "\n" +
                    " Date         : " + b.getBookingDate() + "\n" +
                    "----------------------------------------------------------\n" +
                    " Passenger    : " + (b.getPassengerName() != null ? b.getPassengerName() : "ID #" + b.getPassengerId()) + "\n" +
                    " Contact      : " + (b.getPassengerPhone() != null ? b.getPassengerPhone() : "N/A") + "\n" +
                    "----------------------------------------------------------\n" +
                    " Bus Coach    : " + (b.getBusName() != null ? b.getBusName() : "Bus #" + b.getBusId()) + "\n" +
                    " Departure    : " + b.getSource() + "\n" +
                    " Destination  : " + b.getDestination() + "\n" +
                    " Seat Number  : " + b.getSeatNumber() + "\n" +
                    " Total Fare   : $" + String.format("%.2f", b.getFare()) + " (PAID)\n" +
                    "==========================================================\n" +
                    " Thank you for traveling with SwiftBus Express!\n" +
                    " Please present this boarding receipt before boarding.\n" +
                    "==========================================================\n";

            WebUtils.sendPlainText(exchange, receipt, "Ticket_" + id + ".txt");
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
            WebServer webServer = new WebServer(port);
            webServer.start();
        } catch (Exception e) {
            System.err.println("Failed to start SwiftBus WebServer: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
