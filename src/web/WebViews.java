package web;

import model.Booking;
import model.Bus;
import model.Passenger;

import java.time.LocalDate;
import java.util.List;

public class WebViews {

    private static String navbar(String active) {
        return "<nav class=\"navbar\">" +
                "  <div class=\"nav-container\">" +
                "    <a href=\"/\" class=\"brand\">" +
                "      <span class=\"brand-icon\">🚍</span>" +
                "      <span class=\"brand-text\">SwiftBus <span class=\"brand-badge\">Admin</span></span>" +
                "    </a>" +
                "    <div class=\"nav-links\">" +
                "      <a href=\"/\" class=\"nav-link " + ("dashboard".equals(active) ? "active" : "") + "\">📊 Dashboard</a>" +
                "      <a href=\"/buses\" class=\"nav-link " + ("buses".equals(active) ? "active" : "") + "\">🚌 Buses</a>" +
                "      <a href=\"/passengers\" class=\"nav-link " + ("passengers".equals(active) ? "active" : "") + "\">👥 Passengers</a>" +
                "      <a href=\"/bookings\" class=\"nav-link " + ("bookings".equals(active) ? "active" : "") + "\">🎟️ Bookings</a>" +
                "      <a href=\"/tickets\" class=\"nav-link " + ("tickets".equals(active) ? "active" : "") + "\">🎫 Tickets</a>" +
                "    </div>" +
                "    <div class=\"nav-user\">" +
                "      <span class=\"user-badge\">👤 admin</span>" +
                "      <a href=\"/logout\" class=\"btn btn-sm btn-outline-danger\">Logout</a>" +
                "    </div>" +
                "  </div>" +
                "</nav>";
    }

    private static String alerts(String msg, String err) {
        StringBuilder sb = new StringBuilder();
        if (msg != null && !msg.trim().isEmpty()) {
            sb.append("<div class=\"alert alert-success\">✅ ").append(WebUtils.escapeHtml(msg)).append("</div>");
        }
        if (err != null && !err.trim().isEmpty()) {
            sb.append("<div class=\"alert alert-danger\">⚠️ ").append(WebUtils.escapeHtml(err)).append("</div>");
        }
        return sb.toString();
    }

    private static String htmlDoc(String title, String activeNav, String content, String msg, String err) {
        return "<!DOCTYPE html>" +
                "<html lang=\"en\">" +
                "<head>" +
                "  <meta charset=\"UTF-8\" />" +
                "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />" +
                "  <title>" + WebUtils.escapeHtml(title) + " - Bus Management System</title>" +
                "  <link rel=\"preconnect\" href=\"https://fonts.googleapis.com\">" +
                "  <link rel=\"preconnect\" href=\"https://fonts.gstatic.com\" crossorigin>" +
                "  <link href=\"https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&family=JetBrains+Mono:wght@500;700&display=swap\" rel=\"stylesheet\">" +
                "  <link rel=\"stylesheet\" href=\"/static/style.css\" />" +
                "</head>" +
                "<body>" +
                navbar(activeNav) +
                "<main class=\"main-content\">" +
                alerts(msg, err) +
                content +
                "</main>" +
                "<footer class=\"footer\">" +
                "  <p>&copy; 2026 Bus Management System &bull; Java Native Web Edition &bull; Port 6969</p>" +
                "</footer>" +
                "</body>" +
                "</html>";
    }

    public static String renderLogin(String err) {
        return "<!DOCTYPE html>" +
                "<html lang=\"en\">" +
                "<head>" +
                "  <meta charset=\"UTF-8\" />" +
                "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />" +
                "  <title>Admin Login - Bus Management System</title>" +
                "  <link rel=\"preconnect\" href=\"https://fonts.googleapis.com\">" +
                "  <link rel=\"preconnect\" href=\"https://fonts.gstatic.com\" crossorigin>" +
                "  <link href=\"https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap\" rel=\"stylesheet\">" +
                "  <link rel=\"stylesheet\" href=\"/static/style.css\" />" +
                "</head>" +
                "<body class=\"login-body\">" +
                "  <div class=\"login-card\">" +
                "    <div class=\"login-header\">" +
                "      <div class=\"login-logo\">🚍</div>" +
                "      <h1>SwiftBus Portal</h1>" +
                "      <p>Bus Management & Reservation System</p>" +
                "    </div>" +
                (err != null && !err.isEmpty() ? "<div class=\"alert alert-danger\">⚠️ " + WebUtils.escapeHtml(err) + "</div>" : "") +
                "    <form action=\"/do-login\" method=\"POST\" class=\"login-form\">" +
                "      <div class=\"form-group\">" +
                "        <label for=\"username\">Username</label>" +
                "        <input type=\"text\" id=\"username\" name=\"username\" value=\"admin\" required autofocus />" +
                "      </div>" +
                "      <div class=\"form-group\">" +
                "        <label for=\"password\">Password</label>" +
                "        <input type=\"password\" id=\"password\" name=\"password\" value=\"1234\" required />" +
                "      </div>" +
                "      <button type=\"submit\" class=\"btn btn-primary btn-block\">Sign In &rarr;</button>" +
                "    </form>" +
                "    <div class=\"login-footer\">" +
                "      <small>Default credentials: <code>admin</code> / <code>1234</code></small>" +
                "    </div>" +
                "  </div>" +
                "</body>" +
                "</html>";
    }

    public static String renderDashboard(int totalBuses, int totalPassengers, int totalBookings, double totalRevenue, List<Booking> recentBookings, String msg, String err) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div class=\"page-header\">");
        sb.append("  <div>");
        sb.append("    <h1>System Dashboard</h1>");
        sb.append("    <p class=\"subtitle\">Overview of bus fleet, passengers, and reservation activities</p>");
        sb.append("  </div>");
        sb.append("  <div class=\"header-actions\">");
        sb.append("    <a href=\"/bookings\" class=\"btn btn-primary\">+ New Reservation</a>");
        sb.append("    <a href=\"/buses\" class=\"btn btn-secondary\">+ Manage Buses</a>");
        sb.append("  </div>");
        sb.append("</div>");

        // Stat cards
        sb.append("<div class=\"stats-grid\">");
        sb.append("  <div class=\"stat-card stat-buses\">");
        sb.append("    <div class=\"stat-icon\">🚌</div>");
        sb.append("    <div class=\"stat-info\">");
        sb.append("      <span class=\"stat-value\">").append(totalBuses).append("</span>");
        sb.append("      <span class=\"stat-label\">Registered Buses</span>");
        sb.append("    </div>");
        sb.append("  </div>");

        sb.append("  <div class=\"stat-card stat-passengers\">");
        sb.append("    <div class=\"stat-icon\">👥</div>");
        sb.append("    <div class=\"stat-info\">");
        sb.append("      <span class=\"stat-value\">").append(totalPassengers).append("</span>");
        sb.append("      <span class=\"stat-label\">Total Passengers</span>");
        sb.append("    </div>");
        sb.append("  </div>");

        sb.append("  <div class=\"stat-card stat-bookings\">");
        sb.append("    <div class=\"stat-icon\">🎟️</div>");
        sb.append("    <div class=\"stat-info\">");
        sb.append("      <span class=\"stat-value\">").append(totalBookings).append("</span>");
        sb.append("      <span class=\"stat-label\">Total Reservations</span>");
        sb.append("    </div>");
        sb.append("  </div>");

        sb.append("  <div class=\"stat-card stat-revenue\">");
        sb.append("    <div class=\"stat-icon\">💰</div>");
        sb.append("    <div class=\"stat-info\">");
        sb.append("      <span class=\"stat-value\">$").append(String.format("%.2f", totalRevenue)).append("</span>");
        sb.append("      <span class=\"stat-label\">Confirmed Revenue</span>");
        sb.append("    </div>");
        sb.append("  </div>");
        sb.append("</div>");

        // Quick navigation shortcuts
        sb.append("<div class=\"section-title\"><h3>Quick Actions</h3></div>");
        sb.append("<div class=\"quick-actions-grid\">");
        sb.append("  <a href=\"/buses\" class=\"quick-card\">");
        sb.append("    <span class=\"quick-icon\">🚌</span>");
        sb.append("    <h4>Bus Fleet</h4>");
        sb.append("    <p>Add, edit routes, set seat capacities and ticket fares</p>");
        sb.append("  </a>");
        sb.append("  <a href=\"/passengers\" class=\"quick-card\">");
        sb.append("    <span class=\"quick-icon\">👥</span>");
        sb.append("    <h4>Passengers</h4>");
        sb.append("    <p>Register travelers, manage contact records and demographics</p>");
        sb.append("  </a>");
        sb.append("  <a href=\"/bookings\" class=\"quick-card\">");
        sb.append("    <span class=\"quick-icon\">🎟️</span>");
        sb.append("    <h4>Reservations</h4>");
        sb.append("    <p>Book seats with double-booking safety and cancellations</p>");
        sb.append("  </a>");
        sb.append("  <a href=\"/tickets\" class=\"quick-card\">");
        sb.append("    <span class=\"quick-icon\">🎫</span>");
        sb.append("    <h4>Boarding Passes</h4>");
        sb.append("    <p>Generate, preview, and print official boarding tickets</p>");
        sb.append("  </a>");
        sb.append("</div>");

        // Recent bookings table
        sb.append("<div class=\"card\">");
        sb.append("  <div class=\"card-header\">");
        sb.append("    <h3>Recent Reservations</h3>");
        sb.append("    <a href=\"/bookings\" class=\"btn btn-sm btn-outline\">View All Bookings &rarr;</a>");
        sb.append("  </div>");
        sb.append("  <div class=\"table-responsive\">");
        sb.append("    <table class=\"table\">");
        sb.append("      <thead>");
        sb.append("        <tr><th>ID</th><th>Passenger</th><th>Bus Route</th><th>Seat #</th><th>Date</th><th>Fare</th><th>Status</th><th>Actions</th></tr>");
        sb.append("      </thead><tbody>");
        if (recentBookings == null || recentBookings.isEmpty()) {
            sb.append("      <tr><td colspan=\"8\" class=\"text-center text-muted\">No bookings recorded yet.</td></tr>");
        } else {
            int count = 0;
            for (Booking b : recentBookings) {
                if (count++ >= 8) break;
                sb.append("      <tr>");
                sb.append("        <td><strong>#").append(b.getBookingId()).append("</strong></td>");
                sb.append("        <td>").append(WebUtils.escapeHtml(b.getPassengerName() != null ? b.getPassengerName() : "ID: " + b.getPassengerId())).append("</td>");
                sb.append("        <td>").append(WebUtils.escapeHtml(b.getSource() != null ? b.getSource() + " &rarr; " + b.getDestination() : "Bus #" + b.getBusId())).append("</td>");
                sb.append("        <td><span class=\"badge badge-seat\">Seat ").append(b.getSeatNumber()).append("</span></td>");
                sb.append("        <td>").append(WebUtils.escapeHtml(b.getBookingDate())).append("</td>");
                sb.append("        <td>$").append(String.format("%.2f", b.getFare())).append("</td>");
                sb.append("        <td><span class=\"badge badge-").append("CONFIRMED".equalsIgnoreCase(b.getStatus()) ? "confirmed" : "cancelled").append("\">").append(WebUtils.escapeHtml(b.getStatus())).append("</span></td>");
                sb.append("        <td><a href=\"/tickets?id=").append(b.getBookingId()).append("\" class=\"btn btn-xs btn-primary\">Ticket</a></td>");
                sb.append("      </tr>");
            }
        }
        sb.append("      </tbody>");
        sb.append("    </table>");
        sb.append("  </div>");
        sb.append("</div>");

        return htmlDoc("Dashboard", "dashboard", sb.toString(), msg, err);
    }

    public static String renderBuses(List<Bus> buses, Bus editBus, String msg, String err) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div class=\"page-header\">");
        sb.append("  <div>");
        sb.append("    <h1>Bus Fleet Management</h1>");
        sb.append("    <p class=\"subtitle\">Configure buses, travel routes, capacity limits, and pricing</p>");
        sb.append("  </div>");
        sb.append("</div>");

        sb.append("<div class=\"grid-form-table\">");

        // Form Card
        sb.append("  <div class=\"card form-card\">");
        sb.append("    <div class=\"card-header\">");
        sb.append("      <h3>").append(editBus != null ? "✏️ Edit Bus #" + editBus.getBusId() : "➕ Add New Bus").append("</h3>");
        if (editBus != null) {
            sb.append("      <a href=\"/buses\" class=\"btn btn-xs btn-outline\">Cancel</a>");
        }
        sb.append("    </div>");
        sb.append("    <form action=\"").append(editBus != null ? "/buses/update" : "/buses/add").append("\" method=\"POST\">");
        if (editBus != null) {
            sb.append("      <input type=\"hidden\" name=\"bus_id\" value=\"").append(editBus.getBusId()).append("\" />");
        } else {
            sb.append("      <div class=\"form-group\">");
            sb.append("        <label for=\"bus_id\">Bus ID</label>");
            sb.append("        <input type=\"number\" id=\"bus_id\" name=\"bus_id\" placeholder=\"e.g. 5\" required />");
            sb.append("      </div>");
        }
        sb.append("      <div class=\"form-group\">");
        sb.append("        <label for=\"bus_name\">Bus Name / Model</label>");
        sb.append("        <input type=\"text\" id=\"bus_name\" name=\"bus_name\" value=\"").append(editBus != null ? WebUtils.escapeHtml(editBus.getBusName()) : "").append("\" placeholder=\"e.g. Scania Luxury Cruiser\" required />");
        sb.append("      </div>");
        sb.append("      <div class=\"form-row\">");
        sb.append("        <div class=\"form-group\">");
        sb.append("          <label for=\"source\">Origin / Source</label>");
        sb.append("          <input type=\"text\" id=\"source\" name=\"source\" value=\"").append(editBus != null ? WebUtils.escapeHtml(editBus.getSource()) : "").append("\" placeholder=\"e.g. New York\" required />");
        sb.append("        </div>");
        sb.append("        <div class=\"form-group\">");
        sb.append("          <label for=\"destination\">Destination</label>");
        sb.append("          <input type=\"text\" id=\"destination\" name=\"destination\" value=\"").append(editBus != null ? WebUtils.escapeHtml(editBus.getDestination()) : "").append("\" placeholder=\"e.g. Boston\" required />");
        sb.append("        </div>");
        sb.append("      </div>");
        sb.append("      <div class=\"form-row\">");
        sb.append("        <div class=\"form-group\">");
        sb.append("          <label for=\"total_seats\">Total Seats</label>");
        sb.append("          <input type=\"number\" id=\"total_seats\" name=\"total_seats\" value=\"").append(editBus != null ? editBus.getTotalSeats() : 40).append("\" min=\"10\" max=\"100\" required />");
        sb.append("        </div>");
        sb.append("        <div class=\"form-group\">");
        sb.append("          <label for=\"fare\">Ticket Fare ($)</label>");
        sb.append("          <input type=\"number\" id=\"fare\" name=\"fare\" step=\"0.01\" value=\"").append(editBus != null ? editBus.getFare() : "45.00").append("\" required />");
        sb.append("        </div>");
        sb.append("      </div>");
        sb.append("      <button type=\"submit\" class=\"btn btn-primary btn-block\">").append(editBus != null ? "Save Changes" : "Create Bus").append("</button>");
        sb.append("    </form>");
        sb.append("  </div>");

        // Table Card
        sb.append("  <div class=\"card\">");
        sb.append("    <div class=\"card-header\">");
        sb.append("      <h3>All Buses (").append(buses.size()).append(")</h3>");
        sb.append("    </div>");
        sb.append("    <div class=\"table-responsive\">");
        sb.append("      <table class=\"table\">");
        sb.append("        <thead>");
        sb.append("          <tr><th>ID</th><th>Bus Name</th><th>Origin &rarr; Destination</th><th>Seats</th><th>Fare</th><th>Actions</th></tr>");
        sb.append("        </thead><tbody>");
        if (buses.isEmpty()) {
            sb.append("        <tr><td colspan=\"6\" class=\"text-center text-muted\">No buses registered in the system yet.</td></tr>");
        } else {
            for (Bus b : buses) {
                sb.append("        <tr>");
                sb.append("          <td><strong>#").append(b.getBusId()).append("</strong></td>");
                sb.append("          <td>").append(WebUtils.escapeHtml(b.getBusName())).append("</td>");
                sb.append("          <td><span class=\"badge badge-route\">").append(WebUtils.escapeHtml(b.getSource())).append(" &rarr; ").append(WebUtils.escapeHtml(b.getDestination())).append("</span></td>");
                sb.append("          <td>").append(b.getTotalSeats()).append(" seats</td>");
                sb.append("          <td>$").append(String.format("%.2f", b.getFare())).append("</td>");
                sb.append("          <td class=\"actions-cell\">");
                sb.append("            <a href=\"/buses?edit=").append(b.getBusId()).append("\" class=\"btn btn-xs btn-outline\">Edit</a>");
                sb.append("            <form action=\"/buses/delete\" method=\"POST\" style=\"display:inline;\" onsubmit=\"return confirm('Delete bus #").append(b.getBusId()).append("? Associated bookings will be affected.');\">");
                sb.append("              <input type=\"hidden\" name=\"bus_id\" value=\"").append(b.getBusId()).append("\" />");
                sb.append("              <button type=\"submit\" class=\"btn btn-xs btn-outline-danger\">Delete</button>");
                sb.append("            </form>");
                sb.append("          </td>");
                sb.append("        </tr>");
            }
        }
        sb.append("        </tbody>");
        sb.append("      </table>");
        sb.append("    </div>");
        sb.append("  </div>");

        sb.append("</div>");

        return htmlDoc("Bus Fleet", "buses", sb.toString(), msg, err);
    }

    public static String renderPassengers(List<Passenger> passengers, Passenger editPassenger, String msg, String err) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div class=\"page-header\">");
        sb.append("  <div>");
        sb.append("    <h1>Passenger Directory</h1>");
        sb.append("    <p class=\"subtitle\">Manage passenger profiles, contact information, and travel records</p>");
        sb.append("  </div>");
        sb.append("</div>");

        sb.append("<div class=\"grid-form-table\">");

        // Form Card
        sb.append("  <div class=\"card form-card\">");
        sb.append("    <div class=\"card-header\">");
        sb.append("      <h3>").append(editPassenger != null ? "✏️ Edit Passenger #" + editPassenger.getPassengerId() : "➕ Register Passenger").append("</h3>");
        if (editPassenger != null) {
            sb.append("      <a href=\"/passengers\" class=\"btn btn-xs btn-outline\">Cancel</a>");
        }
        sb.append("    </div>");
        sb.append("    <form action=\"").append(editPassenger != null ? "/passengers/update" : "/passengers/add").append("\" method=\"POST\">");
        if (editPassenger != null) {
            sb.append("      <input type=\"hidden\" name=\"passenger_id\" value=\"").append(editPassenger.getPassengerId()).append("\" />");
        } else {
            sb.append("      <div class=\"form-group\">");
            sb.append("        <label for=\"passenger_id\">Passenger ID</label>");
            sb.append("        <input type=\"number\" id=\"passenger_id\" name=\"passenger_id\" placeholder=\"e.g. 104\" required />");
            sb.append("      </div>");
        }
        sb.append("      <div class=\"form-group\">");
        sb.append("        <label for=\"name\">Full Name</label>");
        sb.append("        <input type=\"text\" id=\"name\" name=\"name\" value=\"").append(editPassenger != null ? WebUtils.escapeHtml(editPassenger.getName()) : "").append("\" placeholder=\"e.g. Emily Watson\" required />");
        sb.append("      </div>");
        sb.append("      <div class=\"form-row\">");
        sb.append("        <div class=\"form-group\">");
        sb.append("          <label for=\"age\">Age</label>");
        sb.append("          <input type=\"number\" id=\"age\" name=\"age\" value=\"").append(editPassenger != null ? editPassenger.getAge() : 25).append("\" min=\"1\" max=\"120\" required />");
        sb.append("        </div>");
        sb.append("        <div class=\"form-group\">");
        sb.append("          <label for=\"gender\">Gender</label>");
        sb.append("          <select id=\"gender\" name=\"gender\" required>");
        String g = editPassenger != null ? editPassenger.getGender() : "Male";
        sb.append("            <option value=\"Male\" ").append("Male".equalsIgnoreCase(g) ? "selected" : "").append(">Male</option>");
        sb.append("            <option value=\"Female\" ").append("Female".equalsIgnoreCase(g) ? "selected" : "").append(">Female</option>");
        sb.append("            <option value=\"Other\" ").append("Other".equalsIgnoreCase(g) ? "selected" : "").append(">Other</option>");
        sb.append("          </select>");
        sb.append("        </div>");
        sb.append("      </div>");
        sb.append("      <div class=\"form-group\">");
        sb.append("        <label for=\"phone\">Phone Number</label>");
        sb.append("        <input type=\"text\" id=\"phone\" name=\"phone\" value=\"").append(editPassenger != null ? WebUtils.escapeHtml(editPassenger.getPhoneNumber()) : "").append("\" placeholder=\"e.g. 9876543210\" required />");
        sb.append("      </div>");
        sb.append("      <button type=\"submit\" class=\"btn btn-primary btn-block\">").append(editPassenger != null ? "Save Changes" : "Register Passenger").append("</button>");
        sb.append("    </form>");
        sb.append("  </div>");

        // Table Card
        sb.append("  <div class=\"card\">");
        sb.append("    <div class=\"card-header\">");
        sb.append("      <h3>All Registered Passengers (").append(passengers.size()).append(")</h3>");
        sb.append("    </div>");
        sb.append("    <div class=\"table-responsive\">");
        sb.append("      <table class=\"table\">");
        sb.append("        <thead>");
        sb.append("          <tr><th>ID</th><th>Name</th><th>Age</th><th>Gender</th><th>Phone</th><th>Actions</th></tr>");
        sb.append("        </thead><tbody>");
        if (passengers.isEmpty()) {
            sb.append("        <tr><td colspan=\"6\" class=\"text-center text-muted\">No passengers registered yet.</td></tr>");
        } else {
            for (Passenger p : passengers) {
                sb.append("        <tr>");
                sb.append("          <td><strong>#").append(p.getPassengerId()).append("</strong></td>");
                sb.append("          <td>").append(WebUtils.escapeHtml(p.getName())).append("</td>");
                sb.append("          <td>").append(p.getAge()).append(" yrs</td>");
                sb.append("          <td><span class=\"badge badge-gender\">").append(WebUtils.escapeHtml(p.getGender())).append("</span></td>");
                sb.append("          <td><code>").append(WebUtils.escapeHtml(p.getPhoneNumber())).append("</code></td>");
                sb.append("          <td class=\"actions-cell\">");
                sb.append("            <a href=\"/passengers?edit=").append(p.getPassengerId()).append("\" class=\"btn btn-xs btn-outline\">Edit</a>");
                sb.append("            <form action=\"/passengers/delete\" method=\"POST\" style=\"display:inline;\" onsubmit=\"return confirm('Delete passenger #").append(p.getPassengerId()).append("?');\">");
                sb.append("              <input type=\"hidden\" name=\"passenger_id\" value=\"").append(p.getPassengerId()).append("\" />");
                sb.append("              <button type=\"submit\" class=\"btn btn-xs btn-outline-danger\">Delete</button>");
                sb.append("            </form>");
                sb.append("          </td>");
                sb.append("        </tr>");
            }
        }
        sb.append("        </tbody>");
        sb.append("      </table>");
        sb.append("    </div>");
        sb.append("  </div>");

        sb.append("</div>");

        return htmlDoc("Passengers", "passengers", sb.toString(), msg, err);
    }

    public static String renderBookings(List<Booking> bookings, List<Bus> buses, List<Passenger> passengers, List<Integer> bookedSeats, Integer selectedBusId, String msg, String err) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div class=\"page-header\">");
        sb.append("  <div>");
        sb.append("    <h1>Seat Reservations</h1>");
        sb.append("    <p class=\"subtitle\">Book passenger seats, track departure dates, and manage ticket reservations</p>");
        sb.append("  </div>");
        sb.append("</div>");

        sb.append("<div class=\"grid-form-table\">");

        // Reservation Form
        sb.append("  <div class=\"card form-card\">");
        sb.append("    <div class=\"card-header\">");
        sb.append("      <h3>🎟️ New Reservation</h3>");
        sb.append("    </div>");
        sb.append("    <form action=\"/bookings/add\" method=\"POST\">");

        // Passenger selector
        sb.append("      <div class=\"form-group\">");
        sb.append("        <label for=\"passenger_id\">Select Passenger</label>");
        sb.append("        <select id=\"passenger_id\" name=\"passenger_id\" required>");
        sb.append("          <option value=\"\">-- Choose Passenger --</option>");
        for (Passenger p : passengers) {
            sb.append("          <option value=\"").append(p.getPassengerId()).append("\">#").append(p.getPassengerId()).append(" - ").append(WebUtils.escapeHtml(p.getName())).append(" (").append(WebUtils.escapeHtml(p.getPhoneNumber())).append(")</option>");
        }
        sb.append("        </select>");
        sb.append("      </div>");

        // Bus selector
        sb.append("      <div class=\"form-group\">");
        sb.append("        <label for=\"bus_id\">Select Bus Route</label>");
        sb.append("        <select id=\"bus_id\" name=\"bus_id\" required onchange=\"location.href='/bookings?bus_id=' + this.value;\">");
        sb.append("          <option value=\"\">-- Choose Bus & Route --</option>");
        for (Bus b : buses) {
            boolean sel = selectedBusId != null && selectedBusId == b.getBusId();
            sb.append("          <option value=\"").append(b.getBusId()).append("\" ").append(sel ? "selected" : "").append(">");
            sb.append(b.getBusId()).append(" - ").append(WebUtils.escapeHtml(b.getBusName())).append(" (").append(WebUtils.escapeHtml(b.getSource())).append(" &rarr; ").append(WebUtils.escapeHtml(b.getDestination())).append(") - $").append(String.format("%.2f", b.getFare()));
            sb.append("</option>");
        }
        sb.append("        </select>");
        sb.append("      </div>");

        // Seat Number & Date
        sb.append("      <div class=\"form-row\">");
        sb.append("        <div class=\"form-group\">");
        sb.append("          <label for=\"seat_number\">Seat Number</label>");
        sb.append("          <input type=\"number\" id=\"seat_number\" name=\"seat_number\" min=\"1\" max=\"100\" placeholder=\"e.g. 12\" required />");
        sb.append("        </div>");
        sb.append("        <div class=\"form-group\">");
        sb.append("          <label for=\"booking_date\">Journey Date</label>");
        sb.append("          <input type=\"date\" id=\"booking_date\" name=\"booking_date\" value=\"").append(LocalDate.now().toString()).append("\" required />");
        sb.append("        </div>");
        sb.append("      </div>");

        // Seat status display if bus selected
        if (selectedBusId != null && bookedSeats != null) {
            sb.append("      <div class=\"booked-seats-preview\">");
            sb.append("        <small><strong>Occupied Seats for this bus:</strong> ");
            if (bookedSeats.isEmpty()) {
                sb.append("<span class=\"text-success\">All seats currently available!</span>");
            } else {
                for (int s : bookedSeats) {
                    sb.append("<span class=\"badge badge-occupied\">#").append(s).append("</span> ");
                }
            }
            sb.append("        </small>");
            sb.append("      </div>");
        }

        sb.append("      <button type=\"submit\" class=\"btn btn-primary btn-block\">Confirm Booking &rarr;</button>");
        sb.append("    </form>");
        sb.append("  </div>");

        // Bookings Table
        sb.append("  <div class=\"card\">");
        sb.append("    <div class=\"card-header\">");
        sb.append("      <h3>All Reservations (").append(bookings.size()).append(")</h3>");
        sb.append("    </div>");
        sb.append("    <div class=\"table-responsive\">");
        sb.append("      <table class=\"table\">");
        sb.append("        <thead>");
        sb.append("          <tr><th>ID</th><th>Passenger</th><th>Bus & Route</th><th>Seat</th><th>Date</th><th>Fare</th><th>Status</th><th>Actions</th></tr>");
        sb.append("        </thead><tbody>");
        if (bookings.isEmpty()) {
            sb.append("        <tr><td colspan=\"8\" class=\"text-center text-muted\">No seat reservations found.</td></tr>");
        } else {
            for (Booking b : bookings) {
                boolean confirmed = "CONFIRMED".equalsIgnoreCase(b.getStatus());
                sb.append("        <tr>");
                sb.append("          <td><strong>#").append(b.getBookingId()).append("</strong></td>");
                sb.append("          <td>").append(WebUtils.escapeHtml(b.getPassengerName() != null ? b.getPassengerName() : "ID: " + b.getPassengerId())).append("</td>");
                sb.append("          <td>").append(WebUtils.escapeHtml(b.getBusName() != null ? b.getBusName() : "Bus #" + b.getBusId()));
                if (b.getSource() != null && b.getDestination() != null) {
                    sb.append("<br><small class=\"text-muted\">").append(WebUtils.escapeHtml(b.getSource())).append(" &rarr; ").append(WebUtils.escapeHtml(b.getDestination())).append("</small>");
                }
                sb.append("          </td>");
                sb.append("          <td><span class=\"badge badge-seat\">Seat ").append(b.getSeatNumber()).append("</span></td>");
                sb.append("          <td>").append(WebUtils.escapeHtml(b.getBookingDate())).append("</td>");
                sb.append("          <td>$").append(String.format("%.2f", b.getFare())).append("</td>");
                sb.append("          <td><span class=\"badge badge-").append(confirmed ? "confirmed" : "cancelled").append("\">").append(WebUtils.escapeHtml(b.getStatus())).append("</span></td>");
                sb.append("          <td class=\"actions-cell\">");
                sb.append("            <a href=\"/tickets?id=").append(b.getBookingId()).append("\" class=\"btn btn-xs btn-primary\">Ticket</a>");
                if (confirmed) {
                    sb.append("            <form action=\"/bookings/cancel\" method=\"POST\" style=\"display:inline;\" onsubmit=\"return confirm('Cancel reservation #").append(b.getBookingId()).append("?');\">");
                    sb.append("              <input type=\"hidden\" name=\"booking_id\" value=\"").append(b.getBookingId()).append("\" />");
                    sb.append("              <button type=\"submit\" class=\"btn btn-xs btn-outline-warning\">Cancel</button>");
                    sb.append("            </form>");
                }
                sb.append("            <form action=\"/bookings/delete\" method=\"POST\" style=\"display:inline;\" onsubmit=\"return confirm('Delete reservation #").append(b.getBookingId()).append(" permanently?');\">");
                sb.append("              <input type=\"hidden\" name=\"booking_id\" value=\"").append(b.getBookingId()).append("\" />");
                sb.append("              <button type=\"submit\" class=\"btn btn-xs btn-outline-danger\">Delete</button>");
                sb.append("            </form>");
                sb.append("          </td>");
                sb.append("        </tr>");
            }
        }
        sb.append("        </tbody>");
        sb.append("      </table>");
        sb.append("    </div>");
        sb.append("  </div>");

        sb.append("</div>");

        return htmlDoc("Reservations", "bookings", sb.toString(), msg, err);
    }

    public static String renderTickets(Booking ticket, List<Booking> allBookings, String msg, String err) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div class=\"page-header\">");
        sb.append("  <div>");
        sb.append("    <h1>Boarding Pass & Tickets</h1>");
        sb.append("    <p class=\"subtitle\">Search, preview, and print official travel boarding passes</p>");
        sb.append("  </div>");
        sb.append("</div>");

        // Search Form
        sb.append("<div class=\"card search-card no-print\">");
        sb.append("  <form action=\"/tickets\" method=\"GET\" class=\"search-form\">");
        sb.append("    <div class=\"search-input-group\">");
        sb.append("      <input type=\"number\" name=\"id\" placeholder=\"Enter Booking ID (e.g. 1001)...\" value=\"").append(ticket != null ? ticket.getBookingId() : "").append("\" required />");
        sb.append("      <button type=\"submit\" class=\"btn btn-primary\">Find Ticket &rarr;</button>");
        sb.append("    </div>");
        sb.append("  </form>");
        sb.append("</div>");

        if (ticket != null) {
            // Render High-end Boarding Pass
            sb.append("<div class=\"boarding-pass-wrapper\">");
            sb.append("  <div class=\"boarding-pass\">");

            // Ticket Header
            sb.append("    <div class=\"ticket-header\">");
            sb.append("      <div class=\"ticket-brand\">");
            sb.append("        <span class=\"ticket-logo\">🚍</span>");
            sb.append("        <div>");
            sb.append("          <h2>SwiftBus Express Lines</h2>");
            sb.append("          <p>Official Travel Boarding Pass</p>");
            sb.append("        </div>");
            sb.append("      </div>");
            sb.append("      <div class=\"ticket-id-tag\">");
            sb.append("        <span class=\"tag-label\">BOOKING REF</span>");
            sb.append("        <span class=\"tag-value\">#").append(ticket.getBookingId()).append("</span>");
            sb.append("      </div>");
            sb.append("    </div>");

            // Ticket Body
            sb.append("    <div class=\"ticket-body\">");

            sb.append("      <div class=\"ticket-route-hero\">");
            sb.append("        <div class=\"route-station\">");
            sb.append("          <span class=\"station-label\">DEPARTURE FROM</span>");
            sb.append("          <span class=\"station-name\">").append(WebUtils.escapeHtml(ticket.getSource() != null ? ticket.getSource() : "Origin")).append("</span>");
            sb.append("        </div>");
            sb.append("        <div class=\"route-arrow\">");
            sb.append("          <span>━━━━ 🚌 ━━━━▶</span>");
            sb.append("          <small>DIRECT ROUTE</small>");
            sb.append("        </div>");
            sb.append("        <div class=\"route-station text-right\">");
            sb.append("          <span class=\"station-label\">DESTINATION</span>");
            sb.append("          <span class=\"station-name\">").append(WebUtils.escapeHtml(ticket.getDestination() != null ? ticket.getDestination() : "Arrival")).append("</span>");
            sb.append("        </div>");
            sb.append("      </div>");

            sb.append("      <div class=\"ticket-details-grid\">");
            sb.append("        <div class=\"ticket-detail-item\">");
            sb.append("          <span class=\"detail-label\">PASSENGER NAME</span>");
            sb.append("          <span class=\"detail-value\">").append(WebUtils.escapeHtml(ticket.getPassengerName() != null ? ticket.getPassengerName() : "ID #" + ticket.getPassengerId())).append("</span>");
            sb.append("        </div>");
            sb.append("        <div class=\"ticket-detail-item\">");
            sb.append("          <span class=\"detail-label\">CONTACT NUMBER</span>");
            sb.append("          <span class=\"detail-value\">").append(WebUtils.escapeHtml(ticket.getPassengerPhone() != null ? ticket.getPassengerPhone() : "N/A")).append("</span>");
            sb.append("        </div>");
            sb.append("        <div class=\"ticket-detail-item\">");
            sb.append("          <span class=\"detail-label\">BUS COACH / MODEL</span>");
            sb.append("          <span class=\"detail-value\">").append(WebUtils.escapeHtml(ticket.getBusName() != null ? ticket.getBusName() : "Bus #" + ticket.getBusId())).append("</span>");
            sb.append("        </div>");
            sb.append("        <div class=\"ticket-detail-item\">");
            sb.append("          <span class=\"detail-label\">DEPARTURE DATE</span>");
            sb.append("          <span class=\"detail-value\">").append(WebUtils.escapeHtml(ticket.getBookingDate())).append("</span>");
            sb.append("        </div>");
            sb.append("      </div>");

            sb.append("    </div>");

            // Ticket Stub / Footer
            sb.append("    <div class=\"ticket-stub\">");
            sb.append("      <div class=\"seat-highlight\">");
            sb.append("        <span class=\"seat-label\">ASSIGNED SEAT</span>");
            sb.append("        <span class=\"seat-number\">").append(ticket.getSeatNumber()).append("</span>");
            sb.append("      </div>");
            sb.append("      <div class=\"ticket-fare-info\">");
            sb.append("        <span class=\"fare-label\">TOTAL FARE</span>");
            sb.append("        <span class=\"fare-amount\">$").append(String.format("%.2f", ticket.getFare())).append("</span>");
            sb.append("        <span class=\"fare-status badge badge-").append("CONFIRMED".equalsIgnoreCase(ticket.getStatus()) ? "confirmed" : "cancelled").append("\">").append(ticket.getStatus()).append("</span>");
            sb.append("      </div>");
            sb.append("      <div class=\"barcode-graphic\">");
            sb.append("        <div class=\"barcode-lines\">||||| |||| || ||||| || |||| ||||| |||| || ||||</div>");
            sb.append("        <small>").append(String.format("SWIFT-%04d-%02d", ticket.getBookingId(), ticket.getSeatNumber())).append("</small>");
            sb.append("      </div>");
            sb.append("    </div>");

            sb.append("  </div>");

            // Print & Download Buttons
            sb.append("  <div class=\"ticket-actions no-print\">");
            sb.append("    <button onclick=\"window.print()\" class=\"btn btn-primary\">🖨️ Print Boarding Pass</button>");
            sb.append("    <a href=\"/tickets/download?id=").append(ticket.getBookingId()).append("\" class=\"btn btn-outline\">💾 Download Text Receipt</a>");
            sb.append("  </div>");

            sb.append("</div>");
        } else {
            // Pick from list of all bookings
            sb.append("<div class=\"card no-print\">");
            sb.append("  <div class=\"card-header\">");
            sb.append("    <h3>Available Tickets</h3>");
            sb.append("    <p class=\"subtitle\">Select any confirmed reservation below to view its boarding pass</p>");
            sb.append("  </div>");
            sb.append("  <div class=\"table-responsive\">");
            sb.append("    <table class=\"table\">");
            sb.append("      <thead>");
            sb.append("        <tr><th>Booking Ref</th><th>Passenger</th><th>Bus Route</th><th>Seat</th><th>Date</th><th>Fare</th><th>Status</th><th>View</th></tr>");
            sb.append("      </thead><tbody>");
            if (allBookings == null || allBookings.isEmpty()) {
                sb.append("      <tr><td colspan=\"8\" class=\"text-center text-muted\">No reservations available.</td></tr>");
            } else {
                for (Booking b : allBookings) {
                    sb.append("      <tr>");
                    sb.append("        <td><strong>#").append(b.getBookingId()).append("</strong></td>");
                    sb.append("        <td>").append(WebUtils.escapeHtml(b.getPassengerName() != null ? b.getPassengerName() : "ID #" + b.getPassengerId())).append("</td>");
                    sb.append("        <td>").append(WebUtils.escapeHtml(b.getSource() != null ? b.getSource() + " &rarr; " + b.getDestination() : "Route #" + b.getBusId())).append("</td>");
                    sb.append("        <td><span class=\"badge badge-seat\">Seat ").append(b.getSeatNumber()).append("</span></td>");
                    sb.append("        <td>").append(WebUtils.escapeHtml(b.getBookingDate())).append("</td>");
                    sb.append("        <td>$").append(String.format("%.2f", b.getFare())).append("</td>");
                    sb.append("        <td><span class=\"badge badge-").append("CONFIRMED".equalsIgnoreCase(b.getStatus()) ? "confirmed" : "cancelled").append("\">").append(WebUtils.escapeHtml(b.getStatus())).append("</span></td>");
                    sb.append("        <td><a href=\"/tickets?id=").append(b.getBookingId()).append("\" class=\"btn btn-xs btn-primary\">View Pass</a></td>");
                    sb.append("      </tr>");
                }
            }
            sb.append("      </tbody>");
            sb.append("    </table>");
            sb.append("  </div>");
            sb.append("</div>");
        }

        return htmlDoc("Boarding Pass", "tickets", sb.toString(), msg, err);
    }

    public static String getCss() {
        return ":root {" +
                "  --bg-primary: #0a0f1d;" +
                "  --bg-secondary: #111827;" +
                "  --bg-card: #162032;" +
                "  --bg-card-hover: #1c2940;" +
                "  --border-color: #24324a;" +
                "  --border-focus: #6366f1;" +
                "  --text-main: #f8fafc;" +
                "  --text-muted: #94a3b8;" +
                "  --primary: #6366f1;" +
                "  --primary-hover: #4f46e5;" +
                "  --primary-gradient: linear-gradient(135deg, #6366f1 0%, #3b82f6 50%, #06b6d4 100%);" +
                "  --success: #10b981;" +
                "  --warning: #f59e0b;" +
                "  --danger: #ef4444;" +
                "  --radius: 12px;" +
                "  --shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.4), 0 8px 10px -6px rgba(0, 0, 0, 0.3);" +
                "}" +
                "* { margin: 0; padding: 0; box-sizing: border-box; }" +
                "body {" +
                "  font-family: 'Plus Jakarta Sans', -apple-system, BlinkMacSystemFont, sans-serif;" +
                "  background-color: var(--bg-primary);" +
                "  color: var(--text-main);" +
                "  line-height: 1.6;" +
                "  min-height: 100vh;" +
                "  display: flex;" +
                "  flex-direction: column;" +
                "}" +
                "a { color: var(--primary); text-decoration: none; transition: all 0.2s; }" +
                "a:hover { color: #818cf8; }" +
                "" +
                "/* Navbar */" +
                ".navbar {" +
                "  background: rgba(17, 24, 39, 0.85);" +
                "  backdrop-filter: blur(12px);" +
                "  border-bottom: 1px solid var(--border-color);" +
                "  position: sticky;" +
                "  top: 0;" +
                "  z-index: 100;" +
                "}" +
                ".nav-container {" +
                "  max-width: 1280px;" +
                "  margin: 0 auto;" +
                "  padding: 0.85rem 1.5rem;" +
                "  display: flex;" +
                "  align-items: center;" +
                "  justify-content: space-between;" +
                "  gap: 1.5rem;" +
                "}" +
                ".brand {" +
                "  display: flex;" +
                "  align-items: center;" +
                "  gap: 0.6rem;" +
                "  font-size: 1.35rem;" +
                "  font-weight: 800;" +
                "  color: #fff;" +
                "}" +
                ".brand-icon { font-size: 1.6rem; }" +
                ".brand-badge {" +
                "  font-size: 0.65rem;" +
                "  text-transform: uppercase;" +
                "  background: var(--primary-gradient);" +
                "  color: white;" +
                "  padding: 0.2rem 0.5rem;" +
                "  border-radius: 6px;" +
                "  letter-spacing: 0.05em;" +
                "}" +
                ".nav-links {" +
                "  display: flex;" +
                "  gap: 0.5rem;" +
                "}" +
                ".nav-link {" +
                "  padding: 0.5rem 0.9rem;" +
                "  border-radius: 8px;" +
                "  font-size: 0.9rem;" +
                "  font-weight: 600;" +
                "  color: var(--text-muted);" +
                "  transition: all 0.2s;" +
                "}" +
                ".nav-link:hover {" +
                "  color: #fff;" +
                "  background: rgba(255, 255, 255, 0.05);" +
                "}" +
                ".nav-link.active {" +
                "  color: #fff;" +
                "  background: rgba(99, 102, 241, 0.2);" +
                "  border: 1px solid rgba(99, 102, 241, 0.4);" +
                "}" +
                ".nav-user {" +
                "  display: flex;" +
                "  align-items: center;" +
                "  gap: 0.75rem;" +
                "}" +
                ".user-badge {" +
                "  font-size: 0.85rem;" +
                "  background: rgba(255, 255, 255, 0.06);" +
                "  padding: 0.35rem 0.75rem;" +
                "  border-radius: 20px;" +
                "  border: 1px solid var(--border-color);" +
                "}" +
                "" +
                "/* Content Layout */" +
                ".main-content {" +
                "  flex: 1;" +
                "  max-width: 1280px;" +
                "  width: 100%;" +
                "  margin: 0 auto;" +
                "  padding: 2rem 1.5rem;" +
                "}" +
                ".page-header {" +
                "  display: flex;" +
                "  justify-content: space-between;" +
                "  align-items: flex-end;" +
                "  margin-bottom: 2rem;" +
                "  flex-wrap: wrap;" +
                "  gap: 1rem;" +
                "}" +
                ".page-header h1 { font-size: 1.85rem; font-weight: 800; }" +
                ".subtitle { color: var(--text-muted); font-size: 0.95rem; margin-top: 0.25rem; }" +
                ".header-actions { display: flex; gap: 0.75rem; }" +
                "" +
                "/* Stats Grid */" +
                ".stats-grid {" +
                "  display: grid;" +
                "  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));" +
                "  gap: 1.25rem;" +
                "  margin-bottom: 2rem;" +
                "}" +
                ".stat-card {" +
                "  background: var(--bg-card);" +
                "  border: 1px solid var(--border-color);" +
                "  border-radius: var(--radius);" +
                "  padding: 1.5rem;" +
                "  display: flex;" +
                "  align-items: center;" +
                "  gap: 1.25rem;" +
                "  transition: transform 0.2s, border-color 0.2s;" +
                "}" +
                ".stat-card:hover {" +
                "  transform: translateY(-3px);" +
                "  border-color: rgba(99, 102, 241, 0.4);" +
                "}" +
                ".stat-icon {" +
                "  width: 56px;" +
                "  height: 56px;" +
                "  border-radius: 12px;" +
                "  display: flex;" +
                "  align-items: center;" +
                "  justify-content: center;" +
                "  font-size: 1.75rem;" +
                "  background: rgba(255, 255, 255, 0.05);" +
                "}" +
                ".stat-buses .stat-icon { background: rgba(99, 102, 241, 0.15); }" +
                ".stat-passengers .stat-icon { background: rgba(6, 182, 212, 0.15); }" +
                ".stat-bookings .stat-icon { background: rgba(245, 158, 11, 0.15); }" +
                ".stat-revenue .stat-icon { background: rgba(16, 185, 129, 0.15); }" +
                ".stat-value {" +
                "  display: block;" +
                "  font-size: 1.85rem;" +
                "  font-weight: 800;" +
                "  line-height: 1.2;" +
                "}" +
                ".stat-label {" +
                "  display: block;" +
                "  color: var(--text-muted);" +
                "  font-size: 0.85rem;" +
                "  font-weight: 500;" +
                "  margin-top: 0.2rem;" +
                "}" +
                "" +
                "/* Quick Actions */" +
                ".section-title { margin-bottom: 1rem; }" +
                ".quick-actions-grid {" +
                "  display: grid;" +
                "  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));" +
                "  gap: 1.25rem;" +
                "  margin-bottom: 2rem;" +
                "}" +
                ".quick-card {" +
                "  background: var(--bg-card);" +
                "  border: 1px solid var(--border-color);" +
                "  border-radius: var(--radius);" +
                "  padding: 1.4rem;" +
                "  transition: all 0.2s;" +
                "  display: block;" +
                "}" +
                ".quick-card:hover {" +
                "  background: var(--bg-card-hover);" +
                "  border-color: var(--primary);" +
                "  transform: translateY(-2px);" +
                "}" +
                ".quick-icon { font-size: 2rem; display: block; margin-bottom: 0.75rem; }" +
                ".quick-card h4 { font-size: 1.05rem; margin-bottom: 0.35rem; color: #fff; }" +
                ".quick-card p { font-size: 0.8rem; color: var(--text-muted); line-height: 1.4; }" +
                "" +
                "/* Cards & Tables */" +
                ".card {" +
                "  background: var(--bg-card);" +
                "  border: 1px solid var(--border-color);" +
                "  border-radius: var(--radius);" +
                "  box-shadow: var(--shadow);" +
                "  overflow: hidden;" +
                "  margin-bottom: 2rem;" +
                "}" +
                ".card-header {" +
                "  padding: 1.25rem 1.5rem;" +
                "  border-bottom: 1px solid var(--border-color);" +
                "  display: flex;" +
                "  justify-content: space-between;" +
                "  align-items: center;" +
                "}" +
                ".card-header h3 { font-size: 1.15rem; font-weight: 700; }" +
                ".table-responsive { width: 100%; overflow-x: auto; }" +
                ".table {" +
                "  width: 100%;" +
                "  border-collapse: collapse;" +
                "  text-align: left;" +
                "  font-size: 0.9rem;" +
                "}" +
                ".table th {" +
                "  padding: 0.9rem 1.25rem;" +
                "  background: rgba(255, 255, 255, 0.02);" +
                "  color: var(--text-muted);" +
                "  font-weight: 600;" +
                "  border-bottom: 1px solid var(--border-color);" +
                "}" +
                ".table td {" +
                "  padding: 1rem 1.25rem;" +
                "  border-bottom: 1px solid rgba(255, 255, 255, 0.04);" +
                "}" +
                ".table tr:last-child td { border-bottom: none; }" +
                ".table tr:hover td { background: rgba(255, 255, 255, 0.02); }" +
                ".actions-cell { white-space: nowrap; }" +
                "" +
                "/* Grid Form Layout */" +
                ".grid-form-table {" +
                "  display: grid;" +
                "  grid-template-columns: 360px 1fr;" +
                "  gap: 1.5rem;" +
                "  align-items: start;" +
                "}" +
                "@media (max-width: 900px) {" +
                "  .grid-form-table { grid-template-columns: 1fr; }" +
                "}" +
                ".form-card { padding: 1.5rem; }" +
                ".form-group { margin-bottom: 1.1rem; }" +
                ".form-group label {" +
                "  display: block;" +
                "  font-size: 0.85rem;" +
                "  font-weight: 600;" +
                "  margin-bottom: 0.4rem;" +
                "  color: var(--text-muted);" +
                "}" +
                ".form-group input, .form-group select, .form-group textarea {" +
                "  width: 100%;" +
                "  padding: 0.65rem 0.9rem;" +
                "  background: var(--bg-secondary);" +
                "  border: 1px solid var(--border-color);" +
                "  border-radius: 8px;" +
                "  color: #fff;" +
                "  font-size: 0.9rem;" +
                "  font-family: inherit;" +
                "  transition: all 0.2s;" +
                "}" +
                ".form-group input:focus, .form-group select:focus {" +
                "  outline: none;" +
                "  border-color: var(--primary);" +
                "  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.25);" +
                "}" +
                ".form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 0.75rem; }" +
                "" +
                "/* Buttons */" +
                ".btn {" +
                "  display: inline-flex;" +
                "  align-items: center;" +
                "  justify-content: center;" +
                "  gap: 0.4rem;" +
                "  font-weight: 600;" +
                "  font-size: 0.9rem;" +
                "  padding: 0.65rem 1.25rem;" +
                "  border-radius: 8px;" +
                "  border: none;" +
                "  cursor: pointer;" +
                "  transition: all 0.2s;" +
                "}" +
                ".btn-primary {" +
                "  background: var(--primary-gradient);" +
                "  color: #fff;" +
                "  box-shadow: 0 4px 14px 0 rgba(99, 102, 241, 0.35);" +
                "}" +
                ".btn-primary:hover {" +
                "  opacity: 0.95;" +
                "  transform: translateY(-1px);" +
                "  box-shadow: 0 6px 20px 0 rgba(99, 102, 241, 0.45);" +
                "}" +
                ".btn-secondary { background: #374151; color: #fff; }" +
                ".btn-secondary:hover { background: #4b5563; }" +
                ".btn-outline { background: transparent; border: 1px solid var(--border-color); color: var(--text-main); }" +
                ".btn-outline:hover { background: rgba(255, 255, 255, 0.05); border-color: #fff; }" +
                ".btn-outline-danger { background: transparent; border: 1px solid rgba(239, 68, 68, 0.4); color: #fca5a5; }" +
                ".btn-outline-danger:hover { background: rgba(239, 68, 68, 0.15); border-color: var(--danger); color: #fff; }" +
                ".btn-outline-warning { background: transparent; border: 1px solid rgba(245, 158, 11, 0.4); color: #fcd34d; }" +
                ".btn-outline-warning:hover { background: rgba(245, 158, 11, 0.15); border-color: var(--warning); color: #fff; }" +
                ".btn-block { width: 100%; }" +
                ".btn-sm { padding: 0.4rem 0.8rem; font-size: 0.8rem; }" +
                ".btn-xs { padding: 0.25rem 0.55rem; font-size: 0.75rem; border-radius: 6px; }" +
                "" +
                "/* Badges */" +
                ".badge {" +
                "  display: inline-block;" +
                "  padding: 0.25rem 0.65rem;" +
                "  border-radius: 20px;" +
                "  font-size: 0.75rem;" +
                "  font-weight: 700;" +
                "  letter-spacing: 0.02em;" +
                "}" +
                ".badge-confirmed { background: rgba(16, 185, 129, 0.15); color: #34d399; border: 1px solid rgba(16, 185, 129, 0.3); }" +
                ".badge-cancelled { background: rgba(239, 68, 68, 0.15); color: #f87171; border: 1px solid rgba(239, 68, 68, 0.3); }" +
                ".badge-seat { background: rgba(99, 102, 241, 0.15); color: #a5b4fc; border: 1px solid rgba(99, 102, 241, 0.3); }" +
                ".badge-route { background: rgba(6, 182, 212, 0.15); color: #67e8f9; border: 1px solid rgba(6, 182, 212, 0.3); }" +
                ".badge-gender { background: rgba(255, 255, 255, 0.06); color: #e2e8f0; }" +
                ".badge-occupied { background: #451a1a; color: #fca5a5; border: 1px solid #7f1d1d; margin-right: 0.3rem; }" +
                "" +
                "/* Alerts */" +
                ".alert {" +
                "  padding: 0.9rem 1.25rem;" +
                "  border-radius: var(--radius);" +
                "  margin-bottom: 1.5rem;" +
                "  font-size: 0.9rem;" +
                "  font-weight: 500;" +
                "}" +
                ".alert-success { background: rgba(16, 185, 129, 0.12); color: #34d399; border: 1px solid rgba(16, 185, 129, 0.3); }" +
                ".alert-danger { background: rgba(239, 68, 68, 0.12); color: #f87171; border: 1px solid rgba(239, 68, 68, 0.3); }" +
                "" +
                "/* Search Bar */" +
                ".search-card { padding: 1.25rem 1.5rem; }" +
                ".search-input-group { display: flex; gap: 0.75rem; }" +
                ".search-input-group input {" +
                "  flex: 1;" +
                "  padding: 0.75rem 1rem;" +
                "  background: var(--bg-secondary);" +
                "  border: 1px solid var(--border-color);" +
                "  border-radius: 8px;" +
                "  color: #fff;" +
                "  font-size: 1rem;" +
                "}" +
                "" +
                "/* Boarding Pass */" +
                ".boarding-pass-wrapper {" +
                "  max-width: 760px;" +
                "  margin: 1.5rem auto;" +
                "}" +
                ".boarding-pass {" +
                "  background: #ffffff;" +
                "  color: #0f172a;" +
                "  border-radius: 16px;" +
                "  overflow: hidden;" +
                "  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.6);" +
                "}" +
                ".ticket-header {" +
                "  background: linear-gradient(135deg, #1e1b4b 0%, #312e81 100%);" +
                "  color: #fff;" +
                "  padding: 1.5rem 2rem;" +
                "  display: flex;" +
                "  justify-content: space-between;" +
                "  align-items: center;" +
                "}" +
                ".ticket-brand { display: flex; align-items: center; gap: 1rem; }" +
                ".ticket-logo { font-size: 2.2rem; }" +
                ".ticket-brand h2 { font-size: 1.35rem; font-weight: 800; }" +
                ".ticket-brand p { font-size: 0.8rem; color: #a5b4fc; }" +
                ".ticket-id-tag { text-align: right; }" +
                ".tag-label { display: block; font-size: 0.7rem; color: #94a3b8; font-weight: 700; }" +
                ".tag-value { font-size: 1.4rem; font-weight: 800; color: #38bdf8; font-family: 'JetBrains Mono', monospace; }" +
                ".ticket-body { padding: 2rem; }" +
                ".ticket-route-hero {" +
                "  display: flex;" +
                "  justify-content: space-between;" +
                "  align-items: center;" +
                "  border-bottom: 2px dashed #e2e8f0;" +
                "  padding-bottom: 1.5rem;" +
                "  margin-bottom: 1.5rem;" +
                "}" +
                ".route-station .station-label { display: block; font-size: 0.75rem; color: #64748b; font-weight: 700; }" +
                ".route-station .station-name { font-size: 1.6rem; font-weight: 800; color: #0f172a; }" +
                ".route-arrow { text-align: center; color: #6366f1; font-weight: 800; font-size: 1.1rem; }" +
                ".route-arrow small { display: block; font-size: 0.65rem; color: #94a3b8; font-weight: 700; }" +
                ".ticket-details-grid {" +
                "  display: grid;" +
                "  grid-template-columns: repeat(2, 1fr);" +
                "  gap: 1.25rem;" +
                "}" +
                ".ticket-detail-item .detail-label { display: block; font-size: 0.7rem; color: #64748b; font-weight: 700; }" +
                ".ticket-detail-item .detail-value { font-size: 1.05rem; font-weight: 700; color: #0f172a; margin-top: 0.15rem; }" +
                ".ticket-stub {" +
                "  background: #f8fafc;" +
                "  border-top: 2px dashed #cbd5e1;" +
                "  padding: 1.5rem 2rem;" +
                "  display: flex;" +
                "  justify-content: space-between;" +
                "  align-items: center;" +
                "}" +
                ".seat-highlight .seat-label { display: block; font-size: 0.7rem; color: #64748b; font-weight: 700; }" +
                ".seat-highlight .seat-number {" +
                "  font-size: 2.2rem;" +
                "  font-weight: 900;" +
                "  color: #4f46e5;" +
                "  font-family: 'JetBrains Mono', monospace;" +
                "}" +
                ".ticket-fare-info .fare-label { display: block; font-size: 0.7rem; color: #64748b; font-weight: 700; }" +
                ".ticket-fare-info .fare-amount { font-size: 1.5rem; font-weight: 800; color: #059669; }" +
                ".barcode-graphic { text-align: center; font-family: 'JetBrains Mono', monospace; color: #334155; }" +
                ".barcode-lines { font-size: 1.2rem; letter-spacing: 2px; font-weight: 700; }" +
                ".barcode-graphic small { font-size: 0.7rem; color: #64748b; }" +
                ".ticket-actions {" +
                "  margin-top: 1.5rem;" +
                "  display: flex;" +
                "  justify-content: center;" +
                "  gap: 1rem;" +
                "}" +
                "" +
                "/* Login Page */" +
                ".login-body {" +
                "  background: radial-gradient(circle at 50% 20%, #1e1b4b 0%, #0a0f1d 70%);" +
                "  display: flex;" +
                "  align-items: center;" +
                "  justify-content: center;" +
                "  padding: 2rem;" +
                "}" +
                ".login-card {" +
                "  width: 100%;" +
                "  max-width: 420px;" +
                "  background: rgba(22, 32, 50, 0.75);" +
                "  backdrop-filter: blur(20px);" +
                "  border: 1px solid rgba(255, 255, 255, 0.1);" +
                "  border-radius: 20px;" +
                "  padding: 2.5rem;" +
                "  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.7);" +
                "}" +
                ".login-header {" +
                "  text-align: center;" +
                "  margin-bottom: 2rem;" +
                "}" +
                ".login-logo { font-size: 3rem; margin-bottom: 0.5rem; }" +
                ".login-header h1 { font-size: 1.6rem; font-weight: 800; }" +
                ".login-header p { font-size: 0.85rem; color: var(--text-muted); margin-top: 0.25rem; }" +
                ".login-footer {" +
                "  text-align: center;" +
                "  margin-top: 1.5rem;" +
                "  color: var(--text-muted);" +
                "}" +
                ".login-footer code { background: rgba(255, 255, 255, 0.1); padding: 0.15rem 0.4rem; border-radius: 4px; color: #38bdf8; }" +
                "" +
                "/* Footer */" +
                ".footer {" +
                "  border-top: 1px solid var(--border-color);" +
                "  padding: 1.5rem;" +
                "  text-align: center;" +
                "  font-size: 0.8rem;" +
                "  color: var(--text-muted);" +
                "  margin-top: auto;" +
                "}" +
                "" +
                "/* Print Media */" +
                "@media print {" +
                "  body { background: #fff !important; color: #000 !important; }" +
                "  .navbar, .footer, .no-print, .page-header { display: none !important; }" +
                "  .main-content { padding: 0 !important; margin: 0 !important; max-width: 100% !important; }" +
                "  .boarding-pass-wrapper { margin: 0 !important; max-width: 100% !important; }" +
                "  .boarding-pass { box-shadow: none !important; border: 1px solid #000 !important; }" +
                "}";
    }
}
