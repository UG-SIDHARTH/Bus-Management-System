package web;

import model.Booking;
import model.Bus;
import model.Passenger;
import model.User;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WebViews {

    private static String navbar(String active, SessionManager.Session session) {
        boolean isAuth = session != null && session.isAuthenticated();
        boolean isAdmin = session != null && session.isAdmin();

        StringBuilder sb = new StringBuilder();
        sb.append("<nav class=\"navbar\">");
        sb.append("  <div class=\"nav-container\">");
        sb.append("    <a href=\"/\" class=\"brand\">");
        sb.append("      <span class=\"brand-icon\">🚍</span>");
        sb.append("      <span class=\"brand-text\">SwiftBus <span class=\"brand-badge\">").append(isAdmin ? "Admin" : "Portal").append("</span></span>");
        sb.append("    </a>");
        sb.append("    <div class=\"nav-links\">");
        sb.append("      <a href=\"/\" class=\"nav-link ").append("dashboard".equals(active) ? "active" : "").append("\">📊 Dashboard</a>");
        sb.append("      <a href=\"/buses\" class=\"nav-link ").append("buses".equals(active) ? "active" : "").append("\">🚌 Routes & Buses</a>");
        
        if (isAuth) {
            if (isAdmin) {
                sb.append("      <a href=\"/admin/users\" class=\"nav-link ").append("admin-users".equals(active) ? "active" : "").append("\">👑 Users & Activity</a>");
                sb.append("      <a href=\"/passengers\" class=\"nav-link ").append("passengers".equals(active) ? "active" : "").append("\">👥 Passengers</a>");
            }
            sb.append("      <a href=\"/bookings\" class=\"nav-link ").append("bookings".equals(active) ? "active" : "").append("\">🎟️ ").append(isAdmin ? "All Bookings" : "Book Seats").append("</a>");
            sb.append("      <a href=\"/tickets\" class=\"nav-link ").append("tickets".equals(active) ? "active" : "").append("\">🎫 Boarding Passes</a>");
        }
        sb.append("    </div>");

        sb.append("    <div class=\"nav-user\">");
        if (isAuth) {
            sb.append("      <span class=\"user-badge\">👤 ").append(WebUtils.escapeHtml(session.getUsername()));
            if (isAdmin) {
                sb.append(" <span class=\"role-tag\">ADMIN</span>");
            }
            sb.append("      </span>");
            sb.append("      <a href=\"/logout\" class=\"btn btn-sm btn-outline-danger\">Logout</a>");
        } else {
            sb.append("      <a href=\"/login\" class=\"btn btn-sm btn-outline\">Sign In</a>");
            sb.append("      <a href=\"/register\" class=\"btn btn-sm btn-primary\">Register</a>");
        }
        sb.append("    </div>");
        sb.append("  </div>");
        sb.append("</nav>");
        return sb.toString();
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

    private static String htmlDoc(String title, String activeNav, SessionManager.Session session, String content, String msg, String err) {
        return "<!DOCTYPE html>" +
                "<html lang=\"en\">" +
                "<head>" +
                "  <meta charset=\"UTF-8\" />" +
                "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />" +
                "  <title>" + WebUtils.escapeHtml(title) + " - SwiftBus</title>" +
                "  <link rel=\"stylesheet\" href=\"/static/style.css\" />" +
                "</head>" +
                "<body>" +
                navbar(activeNav, session) +
                "<main class=\"main-content\">" +
                alerts(msg, err) +
                content +
                "</main>" +
                "<footer class=\"footer\">" +
                "  <div class=\"footer-container\">" +
                "    <div class=\"footer-brand\">" +
                "      <span class=\"brand-icon\">🚍</span>" +
                "      <strong>SwiftBus Management &amp; Reservation System</strong>" +
                "    </div>" +
                "    <div class=\"footer-nav\">" +
                "      <a href=\"/\">Dashboard</a> &bull; " +
                "      <a href=\"/buses\">Routes &amp; Buses</a> &bull; " +
                "      <a href=\"/register\">Register Account</a> &bull; " +
                "      <a href=\"/login\">Sign In</a> &bull; " +
                "      <a href=\"/bookings\">Reservations</a>" +
                "    </div>" +
                "    <p class=\"footer-copy\">&copy; 2026 SwiftBus Inc. All Rights Reserved. &bull; Pure Java Native Architecture &bull; Port 6969</p>" +
                "  </div>" +
                "</footer>" +
                "</body>" +
                "</html>";
    }

    // -------------------------------------------------------------------------
    // LOGIN & REGISTER VIEWS
    // -------------------------------------------------------------------------

    public static String renderLogin(String err, String msg, String nextUrl, String csrfToken) {
        boolean hasError = err != null && !err.isEmpty();
        return "<!DOCTYPE html>" +
                "<html lang=\"en\">" +
                "<head>" +
                "  <meta charset=\"UTF-8\" />" +
                "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />" +
                "  <title>Sign In - SwiftBus Portal</title>" +
                "  <link rel=\"stylesheet\" href=\"/static/style.css\" />" +
                "</head>" +
                "<body class=\"login-body\">" +
                "  <div class=\"login-card" + (hasError ? " shake-anim" : "") + "\">" +
                "    <div class=\"login-header\">" +
                "      <div class=\"login-logo\">🚍</div>" +
                "      <h1>Sign In to SwiftBus</h1>" +
                "      <p>Bus Management & Reservation System</p>" +
                "    </div>" +
                (msg != null && !msg.isEmpty() ? "<div class=\"alert alert-success\">✅ " + WebUtils.escapeHtml(msg) + "</div>" : "") +
                (hasError ? "<div class=\"alert alert-danger\">⚠️ " + WebUtils.escapeHtml(err) + "</div>" : "") +
                "    <form action=\"/do-login\" method=\"POST\" class=\"login-form\">" +
                "      <input type=\"hidden\" name=\"csrf_token\" value=\"" + WebUtils.escapeHtml(csrfToken) + "\" />" +
                "      <input type=\"hidden\" name=\"next\" value=\"" + WebUtils.escapeHtml(nextUrl != null ? nextUrl : "") + "\" />" +
                "      <div class=\"form-group\">" +
                "        <label for=\"username\">Username or Email</label>" +
                "        <input type=\"text\" id=\"username\" name=\"username\" placeholder=\"your username or email\" required autofocus />" +
                "      </div>" +
                "      <div class=\"form-group\">" +
                "        <label for=\"password\">Password</label>" +
                "        <input type=\"password\" id=\"password\" name=\"password\" placeholder=\"&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;\" required />" +
                "      </div>" +
                "      <button type=\"submit\" class=\"btn btn-primary btn-block\">Sign In &rarr;</button>" +
                "    </form>" +
                "    <div class=\"auth-alt-box\">" +
                "      <span class=\"auth-alt-text\">Don't have an account yet?</span>" +
                "      <a href=\"/register\" class=\"btn btn-outline-primary btn-block\" style=\"margin-top:0.6rem;\">➕ Create New Account / Register &rarr;</a>" +
                "    </div>" +
                "    <div class=\"login-footer\">" +
                "      <p class=\"login-copyright\">&copy; 2026 SwiftBus Inc. All Rights Reserved.</p>" +
                "    </div>" +
                "  </div>" +
                "</body>" +
                "</html>";
    }

    public static String renderRegister(String err, String csrfToken, String defaultUser, String defaultEmail) {
        boolean hasError = err != null && !err.isEmpty();
        return "<!DOCTYPE html>" +
                "<html lang=\"en\">" +
                "<head>" +
                "  <meta charset=\"UTF-8\" />" +
                "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />" +
                "  <title>Register Traveler Account - SwiftBus</title>" +
                "  <link rel=\"stylesheet\" href=\"/static/style.css\" />" +
                "</head>" +
                "<body class=\"login-body\">" +
                "  <div class=\"login-card" + (hasError ? " shake-anim" : "") + "\">" +
                "    <div class=\"login-header\">" +
                "      <div class=\"login-logo\">📝</div>" +
                "      <h1>Create Traveler Account</h1>" +
                "      <p>Reserve seats, track bookings, and download passes</p>" +
                "    </div>" +
                (hasError ? "<div class=\"alert alert-danger\">⚠️ " + WebUtils.escapeHtml(err) + "</div>" : "") +
                "    <form action=\"/register\" method=\"POST\" class=\"login-form\" onsubmit=\"return validateRegister();\">" +
                "      <input type=\"hidden\" name=\"csrf_token\" value=\"" + WebUtils.escapeHtml(csrfToken) + "\" />" +
                "      <div class=\"form-group\">" +
                "        <label for=\"username\">Username (3-30 letters/numbers)</label>" +
                "        <input type=\"text\" id=\"username\" name=\"username\" value=\"" + WebUtils.escapeHtml(defaultUser != null ? defaultUser : "") + "\" placeholder=\"e.g. john_doe\" required autofocus />" +
                "      </div>" +
                "      <div class=\"form-group\">" +
                "        <label for=\"email\">Email Address</label>" +
                "        <input type=\"email\" id=\"email\" name=\"email\" value=\"" + WebUtils.escapeHtml(defaultEmail != null ? defaultEmail : "") + "\" placeholder=\"e.g. john@example.com\" required />" +
                "      </div>" +
                "      <div class=\"form-group\">" +
                "        <label for=\"password\">Password (Min 8 chars + special character)</label>" +
                "        <input type=\"password\" id=\"password\" name=\"password\" placeholder=\"e.g. Secret@2026!\" required oninput=\"updateStrength();\" />" +
                "        <div id=\"strengthContainer\" class=\"strength-meter-container\" style=\"display:none;\">" +
                "          <div class=\"strength-meter\"><div id=\"strengthFill\" class=\"strength-fill\"></div></div>" +
                "          <div class=\"strength-label\">Password Rating: <span id=\"strengthText\"></span></div>" +
                "          <ul class=\"strength-rules\">" +
                "            <li id=\"rule-len\" class=\"rule-item\"><span class=\"rule-icon\">○</span> At least 8 characters</li>" +
                "            <li id=\"rule-spec\" class=\"rule-item\"><span class=\"rule-icon\">○</span> At least 1 special character (!@#$%^&*)</li>" +
                "            <li id=\"rule-bonus\" class=\"rule-item\"><span class=\"rule-icon\">○</span> Mixed case & numbers (Recommended)</li>" +
                "          </ul>" +
                "        </div>" +
                "      </div>" +
                "      <div class=\"form-group\">" +
                "        <label for=\"confirm_password\">Confirm Password</label>" +
                "        <input type=\"password\" id=\"confirm_password\" name=\"confirm_password\" placeholder=\"Repeat password\" required oninput=\"checkMatch();\" />" +
                "        <small id=\"passMismatch\" class=\"text-danger\" style=\"display:none;\">Passwords do not match.</small>" +
                "      </div>" +
                "      <button type=\"submit\" class=\"btn btn-primary btn-block\">Complete Registration &rarr;</button>" +
                "    </form>" +
                "    <div class=\"auth-alt-box\">" +
                "      <span class=\"auth-alt-text\">Already have an account?</span>" +
                "      <a href=\"/login\" class=\"btn btn-outline btn-block\" style=\"margin-top:0.6rem;\">Sign In to Existing Account &rarr;</a>" +
                "    </div>" +
                "    <div class=\"login-footer\">" +
                "      <p class=\"login-copyright\">&copy; 2026 SwiftBus Inc. All Rights Reserved.</p>" +
                "    </div>" +
                "  </div>" +
                "  <script>" +
                "    function updateStrength() {" +
                "      var p = document.getElementById('password').value;" +
                "      var c = document.getElementById('strengthContainer');" +
                "      if (!p || p.length === 0) { c.style.display = 'none'; return; }" +
                "      c.style.display = 'block';" +
                "      var hasLen = p.length >= 8;" +
                "      var hasSpec = /[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?~`]/.test(p);" +
                "      var hasUpper = /[A-Z]/.test(p);" +
                "      var hasLower = /[a-z]/.test(p);" +
                "      var hasNum = /[0-9]/.test(p);" +
                "      var rLen = document.getElementById('rule-len');" +
                "      var rSpec = document.getElementById('rule-spec');" +
                "      var rBonus = document.getElementById('rule-bonus');" +
                "      rLen.className = 'rule-item ' + (hasLen ? 'valid' : 'invalid');" +
                "      rLen.querySelector('.rule-icon').textContent = hasLen ? '✓' : '○';" +
                "      rSpec.className = 'rule-item ' + (hasSpec ? 'valid' : 'invalid');" +
                "      rSpec.querySelector('.rule-icon').textContent = hasSpec ? '✓' : '○';" +
                "      var isBonus = (hasUpper && hasLower && hasNum && p.length >= 10);" +
                "      rBonus.className = 'rule-item ' + (isBonus ? 'valid' : 'invalid');" +
                "      rBonus.querySelector('.rule-icon').textContent = isBonus ? '✓' : '○';" +
                "      var textSpan = document.getElementById('strengthText');" +
                "      var fill = document.getElementById('strengthFill');" +
                "      if (!hasLen || !hasSpec) {" +
                "        textSpan.textContent = 'Weak';" +
                "        textSpan.className = 'strength-badge weak';" +
                "        fill.className = 'strength-fill weak';" +
                "      } else if (isBonus) {" +
                "        textSpan.textContent = 'Best';" +
                "        textSpan.className = 'strength-badge best';" +
                "        fill.className = 'strength-fill best';" +
                "      } else {" +
                "        textSpan.textContent = 'Better';" +
                "        textSpan.className = 'strength-badge better';" +
                "        fill.className = 'strength-fill better';" +
                "      }" +
                "      checkMatch();" +
                "    }" +
                "    function checkMatch() {" +
                "      var p1 = document.getElementById('password').value;" +
                "      var p2 = document.getElementById('confirm_password').value;" +
                "      var msg = document.getElementById('passMismatch');" +
                "      if (p2.length > 0 && p1 !== p2) { msg.style.display = 'block'; } else { msg.style.display = 'none'; }" +
                "    }" +
                "    function validateRegister() {" +
                "      var p1 = document.getElementById('password').value;" +
                "      var p2 = document.getElementById('confirm_password').value;" +
                "      var hasLen = p1.length >= 8;" +
                "      var hasSpec = /[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?~`]/.test(p1);" +
                "      var card = document.querySelector('.login-card');" +
                "      if (!hasLen || !hasSpec) {" +
                "        alert('Password must be at least 8 characters long and contain at least one special character (!@#$%^&* etc.)');" +
                "        document.getElementById('password').focus();" +
                "        card.classList.remove('shake-anim');" +
                "        void card.offsetWidth;" +
                "        card.classList.add('shake-anim');" +
                "        return false;" +
                "      }" +
                "      if (p1 !== p2) {" +
                "        document.getElementById('passMismatch').style.display = 'block';" +
                "        document.getElementById('confirm_password').focus();" +
                "        card.classList.remove('shake-anim');" +
                "        void card.offsetWidth;" +
                "        card.classList.add('shake-anim');" +
                "        return false;" +
                "      }" +
                "      return true;" +
                "    }" +
                "  </script>" +
                "</body>" +
                "</html>";
    }

    // -------------------------------------------------------------------------
    // DASHBOARD VIEW
    // -------------------------------------------------------------------------

    public static String renderDashboard(SessionManager.Session session, int totalBuses, int totalPassengers, int totalBookings, double totalRevenue, List<Booking> recentBookings, String msg, String err) {
        StringBuilder sb = new StringBuilder();
        boolean isAdmin = session != null && session.isAdmin();

        sb.append("<div class=\"page-header\">");
        sb.append("  <div>");
        sb.append("    <h1>").append(isAdmin ? "System Dashboard" : "Travel Portal Dashboard").append("</h1>");
        sb.append("    <p class=\"subtitle\">").append(isAdmin ? "Complete overview of bus fleet, passengers, and financial stats" : "Browse available bus routes, seat availability, and manage your tickets").append("</p>");
        sb.append("  </div>");
        sb.append("  <div class=\"header-actions\">");
        sb.append("    <a href=\"/bookings\" class=\"btn btn-primary\">+ Reserve Seats</a>");
        if (isAdmin) {
            sb.append("    <a href=\"/buses\" class=\"btn btn-secondary\">+ Manage Buses</a>");
            sb.append("    <a href=\"/admin/users\" class=\"btn btn-outline\">👑 View All Users</a>");
        }
        sb.append("  </div>");
        sb.append("</div>");

        // Guest Registration Callout Banner
        if (session == null || !session.isAuthenticated()) {
            sb.append("<div class=\"guest-banner\">");
            sb.append("  <div class=\"guest-banner-info\">");
            sb.append("    <h3>👋 New to SwiftBus? Create an Account to Book Seats</h3>");
            sb.append("    <p>Register a free traveler account in seconds to pick your favorite seat on the interactive map and download official boarding passes.</p>");
            sb.append("  </div>");
            sb.append("  <div class=\"guest-banner-actions\">");
            sb.append("    <a href=\"/register\" class=\"btn btn-primary\">➕ Register Free Account &rarr;</a>");
            sb.append("    <a href=\"/login\" class=\"btn btn-outline\">Sign In</a>");
            sb.append("  </div>");
            sb.append("</div>");
        }

        // Stats grid
        sb.append("<div class=\"stats-grid\">");
        sb.append("  <div class=\"stat-card stat-buses\">");
        sb.append("    <div class=\"stat-icon\">🚌</div>");
        sb.append("    <div class=\"stat-info\">");
        sb.append("      <span class=\"stat-value\">").append(totalBuses).append("</span>");
        sb.append("      <span class=\"stat-label\">Available Buses</span>");
        sb.append("    </div>");
        sb.append("  </div>");

        if (isAdmin) {
            sb.append("  <div class=\"stat-card stat-passengers\">");
            sb.append("    <div class=\"stat-icon\">👥</div>");
            sb.append("    <div class=\"stat-info\">");
            sb.append("      <span class=\"stat-value\">").append(totalPassengers).append("</span>");
            sb.append("      <span class=\"stat-label\">Total Passengers</span>");
            sb.append("    </div>");
            sb.append("  </div>");
        }

        sb.append("  <div class=\"stat-card stat-bookings\">");
        sb.append("    <div class=\"stat-icon\">🎟️</div>");
        sb.append("    <div class=\"stat-info\">");
        sb.append("      <span class=\"stat-value\">").append(totalBookings).append("</span>");
        sb.append("      <span class=\"stat-label\">").append(isAdmin ? "Total System Bookings" : "My Active Bookings").append("</span>");
        sb.append("    </div>");
        sb.append("  </div>");

        if (isAdmin) {
            sb.append("  <div class=\"stat-card stat-revenue\">");
            sb.append("    <div class=\"stat-icon\">💰</div>");
            sb.append("    <div class=\"stat-info\">");
            sb.append("      <span class=\"stat-value\">$").append(String.format("%.2f", totalRevenue)).append("</span>");
            sb.append("      <span class=\"stat-label\">Confirmed Revenue</span>");
            sb.append("    </div>");
            sb.append("  </div>");
        }
        sb.append("</div>");

        // Quick Navigation
        sb.append("<div class=\"section-title\"><h3>Quick Navigation</h3></div>");
        sb.append("<div class=\"quick-actions-grid\">");
        sb.append("  <a href=\"/buses\" class=\"quick-card\">");
        sb.append("    <span class=\"quick-icon\">🚌</span>");
        sb.append("    <h4>Bus Routes</h4>");
        sb.append("    <p>View operating schedules, destinations, and seat pricing</p>");
        sb.append("  </a>");
        sb.append("  <a href=\"/bookings\" class=\"quick-card\">");
        sb.append("    <span class=\"quick-icon\">🎟️</span>");
        sb.append("    <h4>Seat Reservation</h4>");
        sb.append("    <p>Select multiple seats on floorplan with live gender neighbor radar</p>");
        sb.append("  </a>");
        sb.append("  <a href=\"/tickets\" class=\"quick-card\">");
        sb.append("    <span class=\"quick-icon\">🎫</span>");
        sb.append("    <h4>Boarding Passes</h4>");
        sb.append("    <p>View, download, and print your confirmed tickets & concession details</p>");
        sb.append("  </a>");
        if (isAdmin) {
            sb.append("  <a href=\"/admin/users\" class=\"quick-card\">");
            sb.append("    <span class=\"quick-icon\">👑</span>");
            sb.append("    <h4>Users &amp; Booking History</h4>");
            sb.append("    <p>Access all user accounts, booking records, and passenger details</p>");
            sb.append("  </a>");
            sb.append("  <a href=\"/passengers\" class=\"quick-card\">");
            sb.append("    <span class=\"quick-icon\">👥</span>");
            sb.append("    <h4>Passenger Registry</h4>");
            sb.append("    <p>Manage passenger records and Disability (PwD) concessions</p>");
            sb.append("  </a>");
        } else if (session == null || !session.isAuthenticated()) {
            sb.append("  <a href=\"/register\" class=\"quick-card\">");
            sb.append("    <span class=\"quick-icon\">📝</span>");
            sb.append("    <h4>Create Account</h4>");
            sb.append("    <p>Register a traveler profile to reserve and manage seats</p>");
            sb.append("  </a>");
        }
        sb.append("</div>");

        // Recent Bookings
        sb.append("<div class=\"card\">");
        sb.append("  <div class=\"card-header\">");
        sb.append("    <h3>").append(isAdmin ? "Recent System Reservations" : "Your Bookings").append("</h3>");
        sb.append("    <a href=\"/bookings\" class=\"btn btn-sm btn-outline\">View All &rarr;</a>");
        sb.append("  </div>");
        sb.append("  <div class=\"table-responsive\">");
        sb.append("    <table class=\"table\">");
        sb.append("      <thead>");
        sb.append("        <tr><th>ID</th><th>Passenger</th><th>Route</th><th>Seat #</th><th>Date</th><th>Fare</th><th>Concession</th><th>Status</th><th>Action</th></tr>");
        sb.append("      </thead><tbody>");
        if (recentBookings == null || recentBookings.isEmpty()) {
            sb.append("      <tr><td colspan=\"9\" class=\"text-center text-muted\">No bookings recorded yet.</td></tr>");
        } else {
            int count = 0;
            for (Booking b : recentBookings) {
                if (count++ >= 6) break;
                boolean confirmed = "CONFIRMED".equalsIgnoreCase(b.getStatus());
                sb.append("      <tr>");
                sb.append("        <td><strong>#").append(b.getBookingId()).append("</strong></td>");
                sb.append("        <td>").append(WebUtils.escapeHtml(b.getPassengerName() != null ? b.getPassengerName() : "ID: " + b.getPassengerId())).append("</td>");
                sb.append("        <td>").append(WebUtils.escapeHtml(b.getSource() != null ? b.getSource() + " → " + b.getDestination() : "Bus #" + b.getBusId())).append("</td>");
                sb.append("        <td><span class=\"badge badge-seat\">Seat ").append(b.getSeatNumber()).append("</span></td>");
                sb.append("        <td>").append(WebUtils.escapeHtml(b.getBookingDate())).append("</td>");
                sb.append("        <td>$").append(String.format("%.2f", b.getFare())).append("</td>");
                sb.append("        <td>");
                if (b.isConcession()) {
                    sb.append("<span class=\"badge badge-pwd\" title=\"-$").append(String.format("%.2f", b.getConcessionAmount())).append(" concession\">♿ 50% PwD</span>");
                } else {
                    sb.append("<span class=\"text-muted\">None</span>");
                }
                sb.append("        </td>");
                sb.append("        <td><span class=\"badge badge-").append(confirmed ? "confirmed" : "cancelled").append("\">").append(WebUtils.escapeHtml(b.getStatus())).append("</span></td>");
                sb.append("        <td><a href=\"/tickets?id=\"").append(b.getBookingId()).append("\" class=\"btn btn-xs btn-primary\">Pass</a></td>");
                sb.append("      </tr>");
            }
        }
        sb.append("      </tbody>");
        sb.append("    </table>");
        sb.append("  </div>");
        sb.append("</div>");

        return htmlDoc("Dashboard", "dashboard", session, sb.toString(), msg, err);
    }

    // -------------------------------------------------------------------------
    // BUSES VIEW (Public browsing, Admin editing)
    // -------------------------------------------------------------------------

    public static String renderBuses(SessionManager.Session session, List<Bus> buses, Bus editBus, String csrfToken, String msg, String err) {
        StringBuilder sb = new StringBuilder();
        boolean isAdmin = session != null && session.isAdmin();

        sb.append("<div class=\"page-header\">");
        sb.append("  <div>");
        sb.append("    <h1>Bus Fleet & Routes</h1>");
        sb.append("    <p class=\"subtitle\">Explore bus routes, scheduled destinations, seat capacities, and ticket fares</p>");
        sb.append("  </div>");
        sb.append("</div>");

        if (isAdmin) {
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
            sb.append("      <input type=\"hidden\" name=\"csrf_token\" value=\"").append(WebUtils.escapeHtml(csrfToken)).append("\" />");
            sb.append("      <div class=\"form-group\">");
            sb.append("        <label for=\"bus_id\">Bus ID</label>");
            sb.append("        <input type=\"number\" id=\"bus_id\" name=\"bus_id\" value=\"").append(editBus != null ? editBus.getBusId() : "").append("\" ").append(editBus != null ? "readonly" : "required").append(" placeholder=\"e.g. 5\" />");
            sb.append("      </div>");
            sb.append("      <div class=\"form-group\">");
            sb.append("        <label for=\"bus_name\">Bus Fleet Name</label>");
            sb.append("        <input type=\"text\" id=\"bus_name\" name=\"bus_name\" value=\"").append(editBus != null ? WebUtils.escapeHtml(editBus.getBusName()) : "").append("\" placeholder=\"e.g. Scania Multi-Axle AC\" required />");
            sb.append("      </div>");
            sb.append("      <div class=\"form-row\">");
            sb.append("        <div class=\"form-group\">");
            sb.append("          <label for=\"source\">Source Origin</label>");
            sb.append("          <input type=\"text\" id=\"source\" name=\"source\" value=\"").append(editBus != null ? WebUtils.escapeHtml(editBus.getSource()) : "").append("\" placeholder=\"e.g. New York\" required />");
            sb.append("        </div>");
            sb.append("        <div class=\"form-group\">");
            sb.append("          <label for=\"destination\">Destination</label>");
            sb.append("          <input type=\"text\" id=\"destination\" name=\"destination\" value=\"").append(editBus != null ? WebUtils.escapeHtml(editBus.getDestination()) : "").append("\" placeholder=\"e.g. Boston\" required />");
            sb.append("        </div>");
            sb.append("      </div>");
            sb.append("      <div class=\"form-row\">");
            sb.append("        <div class=\"form-group\">");
            sb.append("          <label for=\"total_seats\">Total Seats Capacity</label>");
            sb.append("          <input type=\"number\" id=\"total_seats\" name=\"total_seats\" value=\"").append(editBus != null ? editBus.getTotalSeats() : 40).append("\" min=\"10\" max=\"80\" required />");
            sb.append("        </div>");
            sb.append("        <div class=\"form-group\">");
            sb.append("          <label for=\"fare\">Standard Ticket Fare ($)</label>");
            sb.append("          <input type=\"number\" step=\"0.01\" id=\"fare\" name=\"fare\" value=\"").append(editBus != null ? editBus.getFare() : 45.00).append("\" required />");
            sb.append("        </div>");
            sb.append("      </div>");
            sb.append("      <button type=\"submit\" class=\"btn btn-primary btn-block\">").append(editBus != null ? "Save Changes" : "Create Bus").append("</button>");
            sb.append("    </form>");
            sb.append("  </div>");
        }

        // Table Card
        sb.append("  <div class=\"card\">");
        sb.append("    <div class=\"card-header\">");
        sb.append("      <h3>Active Routes &amp; Fleet (").append(buses.size()).append(")</h3>");
        sb.append("    </div>");
        sb.append("    <div class=\"table-responsive\">");
        sb.append("      <table class=\"table\">");
        sb.append("        <thead>");
        sb.append("          <tr><th>ID</th><th>Bus Fleet Name</th><th>Route</th><th>Capacity</th><th>Fare</th><th>Disability Concession</th><th>Action</th></tr>");
        sb.append("        </thead><tbody>");
        if (buses.isEmpty()) {
            sb.append("        <tr><td colspan=\"7\" class=\"text-center text-muted\">No buses available in the fleet.</td></tr>");
        } else {
            for (Bus b : buses) {
                sb.append("        <tr>");
                sb.append("          <td><strong>#").append(b.getBusId()).append("</strong></td>");
                sb.append("          <td><strong>").append(WebUtils.escapeHtml(b.getBusName())).append("</strong></td>");
                sb.append("          <td><span class=\"route-badge\">").append(WebUtils.escapeHtml(b.getSource())).append(" &rarr; ").append(WebUtils.escapeHtml(b.getDestination())).append("</span></td>");
                sb.append("          <td>").append(b.getTotalSeats()).append(" seats</td>");
                sb.append("          <td><strong class=\"text-success\">$").append(String.format("%.2f", b.getFare())).append("</strong></td>");
                sb.append("          <td><span class=\"badge badge-pwd\">$").append(String.format("%.2f", b.getFare() * 0.50)).append(" (50% Off)</span></td>");
                if (isAdmin) {
                    sb.append("          <td class=\"actions-cell\">");
                    sb.append("            <a href=\"/buses?edit=").append(b.getBusId()).append("\" class=\"btn btn-xs btn-outline\">Edit</a>");
                    sb.append("            <form action=\"/buses/delete\" method=\"POST\" style=\"display:inline;\" onsubmit=\"return confirm('Delete bus #").append(b.getBusId()).append("?');\">");
                    sb.append("              <input type=\"hidden\" name=\"csrf_token\" value=\"").append(WebUtils.escapeHtml(csrfToken)).append("\" />");
                    sb.append("              <input type=\"hidden\" name=\"bus_id\" value=\"").append(b.getBusId()).append("\" />");
                    sb.append("              <button type=\"submit\" class=\"btn btn-xs btn-outline-danger\">Delete</button>");
                    sb.append("            </form>");
                    sb.append("          </td>");
                } else {
                    sb.append("          <td><a href=\"/bookings?bus_id=").append(b.getBusId()).append("\" class=\"btn btn-xs btn-primary\">Book Seats</a></td>");
                }
                sb.append("        </tr>");
            }
        }
        sb.append("        </tbody>");
        sb.append("      </table>");
        sb.append("    </div>");
        sb.append("  </div>");

        if (isAdmin) {
            sb.append("</div>");
        }

        return htmlDoc("Buses", "buses", session, sb.toString(), msg, err);
    }

    // -------------------------------------------------------------------------
    // PASSENGERS VIEW (Admin only with Disability / Concession Registry)
    // -------------------------------------------------------------------------

    public static String renderPassengers(SessionManager.Session session, List<Passenger> passengers, Passenger editPassenger, String csrfToken, String msg, String err) {
        StringBuilder sb = new StringBuilder();

        sb.append("<div class=\"page-header\">");
        sb.append("  <div>");
        sb.append("    <h1>Passenger Directory &amp; Concessions</h1>");
        sb.append("    <p class=\"subtitle\">Manage customer traveler records, identity, contact information, and disability concession registry</p>");
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
        sb.append("      <input type=\"hidden\" name=\"csrf_token\" value=\"").append(WebUtils.escapeHtml(csrfToken)).append("\" />");
        sb.append("      <div class=\"form-group\">");
        sb.append("        <label for=\"passenger_id\">Passenger ID</label>");
        sb.append("        <input type=\"number\" id=\"passenger_id\" name=\"passenger_id\" value=\"").append(editPassenger != null ? editPassenger.getPassengerId() : "").append("\" ").append(editPassenger != null ? "readonly" : "required").append(" placeholder=\"e.g. 104\" />");
        sb.append("      </div>");
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
        sb.append("            <option value=\"Male\" ").append("Male".equalsIgnoreCase(g) ? "selected" : "").append(">👨 Male</option>");
        sb.append("            <option value=\"Female\" ").append("Female".equalsIgnoreCase(g) ? "selected" : "").append(">👩 Female</option>");
        sb.append("            <option value=\"Other\" ").append("Other".equalsIgnoreCase(g) ? "selected" : "").append(">🧑 Other</option>");
        sb.append("          </select>");
        sb.append("        </div>");
        sb.append("      </div>");
        sb.append("      <div class=\"form-group\">");
        sb.append("        <label for=\"phone\">Phone Number</label>");
        sb.append("        <input type=\"text\" id=\"phone\" name=\"phone\" value=\"").append(editPassenger != null ? WebUtils.escapeHtml(editPassenger.getPhoneNumber()) : "").append("\" placeholder=\"e.g. 9876543210\" required />");
        sb.append("      </div>");

        // Disability Concession fields
        boolean isPwd = editPassenger != null && editPassenger.isDisability();
        sb.append("      <div class=\"form-group concession-box\">");
        sb.append("        <label class=\"concession-toggle-label\">");
        sb.append("          <input type=\"checkbox\" id=\"is_disability\" name=\"is_disability\" value=\"true\" ").append(isPwd ? "checked" : "").append(" onchange=\"toggleDisabilityFields(this.checked);\" />");
        sb.append("          <span>♿ <strong>Person with Disability (PwD) Concession</strong></span>");
        sb.append("        </label>");
        sb.append("        <small class=\"text-muted\" style=\"display:block;margin-top:0.3rem;\">Enables automatic 50% fare reduction upon ticket booking</small>");
        sb.append("      </div>");

        sb.append("      <div id=\"disability_fields_block\" style=\"display:").append(isPwd ? "block" : "none").append(";\">");
        sb.append("        <div class=\"form-group\">");
        sb.append("          <label for=\"disability_type\">Disability Category</label>");
        sb.append("          <select id=\"disability_type\" name=\"disability_type\">");
        String dtype = editPassenger != null ? editPassenger.getDisabilityType() : "Locomotor / Mobility";
        sb.append("            <option value=\"Locomotor / Mobility\" ").append("Locomotor / Mobility".equalsIgnoreCase(dtype) ? "selected" : "").append(">Locomotor / Physical (Wheelchair/Mobility)</option>");
        sb.append("            <option value=\"Visual Impairment\" ").append("Visual Impairment".equalsIgnoreCase(dtype) ? "selected" : "").append(">Visual Impairment (Blind / Low Vision)</option>");
        sb.append("            <option value=\"Hearing / Speech\" ").append("Hearing / Speech".equalsIgnoreCase(dtype) ? "selected" : "").append(">Hearing / Speech Impairment</option>");
        sb.append("            <option value=\"Cognitive / Intellectual\" ").append("Cognitive / Intellectual".equalsIgnoreCase(dtype) ? "selected" : "").append(">Cognitive / Intellectual</option>");
        sb.append("            <option value=\"Multiple Disabilities\" ").append("Multiple Disabilities".equalsIgnoreCase(dtype) ? "selected" : "").append(">Multiple Disabilities</option>");
        sb.append("            <option value=\"Other\" ").append("Other".equalsIgnoreCase(dtype) ? "selected" : "").append(">Other Verified Disability</option>");
        sb.append("          </select>");
        sb.append("        </div>");
        sb.append("        <div class=\"form-group\">");
        sb.append("          <label for=\"disability_id\">Govt UDID / Disability Card Number</label>");
        sb.append("          <input type=\"text\" id=\"disability_id\" name=\"disability_id\" value=\"").append(editPassenger != null && editPassenger.getDisabilityId() != null ? WebUtils.escapeHtml(editPassenger.getDisabilityId()) : "").append("\" placeholder=\"e.g. UDID-98765-2026\" />");
        sb.append("        </div>");
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
        sb.append("          <tr><th>ID</th><th>Name</th><th>Age</th><th>Gender</th><th>Phone</th><th>Disability Concession</th><th>Actions</th></tr>");
        sb.append("        </thead><tbody>");
        if (passengers.isEmpty()) {
            sb.append("        <tr><td colspan=\"7\" class=\"text-center text-muted\">No passengers registered yet.</td></tr>");
        } else {
            for (Passenger p : passengers) {
                sb.append("        <tr>");
                sb.append("          <td><strong>#").append(p.getPassengerId()).append("</strong></td>");
                sb.append("          <td>").append(WebUtils.escapeHtml(p.getName())).append("</td>");
                sb.append("          <td>").append(p.getAge()).append(" yrs</td>");
                String genderTag = "👨 Male";
                if ("Female".equalsIgnoreCase(p.getGender())) genderTag = "👩 Female";
                else if ("Other".equalsIgnoreCase(p.getGender())) genderTag = "🧑 Other";
                sb.append("          <td><span class=\"badge badge-gender\">").append(genderTag).append("</span></td>");
                sb.append("          <td><code>").append(WebUtils.escapeHtml(p.getPhoneNumber())).append("</code></td>");
                sb.append("          <td>");
                if (p.isDisability()) {
                    sb.append("<span class=\"badge badge-pwd\" title=\"Card: ").append(WebUtils.escapeHtml(p.getDisabilityId() != null ? p.getDisabilityId() : "N/A")).append("\">♿ 50% Concession (").append(WebUtils.escapeHtml(p.getDisabilityType() != null ? p.getDisabilityType() : "PwD")).append(")</span>");
                } else {
                    sb.append("<span class=\"badge badge-outline\">Standard Fare</span>");
                }
                sb.append("          </td>");
                sb.append("          <td class=\"actions-cell\">");
                sb.append("            <a href=\"/passengers?edit=").append(p.getPassengerId()).append("\" class=\"btn btn-xs btn-outline\">Edit</a>");
                sb.append("            <form action=\"/passengers/delete\" method=\"POST\" style=\"display:inline;\" onsubmit=\"return confirm('Delete passenger #").append(p.getPassengerId()).append("?');\">");
                sb.append("              <input type=\"hidden\" name=\"csrf_token\" value=\"").append(WebUtils.escapeHtml(csrfToken)).append("\" />");
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

        sb.append("<script>");
        sb.append("  function toggleDisabilityFields(show) {");
        sb.append("    var b = document.getElementById('disability_fields_block');");
        sb.append("    if (b) b.style.display = show ? 'block' : 'none';");
        sb.append("  }");
        sb.append("</script>");

        return htmlDoc("Passengers", "passengers", session, sb.toString(), msg, err);
    }

    // -------------------------------------------------------------------------
    // BOOKINGS VIEW: MULTI-SEAT BOOKING, GENDER MAP & NEAREST SEAT RADAR
    // -------------------------------------------------------------------------

    public static String renderBookings(SessionManager.Session session, List<Booking> bookings, List<Bus> buses, List<Passenger> passengers, List<Integer> bookedSeats, Integer selectedBusId, String csrfToken, String msg, String err) {
        Map<Integer, String> map = new HashMap<>();
        if (bookedSeats != null) {
            for (Integer s : bookedSeats) {
                map.put(s, "Other");
            }
        }
        return renderBookings(session, bookings, buses, passengers, map, selectedBusId, csrfToken, msg, err);
    }

    public static String renderBookings(SessionManager.Session session, List<Booking> bookings, List<Bus> buses, List<Passenger> passengers, Map<Integer, String> bookedSeatsWithGender, Integer selectedBusId, String csrfToken, String msg, String err) {
        StringBuilder sb = new StringBuilder();
        boolean isAdmin = session != null && session.isAdmin();

        sb.append("<div class=\"page-header\">");
        sb.append("  <div>");
        sb.append("    <h1>Seat Reservations &amp; Multi-Booking</h1>");
        sb.append("    <p class=\"subtitle\">Select single or multiple seats on the interactive bus layout with real-time neighbor gender radar &amp; 50% disability concession</p>");
        sb.append("  </div>");
        sb.append("</div>");

        // Step Progress Indicator
        sb.append("<div class=\"booking-stepper-container\">");
        sb.append("  <div class=\"step-item completed\">");
        sb.append("    <div class=\"step-badge\">1</div>");
        sb.append("    <div class=\"step-title\">Select Route</div>");
        sb.append("  </div>");
        sb.append("  <div class=\"step-line\"></div>");
        sb.append("  <div class=\"step-item active\">");
        sb.append("    <div class=\"step-badge\">2</div>");
        sb.append("    <div class=\"step-title\">Pick Seat(s) &amp; Check Neighbors</div>");
        sb.append("  </div>");
        sb.append("  <div class=\"step-line\"></div>");
        sb.append("  <div class=\"step-item\">");
        sb.append("    <div class=\"step-badge\">3</div>");
        sb.append("    <div class=\"step-title\">Passenger &amp; Concessions</div>");
        sb.append("  </div>");
        sb.append("  <div class=\"step-line\"></div>");
        sb.append("  <div class=\"step-item\">");
        sb.append("    <div class=\"step-badge\">4</div>");
        sb.append("    <div class=\"step-title\">Instant Confirmation</div>");
        sb.append("  </div>");
        sb.append("</div>");

        sb.append("<div class=\"grid-booking-layout\">");

        // Left Panel: Route & Seat Selector Grid
        sb.append("  <div class=\"card booking-grid-card\">");
        sb.append("    <div class=\"card-header\">");
        sb.append("      <h3>💺 Interactive Multi-Seat Floorplan</h3>");
        sb.append("    </div>");

        // Bus Route Dropdown
        sb.append("    <div class=\"form-group\" style=\"margin-bottom: 1.25rem;\">");
        sb.append("      <label for=\"route_selector\"><strong>Step 1: Choose Bus &amp; Route</strong></label>");
        sb.append("      <select id=\"route_selector\" class=\"form-control\" onchange=\"location.href='/bookings?bus_id=' + this.value;\">");
        sb.append("        <option value=\"\">-- Select Bus Route to View Seating Grid --</option>");
        Bus currentBus = null;
        for (Bus b : buses) {
            boolean sel = selectedBusId != null && selectedBusId == b.getBusId();
            if (sel) currentBus = b;
            sb.append("        <option value=\"").append(b.getBusId()).append("\" ").append(sel ? "selected" : "").append(">");
            sb.append(b.getBusId()).append(" - ").append(WebUtils.escapeHtml(b.getBusName())).append(" (").append(WebUtils.escapeHtml(b.getSource())).append(" → ").append(WebUtils.escapeHtml(b.getDestination())).append(") &bull; Standard: $").append(String.format("%.2f", b.getFare())).append(" &bull; PwD: $").append(String.format("%.2f", b.getFare() * 0.50));
            sb.append("</option>");
        }
        sb.append("      </select>");
        sb.append("    </div>");

        // Visual Seat Map
        if (currentBus != null) {
            int totalSeats = currentBus.getTotalSeats();
            sb.append("    <div class=\"seat-legend\">");
            sb.append("      <div class=\"legend-item\"><span class=\"seat-preview available\"></span> Available</div>");
            sb.append("      <div class=\"legend-item\"><span class=\"seat-preview selected\"></span> Selected</div>");
            sb.append("      <div class=\"legend-item\"><span class=\"seat-preview occupied-male\"></span> 👨 Male Occupied</div>");
            sb.append("      <div class=\"legend-item\"><span class=\"seat-preview occupied-female\"></span> 👩 Female Occupied</div>");
            sb.append("      <div class=\"legend-item\"><span class=\"seat-preview occupied-other\"></span> 🧑 Other</div>");
            sb.append("    </div>");

            sb.append("    <div class=\"bus-floorplan\">");
            sb.append("      <div class=\"bus-front\"><span class=\"driver-icon\">💺 Driver Cabin</span> &nbsp;|&nbsp; <span>Front Door 🚪</span></div>");
            sb.append("      <div class=\"seat-matrix\">");

            for (int s = 1; s <= totalSeats; s++) {
                boolean isOccupied = bookedSeatsWithGender != null && bookedSeatsWithGender.containsKey(s);
                String occGender = isOccupied ? bookedSeatsWithGender.get(s) : null;
                if (occGender == null || occGender.trim().isEmpty()) occGender = "Other";
                String genderClass = isOccupied ? "occupied-" + occGender.toLowerCase() : "";
                String genderIcon = "👨";
                if ("Female".equalsIgnoreCase(occGender)) genderIcon = "👩";
                else if ("Other".equalsIgnoreCase(occGender)) genderIcon = "🧑";

                sb.append("        <button type=\"button\" ");
                sb.append("          class=\"seat-btn ").append(isOccupied ? "occupied " + genderClass : "available").append("\" ");
                sb.append("          id=\"seat-btn-").append(s).append("\" ");
                sb.append("          data-seat=\"").append(s).append("\" ");
                if (isOccupied) {
                    sb.append("          disabled ");
                    sb.append("          title=\"Seat #").append(s).append(" is taken by ").append(WebUtils.escapeHtml(occGender)).append(" passenger\">");
                    sb.append("          <span class=\"seat-num\">").append(s).append("</span>");
                    sb.append("          <span class=\"seat-occupant\">").append(genderIcon).append("</span>");
                } else {
                    sb.append("          onclick=\"toggleSeat(").append(s).append(")\" ");
                    sb.append("          onmouseenter=\"updateNearestSeatInfo(").append(s).append(")\" ");
                    sb.append("          title=\"Click to select/unselect Seat #").append(s).append("\">");
                    sb.append("          <span class=\"seat-num\">").append(s).append("</span>");
                }
                sb.append("        </button>");
            }

            sb.append("      </div>");
            sb.append("      <div class=\"bus-back\">Rear Engine &amp; Emergency Exit</div>");
            sb.append("    </div>");

            // Real-Time Neighbor & Gender Radar Box
            sb.append("    <div id=\"seat-radar-card\" class=\"neighbor-radar-card\" style=\"display:none;\">");
            sb.append("      <div class=\"radar-header\">");
            sb.append("        <span class=\"radar-pulse\"></span>");
            sb.append("        <span id=\"radar-title\">🧭 Live Passenger Distance &amp; Gender Radar</span>");
            sb.append("      </div>");
            sb.append("      <div id=\"radar-body\" class=\"radar-body\"></div>");
            sb.append("    </div>");

        } else {
            sb.append("    <div class=\"empty-seat-state\">");
            sb.append("      <p>👈 Please select a bus from the dropdown above to load the visual seat layout.</p>");
            sb.append("    </div>");
        }
        sb.append("  </div>");

        // Right Panel: Reservation Form with Multi-Booking and Concession
        sb.append("  <div class=\"card form-card\">");
        sb.append("    <div class=\"card-header\">");
        sb.append("      <h3>🎟️ Reservation &amp; Multi-Booking</h3>");
        sb.append("    </div>");
        sb.append("    <form action=\"/bookings/add\" method=\"POST\" id=\"bookingForm\" onsubmit=\"return handleBookingSubmit(event);\">");
        sb.append("      <input type=\"hidden\" name=\"csrf_token\" value=\"").append(WebUtils.escapeHtml(csrfToken)).append("\" />");

        // Hidden input for comma-separated multiple seat numbers
        sb.append("      <input type=\"hidden\" id=\"seat_numbers\" name=\"seat_numbers\" value=\"\" />");
        sb.append("      <input type=\"hidden\" id=\"seat_number\" name=\"seat_number\" value=\"\" />");

        // Selected Bus ID
        sb.append("      <div class=\"form-group\">");
        sb.append("        <label for=\"bus_id\">Bus Selection</label>");
        sb.append("        <select id=\"bus_id\" name=\"bus_id\" required onchange=\"location.href='/bookings?bus_id=' + this.value;\">");
        sb.append("          <option value=\"\">-- Select Bus --</option>");
        for (Bus b : buses) {
            boolean sel = selectedBusId != null && selectedBusId == b.getBusId();
            sb.append("          <option value=\"").append(b.getBusId()).append("\" data-fare=\"").append(b.getFare()).append("\" ").append(sel ? "selected" : "").append(">");
            sb.append(b.getBusId()).append(" - ").append(WebUtils.escapeHtml(b.getBusName())).append(" ($").append(String.format("%.2f", b.getFare())).append(" / PwD: $").append(String.format("%.2f", b.getFare() * 0.50)).append(")");
            sb.append("</option>");
        }
        sb.append("        </select>");
        sb.append("      </div>");

        // Primary / Default Passenger Selector
        sb.append("      <div class=\"form-group\">");
        sb.append("        <label for=\"passenger_id\">Primary Booker / Default Passenger <span class=\"text-muted\" style=\"font-weight:normal;\">(Optional)</span></label>");
        if (!passengers.isEmpty()) {
            sb.append("        <select id=\"passenger_id\" name=\"passenger_id\" onchange=\"handlePrimaryPassengerChange();\">");
            sb.append("          <option value=\"\">-- Choose Default Booker (Optional) --</option>");
            for (Passenger p : passengers) {
                sb.append("          <option value=\"").append(p.getPassengerId()).append("\" ");
                sb.append("            data-name=\"").append(WebUtils.escapeHtml(p.getName())).append("\" ");
                sb.append("            data-gender=\"").append(WebUtils.escapeHtml(p.getGender())).append("\" ");
                sb.append("            data-age=\"").append(p.getAge()).append("\" ");
                sb.append("            data-phone=\"").append(WebUtils.escapeHtml(p.getPhoneNumber() != null ? p.getPhoneNumber() : "")).append("\" ");
                sb.append("            data-pwd=\"").append(p.isDisability()).append("\" ");
                sb.append("            data-pwd-type=\"").append(WebUtils.escapeHtml(p.getDisabilityType() != null ? p.getDisabilityType() : "")).append("\" ");
                sb.append("            data-pwd-id=\"").append(WebUtils.escapeHtml(p.getDisabilityId() != null ? p.getDisabilityId() : "")).append("\">");
                sb.append("#").append(p.getPassengerId()).append(" - ").append(WebUtils.escapeHtml(p.getName())).append(" (").append(WebUtils.escapeHtml(p.getGender())).append(", ").append(p.getAge()).append(" yrs)");
                if (p.isDisability()) {
                    sb.append(" ♿ [PwD Concession]");
                }
                sb.append("</option>");
            }
            sb.append("        </select>");
            sb.append("        <small class=\"text-muted\" style=\"display:block;margin-top:0.25rem;\">Select to auto-populate the first seat, or type each person's details directly per seat below.</small>");
        } else {
            sb.append("        <input type=\"number\" id=\"passenger_id\" name=\"passenger_id\" placeholder=\"Enter Passenger ID (e.g. 101)\" />");
        }
        sb.append("      </div>");

        // Selected Seats Display (Multiple Booking Widget)
        sb.append("      <div class=\"form-group\">");
        sb.append("        <label>Selected Seats (<span id=\"seat_count_display\">0</span> selected)</label>");
        sb.append("        <div id=\"selected_badges_list\" class=\"selected-seats-badge-row\">");
        sb.append("          <span class=\"text-muted\" id=\"no_seat_msg\">👈 Click any available seat(s) on the bus layout. Multiple seats allowed!</span>");
        sb.append("        </div>");
        sb.append("      </div>");

        // Individual Passenger Details per Selected Seat Section
        sb.append("      <div id=\"per_seat_passengers_section\" class=\"per-seat-passengers-section\">");
        sb.append("        <div class=\"section-subheading\">");
        sb.append("          <span>👤 Passenger Details for Each Seat</span>");
        sb.append("          <small class=\"text-muted\">Enter individual name, gender, age &amp; concession for each selected seat</small>");
        sb.append("        </div>");
        sb.append("        <div id=\"per_seat_passengers_container\">");
        sb.append("          <div class=\"text-muted-box\" id=\"seat_prompt_box\">👈 Click any seat on the floorplan to enter passenger details for that seat.</div>");
        sb.append("        </div>");
        sb.append("      </div>");

        // Travel Date
        sb.append("      <div class=\"form-group\">");
        sb.append("        <label for=\"booking_date\">Travel Date</label>");
        sb.append("        <input type=\"date\" id=\"booking_date\" name=\"booking_date\" value=\"").append(LocalDate.now().toString()).append("\" required />");
        sb.append("      </div>");

        // Global / Master Disability Concession Checkbox & Indicator
        sb.append("      <div class=\"form-group concession-box\">");
        sb.append("        <label class=\"concession-toggle-label\">");
        sb.append("          <input type=\"checkbox\" id=\"concession_checkbox\" name=\"is_concession\" value=\"true\" onchange=\"recalculateFare();\" />");
        sb.append("          <span>♿ <strong>Apply 50% Disability Concession (All Seats)</strong></span>");
        sb.append("        </label>");
        sb.append("        <small class=\"text-muted\" style=\"display:block;margin-top:0.3rem;\">Or check individual PwD concession per seat in the cards above.</small>");
        sb.append("        <div id=\"pwd_info_banner\" style=\"display:none;margin-top:0.5rem;\" class=\"badge badge-pwd\">");
        sb.append("          Verified PwD Booker: <span id=\"pwd_detail_text\"></span>");
        sb.append("        </div>");
        sb.append("      </div>");

        // Dynamic Fare Breakdown Card
        double unitFare = currentBus != null ? currentBus.getFare() : 0.0;
        sb.append("      <div class=\"fare-breakdown-card\">");
        sb.append("        <div class=\"fare-row\">");
        sb.append("          <span>Base Fare per Seat:</span>");
        sb.append("          <span>$<span id=\"unit_fare_display\">").append(String.format("%.2f", unitFare)).append("</span></span>");
        sb.append("        </div>");
        sb.append("        <div class=\"fare-row\">");
        sb.append("          <span>Seats Booked:</span>");
        sb.append("          <span id=\"seats_quantity_display\">0</span>");
        sb.append("        </div>");
        sb.append("        <div class=\"fare-row discount\" id=\"discount_row\" style=\"display:none;\">");
        sb.append("          <span>Disability Concession (50% Off):</span>");
        sb.append("          <span>-$<span id=\"discount_amount_display\">0.00</span></span>");
        sb.append("        </div>");
        sb.append("        <div class=\"fare-row total\">");
        sb.append("          <span>Net Total Payable:</span>");
        sb.append("          <span class=\"text-success\">$<span id=\"total_fare_display\">0.00</span></span>");
        sb.append("        </div>");
        sb.append("      </div>");

        // Submit Button with animated spinner
        sb.append("      <button type=\"submit\" class=\"btn btn-primary btn-block\" id=\"submitBookingBtn\">");
        sb.append("        <span class=\"btn-text\">Confirm Reservation &rarr;</span>");
        sb.append("        <span class=\"btn-spinner\" style=\"display:none;\"><svg class=\"spinner-svg\" viewBox=\"0 0 24 24\"><circle cx=\"12\" cy=\"12\" r=\"10\" stroke=\"currentColor\" stroke-width=\"4\" fill=\"none\" stroke-dasharray=\"30 30\"></circle></svg> Processing...</span>");
        sb.append("      </button>");
        sb.append("    </form>");
        sb.append("  </div>");

        sb.append("</div>");

        // Bookings Table
        sb.append("<div class=\"card\" style=\"margin-top: 1.5rem;\">");
        sb.append("  <div class=\"card-header\">");
        sb.append("    <h3>").append(isAdmin ? "All System Bookings (" + bookings.size() + ")" : "My Bookings (" + bookings.size() + ")").append("</h3>");
        sb.append("  </div>");
        sb.append("  <div class=\"table-responsive\">");
        sb.append("    <table class=\"table\">");
        sb.append("      <thead>");
        sb.append("        <tr><th>ID</th><th>Passenger</th><th>Route</th><th>Seat #</th><th>Date</th><th>Fare</th><th>Concession</th><th>Status</th><th>Actions</th></tr>");
        sb.append("      </thead><tbody>");
        if (bookings.isEmpty()) {
            sb.append("      <tr><td colspan=\"9\" class=\"text-center text-muted\">No seat reservations found.</td></tr>");
        } else {
            for (Booking b : bookings) {
                boolean confirmed = "CONFIRMED".equalsIgnoreCase(b.getStatus());
                sb.append("      <tr>");
                sb.append("        <td><strong>#").append(b.getBookingId()).append("</strong></td>");
                sb.append("        <td>").append(WebUtils.escapeHtml(b.getPassengerName() != null ? b.getPassengerName() : "ID: " + b.getPassengerId())).append("</td>");
                sb.append("        <td>").append(WebUtils.escapeHtml(b.getBusName() != null ? b.getBusName() : "Bus #" + b.getBusId()));
                if (b.getSource() != null && b.getDestination() != null) {
                    sb.append("<br><small class=\"text-muted\">").append(WebUtils.escapeHtml(b.getSource())).append(" &rarr; ").append(WebUtils.escapeHtml(b.getDestination())).append("</small>");
                }
                sb.append("        </td>");
                sb.append("        <td><span class=\"badge badge-seat\">Seat ").append(b.getSeatNumber()).append("</span></td>");
                sb.append("        <td>").append(WebUtils.escapeHtml(b.getBookingDate())).append("</td>");
                sb.append("        <td>$").append(String.format("%.2f", b.getFare())).append("</td>");
                sb.append("        <td>");
                if (b.isConcession()) {
                    sb.append("<span class=\"badge badge-pwd\" title=\"-$").append(String.format("%.2f", b.getConcessionAmount())).append(" Saved\">♿ 50% Concession</span>");
                } else {
                    sb.append("<span class=\"text-muted\">Standard</span>");
                }
                sb.append("        </td>");
                sb.append("        <td><span class=\"badge badge-").append(confirmed ? "confirmed" : "cancelled").append("\">").append(WebUtils.escapeHtml(b.getStatus())).append("</span></td>");
                sb.append("        <td class=\"actions-cell\">");
                sb.append("          <a href=\"/tickets?id=").append(b.getBookingId()).append("\" class=\"btn btn-xs btn-primary\">Pass</a>");
                if (confirmed) {
                    sb.append("          <form action=\"/bookings/cancel\" method=\"POST\" style=\"display:inline;\" onsubmit=\"return confirm('Cancel reservation #").append(b.getBookingId()).append("?');\">");
                    sb.append("            <input type=\"hidden\" name=\"csrf_token\" value=\"").append(WebUtils.escapeHtml(csrfToken)).append("\" />");
                    sb.append("            <input type=\"hidden\" name=\"booking_id\" value=\"").append(b.getBookingId()).append("\" />");
                    sb.append("            <button type=\"submit\" class=\"btn btn-xs btn-outline-warning\">Cancel</button>");
                    sb.append("          </form>");
                }
                if (isAdmin) {
                    sb.append("          <form action=\"/bookings/delete\" method=\"POST\" style=\"display:inline;\" onsubmit=\"return confirm('Delete reservation permanently?');\">");
                    sb.append("            <input type=\"hidden\" name=\"csrf_token\" value=\"").append(WebUtils.escapeHtml(csrfToken)).append("\" />");
                    sb.append("            <input type=\"hidden\" name=\"booking_id\" value=\"").append(b.getBookingId()).append("\" />");
                    sb.append("            <button type=\"submit\" class=\"btn btn-xs btn-outline-danger\">Delete</button>");
                    sb.append("          </form>");
                }
                sb.append("        </td>");
                sb.append("      </tr>");
            }
        }
        sb.append("      </tbody>");
        sb.append("    </table>");
        sb.append("  </div>");
        sb.append("</div>");

        // Embedded JSON and Client-side JavaScript for Multi-Seat selection, live gender neighbor radar, per-seat passenger cards, and fare calculation
        sb.append("<script>");
        sb.append("  var currentUnitFare = ").append(currentBus != null ? currentBus.getFare() : 0.0).append(";");
        sb.append("  var selectedSeats = [];");
        sb.append("  var bookedSeatsGenderMap = {");
        if (bookedSeatsWithGender != null) {
            boolean first = true;
            for (Map.Entry<Integer, String> entry : bookedSeatsWithGender.entrySet()) {
                if (!first) sb.append(",");
                sb.append(entry.getKey()).append(":'").append(WebUtils.escapeHtml(entry.getValue())).append("'");
                first = false;
            }
        }
        sb.append("  };");

        // Registered passenger database for quick autofill
        sb.append("  var registeredPassengers = [");
        if (passengers != null) {
            boolean firstP = true;
            for (Passenger p : passengers) {
                if (!firstP) sb.append(",");
                String safeName = p.getName() != null ? p.getName().replace("'", "\\'").replace("\"", "\\\"") : "";
                String safeGender = p.getGender() != null ? p.getGender().replace("'", "\\'") : "Male";
                String safePhone = p.getPhoneNumber() != null ? p.getPhoneNumber().replace("'", "\\'") : "";
                String safeType = p.getDisabilityType() != null ? p.getDisabilityType().replace("'", "\\'") : "";
                String safeId = p.getDisabilityId() != null ? p.getDisabilityId().replace("'", "\\'") : "";
                sb.append("{id:").append(p.getPassengerId())
                  .append(",name:'").append(safeName).append("'")
                  .append(",gender:'").append(safeGender).append("'")
                  .append(",age:").append(p.getAge())
                  .append(",phone:'").append(safePhone).append("'")
                  .append(",isPwd:").append(p.isDisability())
                  .append(",pwdType:'").append(safeType).append("'")
                  .append(",pwdId:'").append(safeId).append("'}");
                firstP = false;
            }
        }
        sb.append("  ];");

        sb.append("  var seatPassengerData = {};");

        sb.append("  function escapeHtml(str) {");
        sb.append("    if (!str) return '';");
        sb.append("    return String(str).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/\"/g, '&quot;').replace(/'/g, '&#39;');");
        sb.append("  }");

        // Keep DOM values synchronized with internal state
        sb.append("  function syncDomToState() {");
        sb.append("    for (var i = 0; i < selectedSeats.length; i++) {");
        sb.append("      var s = selectedSeats[i];");
        sb.append("      var nInput = document.getElementById('pname_' + s);");
        sb.append("      var gSelect = document.getElementById('pgender_' + s);");
        sb.append("      var aInput = document.getElementById('page_' + s);");
        sb.append("      var pChk = document.getElementById('ppwd_' + s);");
        sb.append("      if (nInput) {");
        sb.append("        if (!seatPassengerData[s]) seatPassengerData[s] = {};");
        sb.append("        seatPassengerData[s].name = nInput.value;");
        sb.append("        if (gSelect) seatPassengerData[s].gender = gSelect.value;");
        sb.append("        if (aInput) seatPassengerData[s].age = aInput.value;");
        sb.append("        if (pChk) seatPassengerData[s].isPwd = pChk.checked;");
        sb.append("      }");
        sb.append("    }");
        sb.append("  }");

        // Toggle multiple seats
        sb.append("  function toggleSeat(seatNum) {");
        sb.append("    syncDomToState();");
        sb.append("    var idx = selectedSeats.indexOf(seatNum);");
        sb.append("    var btn = document.getElementById('seat-btn-' + seatNum);");
        sb.append("    if (idx >= 0) {");
        sb.append("      selectedSeats.splice(idx, 1);");
        sb.append("      delete seatPassengerData[seatNum];");
        sb.append("      if (btn) btn.classList.remove('selected');");
        sb.append("    } else {");
        sb.append("      selectedSeats.push(seatNum);");
        sb.append("      if (btn) btn.classList.add('selected');");
        sb.append("      if (!seatPassengerData[seatNum]) {");
        sb.append("        var pSel = document.getElementById('passenger_id');");
        sb.append("        var opt = pSel && pSel.selectedIndex >= 0 ? pSel.options[pSel.selectedIndex] : null;");
        sb.append("        if (selectedSeats.length === 1 && opt && opt.value) {");
        sb.append("          seatPassengerData[seatNum] = {");
        sb.append("            name: opt.getAttribute('data-name') || '',");
        sb.append("            gender: opt.getAttribute('data-gender') || 'Male',");
        sb.append("            age: opt.getAttribute('data-age') || '25',");
        sb.append("            phone: opt.getAttribute('data-phone') || '',");
        sb.append("            isPwd: opt.getAttribute('data-pwd') === 'true'");
        sb.append("          };");
        sb.append("        } else {");
        sb.append("          seatPassengerData[seatNum] = { name: '', gender: 'Male', age: '25', phone: '', isPwd: false };");
        sb.append("        }");
        sb.append("      }");
        sb.append("    }");
        sb.append("    updateSelectedSeatsDisplay();");
        sb.append("    renderSeatPassengerCards();");
        sb.append("    updateNearestSeatInfo(seatNum);");
        sb.append("    recalculateFare();");
        sb.append("  }");

        sb.append("  function saveSeatField(seatNum, field, val) {");
        sb.append("    if (!seatPassengerData[seatNum]) {");
        sb.append("      seatPassengerData[seatNum] = { name: '', gender: 'Male', age: '25', phone: '', isPwd: false };");
        sb.append("    }");
        sb.append("    seatPassengerData[seatNum][field] = val;");
        sb.append("    if (field === 'isPwd') { recalculateFare(); }");
        sb.append("  }");

        // Autofill an individual seat from registered passengers list
        sb.append("  function autofillSeat(seatNum, pIdStr) {");
        sb.append("    if (!pIdStr) return;");
        sb.append("    var pId = parseInt(pIdStr);");
        sb.append("    var p = null;");
        sb.append("    for (var i = 0; i < registeredPassengers.length; i++) {");
        sb.append("      if (registeredPassengers[i].id === pId) { p = registeredPassengers[i]; break; }");
        sb.append("    }");
        sb.append("    if (!p) return;");
        sb.append("    seatPassengerData[seatNum] = {");
        sb.append("      name: p.name,");
        sb.append("      gender: p.gender || 'Male',");
        sb.append("      age: p.age || '25',");
        sb.append("      phone: p.phone || '',");
        sb.append("      isPwd: p.isPwd === true");
        sb.append("    };");
        sb.append("    var nameInput = document.getElementById('pname_' + seatNum);");
        sb.append("    var genderSelect = document.getElementById('pgender_' + seatNum);");
        sb.append("    var ageInput = document.getElementById('page_' + seatNum);");
        sb.append("    var pwdChk = document.getElementById('ppwd_' + seatNum);");
        sb.append("    if (nameInput) nameInput.value = p.name;");
        sb.append("    if (genderSelect) genderSelect.value = p.gender || 'Male';");
        sb.append("    if (ageInput) ageInput.value = p.age || '25';");
        sb.append("    if (pwdChk) pwdChk.checked = p.isPwd === true;");
        sb.append("    recalculateFare();");
        sb.append("    updateNearestSeatInfo(seatNum);");
        sb.append("  }");

        // Handle primary passenger selection
        sb.append("  function handlePrimaryPassengerChange() {");
        sb.append("    syncDomToState();");
        sb.append("    var sel = document.getElementById('passenger_id');");
        sb.append("    if (!sel) return;");
        sb.append("    var opt = sel.options[sel.selectedIndex];");
        sb.append("    if (!opt || !opt.value) return;");
        sb.append("    var name = opt.getAttribute('data-name') || '';");
        sb.append("    var gender = opt.getAttribute('data-gender') || 'Male';");
        sb.append("    var age = opt.getAttribute('data-age') || '25';");
        sb.append("    var phone = opt.getAttribute('data-phone') || '';");
        sb.append("    var isPwd = opt.getAttribute('data-pwd') === 'true';");
        sb.append("    var pwdType = opt.getAttribute('data-pwd-type') || '';");
        sb.append("    var pwdId = opt.getAttribute('data-pwd-id') || '';");
        sb.append("    var banner = document.getElementById('pwd_info_banner');");
        sb.append("    var bannerText = document.getElementById('pwd_detail_text');");
        sb.append("    if (isPwd) {");
        sb.append("      if (banner) { banner.style.display = 'inline-block'; bannerText.textContent = pwdType + (pwdId ? ' (' + pwdId + ')' : ''); }");
        sb.append("    } else {");
        sb.append("      if (banner) banner.style.display = 'none';");
        sb.append("    }");
        sb.append("    if (selectedSeats.length > 0) {");
        sb.append("      var firstSeat = selectedSeats[0];");
        sb.append("      if (!seatPassengerData[firstSeat] || !seatPassengerData[firstSeat].name) {");
        sb.append("        seatPassengerData[firstSeat] = { name: name, gender: gender, age: age, phone: phone, isPwd: isPwd };");
        sb.append("        var nInput = document.getElementById('pname_' + firstSeat);");
        sb.append("        var gSelect = document.getElementById('pgender_' + firstSeat);");
        sb.append("        var aInput = document.getElementById('page_' + firstSeat);");
        sb.append("        var pChk = document.getElementById('ppwd_' + firstSeat);");
        sb.append("        if (nInput) nInput.value = name;");
        sb.append("        if (gSelect) gSelect.value = gender;");
        sb.append("        if (aInput) aInput.value = age;");
        sb.append("        if (pChk) pChk.checked = isPwd;");
        sb.append("      }");
        sb.append("    }");
        sb.append("    recalculateFare();");
        sb.append("  }");

        // Render individual passenger card for each selected seat
        sb.append("  function renderSeatPassengerCards() {");
        sb.append("    var container = document.getElementById('per_seat_passengers_container');");
        sb.append("    if (!container) return;");
        sb.append("    if (selectedSeats.length === 0) {");
        sb.append("      container.innerHTML = '<div class=\"text-muted-box\" id=\"seat_prompt_box\">👈 Click any available seat(s) on the bus floorplan to enter passenger details.</div>';");
        sb.append("      return;");
        sb.append("    }");
        sb.append("    selectedSeats.sort(function(a,b){return a-b;});");
        sb.append("    var html = '';");
        sb.append("    for (var i = 0; i < selectedSeats.length; i++) {");
        sb.append("      var s = selectedSeats[i];");
        sb.append("      var data = seatPassengerData[s] || { name: '', gender: 'Male', age: '25', phone: '', isPwd: false };");
        sb.append("      html += '<div class=\"seat-passenger-card\" id=\"card_seat_' + s + '\">';");
        sb.append("      html += '  <div class=\"seat-card-header\">';");
        sb.append("      html += '    <div class=\"seat-card-title\"><span class=\"seat-badge-pill\">Seat #' + s + '</span> <span>Passenger Details</span></div>';");
        sb.append("      if (registeredPassengers.length > 0) {");
        sb.append("        html += '    <select class=\"autofill-select\" onchange=\"autofillSeat(' + s + ', this.value)\">';");
        sb.append("        html += '      <option value=\"\">⚡ Autofill...</option>';");
        sb.append("        for (var j = 0; j < registeredPassengers.length; j++) {");
        sb.append("          var rp = registeredPassengers[j];");
        sb.append("          html += '      <option value=\"' + rp.id + '\">' + escapeHtml(rp.name) + (rp.isPwd ? ' ♿' : '') + '</option>';");
        sb.append("        }");
        sb.append("        html += '    </select>';");
        sb.append("      }");
        sb.append("      html += '  </div>';");
        sb.append("      html += '  <div class=\"seat-card-grid\">';");
        sb.append("      html += '    <div>';");
        sb.append("      html += '      <label style=\"font-size:0.78rem;color:var(--text-muted);display:block;margin-bottom:0.25rem;\">Person Full Name *</label>';");
        sb.append("      html += '      <input type=\"text\" name=\"passenger_name_' + s + '\" id=\"pname_' + s + '\" value=\"' + escapeHtml(data.name) + '\" placeholder=\"Enter name for Seat #' + s + '\" required oninput=\"saveSeatField(' + s + ', \\'name\\', this.value)\" />';");
        sb.append("      html += '    </div>';");
        sb.append("      html += '    <div class=\"seat-card-row\">';");
        sb.append("      html += '      <div style=\"flex:1;\">';");
        sb.append("      html += '        <label style=\"font-size:0.75rem;color:var(--text-muted);display:block;margin-bottom:0.2rem;\">Gender</label>';");
        sb.append("      html += '        <select name=\"passenger_gender_' + s + '\" id=\"pgender_' + s + '\" onchange=\"saveSeatField(' + s + ', \\'gender\\', this.value); updateNearestSeatInfo(' + s + ');\">';");
        sb.append("      html += '          <option value=\"Male\"' + (data.gender === 'Male' ? ' selected' : '') + '>👨 Male</option>';");
        sb.append("      html += '          <option value=\"Female\"' + (data.gender === 'Female' ? ' selected' : '') + '>👩 Female</option>';");
        sb.append("      html += '          <option value=\"Other\"' + (data.gender === 'Other' ? ' selected' : '') + '>🧑 Other</option>';");
        sb.append("      html += '        </select>';");
        sb.append("      html += '      </div>';");
        sb.append("      html += '      <div style=\"width:75px;\">';");
        sb.append("      html += '        <label style=\"font-size:0.75rem;color:var(--text-muted);display:block;margin-bottom:0.2rem;\">Age</label>';");
        sb.append("      html += '        <input type=\"number\" name=\"passenger_age_' + s + '\" id=\"page_' + s + '\" value=\"' + escapeHtml(data.age) + '\" min=\"1\" max=\"120\" oninput=\"saveSeatField(' + s + ', \\'age\\', this.value)\" />';");
        sb.append("      html += '      </div>';");
        sb.append("      html += '      <div style=\"display:flex;align-items:flex-end;padding-bottom:0.35rem;\">';");
        sb.append("      html += '        <label class=\"concession-mini-label\" title=\"Apply 50% concession for Seat #' + s + '\">';");
        sb.append("      html += '          <input type=\"checkbox\" name=\"passenger_pwd_' + s + '\" id=\"ppwd_' + s + '\" value=\"true\" ' + (data.isPwd ? 'checked' : '') + ' onchange=\"saveSeatField(' + s + ', \\'isPwd\\', this.checked)\" />';");
        sb.append("      html += '          <span>♿ 50% PwD</span>';");
        sb.append("      html += '        </label>';");
        sb.append("      html += '      </div>';");
        sb.append("      html += '    </div>';");
        sb.append("      html += '  </div>';");
        sb.append("      html += '</div>';");
        sb.append("    }");
        sb.append("    container.innerHTML = html;");
        sb.append("  }");

        // Update badges and hidden inputs
        sb.append("  function updateSelectedSeatsDisplay() {");
        sb.append("    var container = document.getElementById('selected_badges_list');");
        sb.append("    var countDisplay = document.getElementById('seat_count_display');");
        sb.append("    var numbersInput = document.getElementById('seat_numbers');");
        sb.append("    var singleInput = document.getElementById('seat_number');");
        sb.append("    countDisplay.textContent = selectedSeats.length;");
        sb.append("    numbersInput.value = selectedSeats.join(',');");
        sb.append("    singleInput.value = selectedSeats.length > 0 ? selectedSeats[0] : '';");
        sb.append("    if (selectedSeats.length === 0) {");
        sb.append("      container.innerHTML = '<span class=\"text-muted\" id=\"no_seat_msg\">👈 Click any available seat(s) on the bus layout. Multiple seats allowed!</span>';");
        sb.append("    } else {");
        sb.append("      var html = '';");
        sb.append("      selectedSeats.sort(function(a,b){return a-b;});");
        sb.append("      for (var i = 0; i < selectedSeats.length; i++) {");
        sb.append("        var s = selectedSeats[i];");
        sb.append("        html += '<span class=\"seat-tag\">Seat #' + s + ' <span class=\"seat-tag-remove\" onclick=\"toggleSeat(' + s + ')\">&times;</span></span>';");
        sb.append("      }");
        sb.append("      container.innerHTML = html;");
        sb.append("    }");
        sb.append("    recalculateFare();");
        sb.append("  }");

        // Real-Time Nearest Occupied Seat & Gender Radar
        sb.append("  function updateNearestSeatInfo(seatNum) {");
        sb.append("    var radarCard = document.getElementById('seat-radar-card');");
        sb.append("    var radarBody = document.getElementById('radar-body');");
        sb.append("    var radarTitle = document.getElementById('radar-title');");
        sb.append("    if (!radarCard || !radarBody) return;");
        sb.append("    radarCard.style.display = 'block';");
        sb.append("    radarTitle.innerHTML = '🧭 Passenger Neighbor Radar for <strong>Seat #' + seatNum + '</strong>';");

        // Geometry: 4 seats per row (2 on left, aisle, 2 on right)
        sb.append("    var r = Math.floor((seatNum - 1) / 4);");
        sb.append("    var c = (seatNum - 1) % 4;");
        // Direct adjacent neighbor in same pair
        sb.append("    var adjSeat = (c === 0) ? (seatNum + 1) : (c === 1) ? (seatNum - 1) : (c === 2) ? (seatNum + 1) : (seatNum - 1);");
        sb.append("    var adjOccupied = bookedSeatsGenderMap[adjSeat];");

        sb.append("    var minFemale = null, minFemaleDist = 999;");
        sb.append("    var minMale = null, minMaleDist = 999;");
        sb.append("    var minOther = null, minOtherDist = 999;");
        sb.append("    var overallNearest = null, overallDist = 999;");

        sb.append("    for (var key in bookedSeatsGenderMap) {");
        sb.append("      var occSeat = parseInt(key);");
        sb.append("      var occGender = bookedSeatsGenderMap[key];");
        sb.append("      var occR = Math.floor((occSeat - 1) / 4);");
        sb.append("      var occC = (occSeat - 1) % 4;");
        sb.append("      var dx = (occC >= 2 ? occC + 0.5 : occC) - (c >= 2 ? c + 0.5 : c);");
        sb.append("      var dy = (occR - r) * 1.5;");
        sb.append("      var dist = Math.sqrt(dx*dx + dy*dy);");
        sb.append("      if (dist < overallDist) { overallDist = dist; overallNearest = {seat: occSeat, gender: occGender, dist: dist}; }");
        sb.append("      if (occGender.toLowerCase() === 'female' && dist < minFemaleDist) { minFemaleDist = dist; minFemale = occSeat; }");
        sb.append("      if (occGender.toLowerCase() === 'male' && dist < minMaleDist) { minMaleDist = dist; minMale = occSeat; }");
        sb.append("      if (occGender.toLowerCase() === 'other' && dist < minOtherDist) { minOtherDist = dist; minOther = occSeat; }");
        sb.append("    }");

        sb.append("    var html = '<div class=\"radar-item\"><strong>Direct Adjacent Seat #' + adjSeat + ':</strong> ';");
        sb.append("    if (adjOccupied) {");
        sb.append("      var gBadge = adjOccupied.toLowerCase();");
        sb.append("      var gIcon = gBadge === 'female' ? '👩' : gBadge === 'male' ? '👨' : '🧑';");
        sb.append("      html += '<span class=\"radar-badge ' + gBadge + '\">' + gIcon + ' Taken by ' + adjOccupied + '</span>';");
        sb.append("    } else {");
        sb.append("      html += '<span class=\"radar-badge free\">🟢 Free / Empty</span>';");
        sb.append("    }");
        sb.append("    html += '</div>';");

        sb.append("    html += '<div class=\"radar-item\" style=\"margin-top:0.4rem;display:flex;flex-wrap:wrap;gap:0.75rem;\">';");
        sb.append("    if (minFemale) { html += '<span>👩 Nearest Female: <strong>Seat #' + minFemale + '</strong></span>'; } else { html += '<span class=\"text-muted\">👩 Nearest Female: None</span>'; }");
        sb.append("    if (minMale) { html += '<span>👨 Nearest Male: <strong>Seat #' + minMale + '</strong></span>'; } else { html += '<span class=\"text-muted\">👨 Nearest Male: None</span>'; }");
        sb.append("    if (minOther) { html += '<span>🧑 Nearest Other: <strong>Seat #' + minOther + '</strong></span>'; }");
        sb.append("    html += '</div>';");
        sb.append("    radarBody.innerHTML = html;");
        sb.append("  }");

        // Recalculate Fare with Multi-Seat and Concessions
        sb.append("  function recalculateFare() {");
        sb.append("    syncDomToState();");
        sb.append("    var count = selectedSeats.length;");
        sb.append("    var globalChk = document.getElementById('concession_checkbox');");
        sb.append("    var globalConcession = globalChk ? globalChk.checked : false;");
        sb.append("    var subtotal = count * currentUnitFare;");
        sb.append("    var discount = 0.0;");
        sb.append("    for (var i = 0; i < selectedSeats.length; i++) {");
        sb.append("      var s = selectedSeats[i];");
        sb.append("      var isPwd = (seatPassengerData[s] && seatPassengerData[s].isPwd) || globalConcession;");
        sb.append("      if (isPwd) {");
        sb.append("        discount += (currentUnitFare * 0.50);");
        sb.append("      }");
        sb.append("    }");
        sb.append("    var total = Math.max(0, subtotal - discount);");
        sb.append("    document.getElementById('seats_quantity_display').textContent = count;");
        sb.append("    var discRow = document.getElementById('discount_row');");
        sb.append("    if (discount > 0 && count > 0) {");
        sb.append("      discRow.style.display = 'flex';");
        sb.append("      document.getElementById('discount_amount_display').textContent = discount.toFixed(2);");
        sb.append("    } else {");
        sb.append("      discRow.style.display = 'none';");
        sb.append("    }");
        sb.append("    document.getElementById('total_fare_display').textContent = total.toFixed(2);");
        sb.append("  }");

        // Handle submit
        sb.append("  function handleBookingSubmit(e) {");
        sb.append("    syncDomToState();");
        sb.append("    if (selectedSeats.length === 0) {");
        sb.append("      alert('Please select at least one available seat on the visual bus floorplan.');");
        sb.append("      return false;");
        sb.append("    }");
        sb.append("    for (var i = 0; i < selectedSeats.length; i++) {");
        sb.append("      var s = selectedSeats[i];");
        sb.append("      var input = document.getElementById('pname_' + s);");
        sb.append("      if (!input || !input.value.trim()) {");
        sb.append("        alert('Please enter the passenger name for Seat #' + s + '.');");
        sb.append("        if (input) input.focus();");
        sb.append("        return false;");
        sb.append("      }");
        sb.append("    }");
        sb.append("    var btn = document.getElementById('submitBookingBtn');");
        sb.append("    if (btn) {");
        sb.append("      btn.classList.add('loading');");
        sb.append("      var textSpan = btn.querySelector('.btn-text');");
        sb.append("      var spinSpan = btn.querySelector('.btn-spinner');");
        sb.append("      if (textSpan) textSpan.style.display = 'none';");
        sb.append("      if (spinSpan) spinSpan.style.display = 'inline-flex';");
        sb.append("      btn.disabled = true;");
        sb.append("    }");
        sb.append("    document.getElementById('bookingForm').submit();");
        sb.append("    return true;");
        sb.append("  }");
        sb.append("</script>");

        return htmlDoc("Reservations", "bookings", session, sb.toString(), msg, err);
    }

    // -------------------------------------------------------------------------
    // ADMIN USERS & BOOKING HISTORY MANAGEMENT VIEW
    // -------------------------------------------------------------------------

    public static String renderAdminUsers(SessionManager.Session session, List<User> users, User selectedUser, List<Booking> userHistory, String msg, String err) {
        StringBuilder sb = new StringBuilder();

        sb.append("<div class=\"page-header\">");
        sb.append("  <div>");
        sb.append("    <h1>👑 Admin User Management &amp; Activity Audit</h1>");
        sb.append("    <p class=\"subtitle\">Full administrative access to all registered users, their booking logs, and passenger demographics</p>");
        sb.append("  </div>");
        sb.append("</div>");

        // Aggregated Stats
        int totalUsers = users != null ? users.size() : 0;
        int totalBookingsCount = 0;
        double totalSystemRevenue = 0.0;
        if (users != null) {
            for (User u : users) {
                totalBookingsCount += u.getBookingCount();
                totalSystemRevenue += u.getTotalSpent();
            }
        }

        sb.append("<div class=\"stats-grid\">");
        sb.append("  <div class=\"stat-card\">");
        sb.append("    <div class=\"stat-icon\">👥</div>");
        sb.append("    <div class=\"stat-info\">");
        sb.append("      <span class=\"stat-value\">").append(totalUsers).append("</span>");
        sb.append("      <span class=\"stat-label\">Registered Accounts</span>");
        sb.append("    </div>");
        sb.append("  </div>");
        sb.append("  <div class=\"stat-card\">");
        sb.append("    <div class=\"stat-icon\">🎟️</div>");
        sb.append("    <div class=\"stat-info\">");
        sb.append("      <span class=\"stat-value\">").append(totalBookingsCount).append("</span>");
        sb.append("      <span class=\"stat-label\">Total User Bookings</span>");
        sb.append("    </div>");
        sb.append("  </div>");
        sb.append("  <div class=\"stat-card\">");
        sb.append("    <div class=\"stat-icon\">💰</div>");
        sb.append("    <div class=\"stat-info\">");
        sb.append("      <span class=\"stat-value\">$").append(String.format("%.2f", totalSystemRevenue)).append("</span>");
        sb.append("      <span class=\"stat-label\">Customer Revenue</span>");
        sb.append("    </div>");
        sb.append("  </div>");
        sb.append("</div>");

        // If a specific user is selected for detailed audit
        if (selectedUser != null) {
            sb.append("<div class=\"card\" style=\"margin-bottom: 2rem; border-color: var(--primary);\">");
            sb.append("  <div class=\"card-header\">");
            sb.append("    <div>");
            sb.append("      <h3>🔍 Detailed Activity History: @").append(WebUtils.escapeHtml(selectedUser.getUsername())).append("</h3>");
            sb.append("      <p class=\"subtitle\">Email: <code>").append(WebUtils.escapeHtml(selectedUser.getEmail())).append("</code> &bull; Role: <span class=\"role-tag\">").append(selectedUser.getRole()).append("</span> &bull; User ID: #").append(selectedUser.getId()).append("</p>");
            sb.append("    </div>");
            sb.append("    <a href=\"/admin/users\" class=\"btn btn-sm btn-outline\">&larr; Close Drilldown</a>");
            sb.append("  </div>");

            sb.append("  <div class=\"table-responsive\">");
            sb.append("    <table class=\"table\">");
            sb.append("      <thead>");
            sb.append("        <tr>");
            sb.append("          <th>Booking #</th>");
            sb.append("          <th>Travel Date</th>");
            sb.append("          <th>Bus &amp; Route</th>");
            sb.append("          <th>Seat #</th>");
            sb.append("          <th>Passenger Details</th>");
            sb.append("          <th>Disability Status</th>");
            sb.append("          <th>Fare Paid</th>");
            sb.append("          <th>Status</th>");
            sb.append("          <th>Action</th>");
            sb.append("        </tr>");
            sb.append("      </thead><tbody>");

            if (userHistory == null || userHistory.isEmpty()) {
                sb.append("      <tr><td colspan=\"9\" class=\"text-center text-muted\">This user has not placed any reservations yet.</td></tr>");
            } else {
                for (Booking b : userHistory) {
                    boolean confirmed = "CONFIRMED".equalsIgnoreCase(b.getStatus());
                    sb.append("      <tr>");
                    sb.append("        <td><strong>#").append(b.getBookingId()).append("</strong></td>");
                    sb.append("        <td>").append(WebUtils.escapeHtml(b.getBookingDate())).append("</td>");
                    sb.append("        <td>");
                    sb.append("          <strong>").append(WebUtils.escapeHtml(b.getBusName() != null ? b.getBusName() : "Bus #" + b.getBusId())).append("</strong><br>");
                    sb.append("          <small class=\"text-muted\">").append(WebUtils.escapeHtml(b.getSource())).append(" &rarr; ").append(WebUtils.escapeHtml(b.getDestination())).append("</small>");
                    sb.append("        </td>");
                    sb.append("        <td><span class=\"badge badge-seat\">Seat ").append(b.getSeatNumber()).append("</span></td>");
                    sb.append("        <td>");
                    sb.append("          <strong>").append(WebUtils.escapeHtml(b.getPassengerName() != null ? b.getPassengerName() : "ID: " + b.getPassengerId())).append("</strong><br>");
                    String gIcon = "👨";
                    if ("Female".equalsIgnoreCase(b.getPassengerGender())) gIcon = "👩";
                    else if ("Other".equalsIgnoreCase(b.getPassengerGender())) gIcon = "🧑";
                    sb.append("          <small class=\"text-muted\">").append(gIcon).append(" ").append(WebUtils.escapeHtml(b.getPassengerGender() != null ? b.getPassengerGender() : "")).append(", ").append(b.getPassengerAge()).append(" yrs &bull; ").append(WebUtils.escapeHtml(b.getPassengerPhone() != null ? b.getPassengerPhone() : "")).append("</small>");
                    sb.append("        </td>");
                    sb.append("        <td>");
                    if (b.isPassengerDisability() || b.isConcession()) {
                        sb.append("<span class=\"badge badge-pwd\" title=\"UDID: ").append(WebUtils.escapeHtml(b.getDisabilityId() != null ? b.getDisabilityId() : "N/A")).append("\">♿ PwD (").append(WebUtils.escapeHtml(b.getDisabilityType() != null ? b.getDisabilityType() : "Verified")).append(")</span>");
                    } else {
                        sb.append("<span class=\"badge badge-outline\">Standard</span>");
                    }
                    sb.append("        </td>");
                    sb.append("        <td>");
                    sb.append("          <strong>$").append(String.format("%.2f", b.getFare())).append("</strong>");
                    if (b.isConcession()) {
                        sb.append("<br><small class=\"text-success\">Saved: -$").append(String.format("%.2f", b.getConcessionAmount())).append("</small>");
                    }
                    sb.append("        </td>");
                    sb.append("        <td><span class=\"badge badge-").append(confirmed ? "confirmed" : "cancelled").append("\">").append(WebUtils.escapeHtml(b.getStatus())).append("</span></td>");
                    sb.append("        <td><a href=\"/tickets?id=").append(b.getBookingId()).append("\" class=\"btn btn-xs btn-primary\">Pass &rarr;</a></td>");
                    sb.append("      </tr>");
                }
            }
            sb.append("      </tbody>");
            sb.append("    </table>");
            sb.append("  </div>");
            sb.append("</div>");
        }

        // All Users Directory Table
        sb.append("<div class=\"card\">");
        sb.append("  <div class=\"card-header\">");
        sb.append("    <h3>All Registered User Accounts (").append(totalUsers).append(")</h3>");
        sb.append("  </div>");
        sb.append("  <div class=\"table-responsive\">");
        sb.append("    <table class=\"table\">");
        sb.append("      <thead>");
        sb.append("        <tr>");
        sb.append("          <th>ID</th>");
        sb.append("          <th>Username</th>");
        sb.append("          <th>Email Address</th>");
        sb.append("          <th>Role</th>");
        sb.append("          <th>Registered On</th>");
        sb.append("          <th>Total Bookings</th>");
        sb.append("          <th>Total Spent</th>");
        sb.append("          <th>Actions</th>");
        sb.append("        </tr>");
        sb.append("      </thead><tbody>");

        if (users == null || users.isEmpty()) {
            sb.append("      <tr><td colspan=\"8\" class=\"text-center text-muted\">No user accounts registered.</td></tr>");
        } else {
            for (User u : users) {
                boolean isSelected = selectedUser != null && selectedUser.getId() == u.getId();
                sb.append("      <tr style=\"").append(isSelected ? "background: rgba(99, 102, 241, 0.12);" : "").append("\">");
                sb.append("        <td><strong>#").append(u.getId()).append("</strong></td>");
                sb.append("        <td><strong>@").append(WebUtils.escapeHtml(u.getUsername())).append("</strong></td>");
                sb.append("        <td><code>").append(WebUtils.escapeHtml(u.getEmail())).append("</code></td>");
                sb.append("        <td><span class=\"role-tag\">").append(u.getRole()).append("</span></td>");
                sb.append("        <td>").append(u.getCreatedAt() != null ? u.getCreatedAt().toString().substring(0, 10) : "N/A").append("</td>");
                sb.append("        <td><span class=\"badge badge-seat\">").append(u.getBookingCount()).append(" Bookings</span></td>");
                sb.append("        <td><strong class=\"text-success\">$").append(String.format("%.2f", u.getTotalSpent())).append("</strong></td>");
                sb.append("        <td>");
                sb.append("          <a href=\"/admin/users?user_id=").append(u.getId()).append("\" class=\"btn btn-xs btn-primary\">View History &amp; Passengers &rarr;</a>");
                sb.append("        </td>");
                sb.append("      </tr>");
            }
        }

        sb.append("      </tbody>");
        sb.append("    </table>");
        sb.append("  </div>");
        sb.append("</div>");

        return htmlDoc("Admin Users & History", "admin-users", session, sb.toString(), msg, err);
    }

    // -------------------------------------------------------------------------
    // TICKETS & BOARDING PASS VIEW (With Animated Checkmark & Concession Details)
    // -------------------------------------------------------------------------

    public static String renderTickets(SessionManager.Session session, Booking ticket, List<Booking> userBookings, String msg, String err) {
        StringBuilder sb = new StringBuilder();

        sb.append("<div class=\"page-header no-print\">");
        sb.append("  <div>");
        sb.append("    <h1>Boarding Pass &amp; Confirmation</h1>");
        sb.append("    <p class=\"subtitle\">Official travel ticket verification with print and receipt download</p>");
        sb.append("  </div>");
        sb.append("</div>");

        // Search or select ticket
        sb.append("<div class=\"card search-card no-print\">");
        sb.append("  <form action=\"/tickets\" method=\"GET\" class=\"search-form\">");
        sb.append("    <div class=\"search-input-group\">");
        sb.append("      <input type=\"number\" name=\"id\" placeholder=\"Enter Booking ID (e.g. 1001)...\" value=\"").append(ticket != null ? ticket.getBookingId() : "").append("\" required />");
        sb.append("      <button type=\"submit\" class=\"btn btn-primary\">Find Ticket &rarr;</button>");
        sb.append("    </div>");
        sb.append("  </form>");
        if (userBookings != null && !userBookings.isEmpty()) {
            sb.append("  <div class=\"quick-ticket-picks\">");
            sb.append("    <small>Your recent tickets: </small>");
            int count = 0;
            for (Booking b : userBookings) {
                if (count++ >= 5) break;
                sb.append("    <a href=\"/tickets?id=").append(b.getBookingId()).append("\" class=\"badge badge-outline\">#").append(b.getBookingId()).append(" (Seat ").append(b.getSeatNumber()).append(")</a> ");
            }
            sb.append("  </div>");
        }
        sb.append("</div>");

        if (ticket != null) {
            boolean isConfirmed = "CONFIRMED".equalsIgnoreCase(ticket.getStatus());

            // Success Confirmation Banner with Animated Checkmark
            if (isConfirmed) {
                sb.append("<div class=\"confirmation-card\">");
                sb.append("  <div class=\"checkmark-wrapper\">");
                sb.append("    <svg class=\"checkmark-svg\" viewBox=\"0 0 52 52\">");
                sb.append("      <circle class=\"checkmark-circle\" cx=\"26\" cy=\"26\" r=\"24\" fill=\"none\" />");
                sb.append("      <path class=\"checkmark-check\" fill=\"none\" d=\"M14 27l8 8 16-16\" />");
                sb.append("    </svg>");
                sb.append("  </div>");
                sb.append("  <div class=\"confirmation-text\">");
                sb.append("    <h2>Booking Confirmed!</h2>");
                sb.append("    <p>Your seat is locked and official boarding pass #<strong>").append(ticket.getBookingId()).append("</strong> is ready for travel.</p>");
                sb.append("  </div>");
                sb.append("</div>");
            }

            // Boarding pass card
            sb.append("<div class=\"boarding-pass\">");
            sb.append("  <div class=\"pass-header\">");
            sb.append("    <div class=\"pass-brand\">");
            sb.append("      <span class=\"pass-icon\">🚍</span>");
            sb.append("      <div>");
            sb.append("        <h2>SwiftBus Express Lines</h2>");
            sb.append("        <small>Official Passenger Boarding Pass</small>");
            sb.append("      </div>");
            sb.append("    </div>");
            sb.append("    <div class=\"pass-status\">");
            sb.append("      <span class=\"badge badge-").append(isConfirmed ? "confirmed" : "cancelled").append("\">").append(ticket.getStatus()).append("</span>");
            sb.append("    </div>");
            sb.append("  </div>");

            sb.append("  <div class=\"pass-body\">");
            sb.append("    <div class=\"pass-route-banner\">");
            sb.append("      <div class=\"route-station\">");
            sb.append("        <span class=\"station-label\">ORIGIN</span>");
            sb.append("        <span class=\"station-name\">").append(WebUtils.escapeHtml(ticket.getSource() != null ? ticket.getSource() : "N/A")).append("</span>");
            sb.append("      </div>");
            sb.append("      <div class=\"route-arrow\">🚍 ──➔ 🚏</div>");
            sb.append("      <div class=\"route-station\">");
            sb.append("        <span class=\"station-label\">DESTINATION</span>");
            sb.append("        <span class=\"station-name\">").append(WebUtils.escapeHtml(ticket.getDestination() != null ? ticket.getDestination() : "N/A")).append("</span>");
            sb.append("      </div>");
            sb.append("    </div>");

            sb.append("    <div class=\"pass-details-grid\">");
            sb.append("      <div class=\"pass-field\">");
            sb.append("        <span class=\"field-label\">PASSENGER NAME</span>");
            sb.append("        <span class=\"field-value\">").append(WebUtils.escapeHtml(ticket.getPassengerName() != null ? ticket.getPassengerName() : "Passenger #" + ticket.getPassengerId())).append("</span>");
            sb.append("      </div>");
            sb.append("      <div class=\"pass-field\">");
            sb.append("        <span class=\"field-label\">SEAT NUMBER</span>");
            sb.append("        <span class=\"field-value highlight\">Seat #").append(ticket.getSeatNumber()).append("</span>");
            sb.append("      </div>");
            sb.append("      <div class=\"pass-field\">");
            sb.append("        <span class=\"field-label\">BUS NAME / FLEET</span>");
            sb.append("        <span class=\"field-value\">").append(WebUtils.escapeHtml(ticket.getBusName() != null ? ticket.getBusName() : "Bus #" + ticket.getBusId())).append("</span>");
            sb.append("      </div>");
            sb.append("      <div class=\"pass-field\">");
            sb.append("        <span class=\"field-label\">DEPARTURE DATE</span>");
            sb.append("        <span class=\"field-value\">").append(WebUtils.escapeHtml(ticket.getBookingDate())).append("</span>");
            sb.append("      </div>");
            sb.append("      <div class=\"pass-field\">");
            sb.append("        <span class=\"field-label\">PASSENGER CONTACT</span>");
            sb.append("        <span class=\"field-value\">").append(WebUtils.escapeHtml(ticket.getPassengerPhone() != null ? ticket.getPassengerPhone() : "N/A")).append("</span>");
            sb.append("      </div>");
            sb.append("      <div class=\"pass-field\">");
            sb.append("        <span class=\"field-label\">TOTAL FARE PAID</span>");
            sb.append("        <span class=\"field-value text-success\">$").append(String.format("%.2f", ticket.getFare())).append("</span>");
            sb.append("      </div>");

            // Disability Concession verification callout on ticket
            if (ticket.isConcession()) {
                sb.append("      <div class=\"pass-field\" style=\"grid-column: 1 / -1;\">");
                sb.append("        <span class=\"field-label\">SPECIAL ASSISTANCE &amp; CONCESSION</span>");
                sb.append("        <span class=\"field-value\"><span class=\"badge badge-pwd\">♿ 50% Disability Concession Verified (Saved: $").append(String.format("%.2f", ticket.getConcessionAmount())).append(")</span></span>");
                sb.append("      </div>");
            }

            sb.append("    </div>");

            sb.append("    <div class=\"pass-barcode-area\">");
            sb.append("      <div class=\"barcode-lines\">||||| | |||| ||| ||||||| | ||| ||||| |||| | ||| |||||| ||||</div>");
            sb.append("      <div class=\"barcode-num\">SWIFTBUS-PASS-").append(ticket.getBookingId()).append("-SEAT-").append(ticket.getSeatNumber()).append("</div>");
            sb.append("      <div class=\"pass-copyright\">&copy; 2026 SwiftBus Express Lines. All Rights Reserved. Official Travel Document.</div>");
            sb.append("    </div>");
            sb.append("  </div>");

            sb.append("  <div class=\"pass-actions no-print\">");
            sb.append("    <button onclick=\"window.print();\" class=\"btn btn-primary\">🖨️ Print Boarding Pass</button>");
            sb.append("    <a href=\"/tickets/download?id=").append(ticket.getBookingId()).append("\" class=\"btn btn-secondary\">📥 Download Receipt (.txt)</a>");
            sb.append("    <a href=\"/bookings\" class=\"btn btn-outline-dark\">&larr; Back to Reservations</a>");
            sb.append("  </div>");
            sb.append("</div>");
        }

        return htmlDoc("Boarding Pass", "tickets", session, sb.toString(), msg, err);
    }

    // -------------------------------------------------------------------------
    // VANILLA CSS STYLESHEET (GPU-Accelerated, Reduced-Motion, Zero Web-Fonts)
    // -------------------------------------------------------------------------

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
                "  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;" +
                "  background-color: var(--bg-primary);" +
                "  color: var(--text-main);" +
                "  line-height: 1.6;" +
                "  min-height: 100vh;" +
                "  display: flex;" +
                "  flex-direction: column;" +
                "}" +
                "a { color: var(--primary); text-decoration: none; transition: opacity 0.2s ease; }" +
                "a:hover { opacity: 0.85; }" +
                "" +
                "/* Navbar */" +
                ".navbar {" +
                "  background: #111827;" +
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
                ".brand { display: flex; align-items: center; gap: 0.6rem; color: #fff; font-weight: 800; font-size: 1.3rem; }" +
                ".brand-icon { font-size: 1.6rem; }" +
                ".brand-badge { font-size: 0.75rem; background: var(--primary-gradient); padding: 0.15rem 0.5rem; border-radius: 9999px; font-weight: 700; }" +
                ".nav-links { display: flex; gap: 0.5rem; align-items: center; flex-wrap: wrap; }" +
                ".nav-link { color: var(--text-muted); font-weight: 600; font-size: 0.95rem; padding: 0.4rem 0.85rem; border-radius: 8px; transition: opacity 0.2s; }" +
                ".nav-link:hover, .nav-link.active { color: #fff; background: rgba(99, 102, 241, 0.15); }" +
                ".nav-user { display: flex; align-items: center; gap: 0.75rem; }" +
                ".user-badge { font-size: 0.85rem; color: var(--text-muted); font-weight: 600; background: rgba(255,255,255,0.06); padding: 0.35rem 0.75rem; border-radius: 8px; }" +
                ".role-tag { font-size: 0.65rem; background: var(--primary); color: #fff; padding: 0.1rem 0.4rem; border-radius: 4px; margin-left: 0.3rem; font-weight: 700; }" +
                "" +
                "/* Main Container */" +
                ".main-content { max-width: 1280px; width: 100%; margin: 0 auto; padding: 2rem 1.5rem; flex: 1; }" +
                ".page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 2rem; flex-wrap: wrap; gap: 1rem; }" +
                ".page-header h1 { font-size: 1.85rem; font-weight: 800; }" +
                ".subtitle { color: var(--text-muted); font-size: 0.95rem; margin-top: 0.25rem; }" +
                ".header-actions { display: flex; gap: 0.75rem; }" +
                "" +
                "/* Alerts */" +
                ".alert { padding: 0.85rem 1.25rem; border-radius: 8px; margin-bottom: 1.5rem; font-weight: 500; font-size: 0.95rem; display: flex; align-items: center; }" +
                ".alert-success { background: rgba(16, 185, 129, 0.15); border: 1px solid rgba(16, 185, 129, 0.3); color: #34d399; }" +
                ".alert-danger { background: rgba(239, 68, 68, 0.15); border: 1px solid rgba(239, 68, 68, 0.3); color: #f87171; }" +
                "" +
                "/* Buttons */" +
                ".btn { display: inline-flex; align-items: center; justify-content: center; gap: 0.5rem; font-weight: 600; font-size: 0.95rem; padding: 0.6rem 1.25rem; border-radius: 8px; border: none; cursor: pointer; transition: transform 0.15s ease, opacity 0.15s ease; text-decoration: none; }" +
                ".btn:hover { transform: translateY(-1px); }" +
                ".btn:active { transform: translateY(0); }" +
                ".btn-primary { background: var(--primary); color: #fff; box-shadow: 0 4px 12px rgba(99, 102, 241, 0.3); }" +
                ".btn-primary:hover { background: var(--primary-hover); }" +
                ".btn-secondary { background: #374151; color: #fff; }" +
                ".btn-secondary:hover { background: #4b5563; }" +
                ".btn-outline { background: transparent; border: 1px solid var(--border-color); color: var(--text-main); }" +
                ".btn-outline:hover { background: rgba(255,255,255,0.05); border-color: var(--text-muted); }" +
                ".btn-outline-danger { background: transparent; border: 1px solid rgba(239, 68, 68, 0.4); color: #f87171; }" +
                ".btn-outline-danger:hover { background: rgba(239, 68, 68, 0.15); }" +
                ".btn-outline-warning { background: transparent; border: 1px solid rgba(245, 158, 11, 0.4); color: #fbbf24; }" +
                ".btn-outline-warning:hover { background: rgba(245, 158, 11, 0.15); }" +
                ".btn-block { width: 100%; }" +
                ".btn-sm { padding: 0.35rem 0.75rem; font-size: 0.85rem; border-radius: 6px; }" +
                ".btn-xs { padding: 0.25rem 0.55rem; font-size: 0.75rem; border-radius: 6px; }" +
                "" +
                "/* Stats Grid */" +
                ".stats-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 1.25rem; margin-bottom: 2rem; }" +
                ".stat-card { background: var(--bg-card); border: 1px solid var(--border-color); border-radius: var(--radius); padding: 1.25rem; display: flex; align-items: center; gap: 1rem; box-shadow: var(--shadow); }" +
                ".stat-icon { font-size: 2.2rem; background: rgba(255, 255, 255, 0.05); padding: 0.75rem; border-radius: 12px; }" +
                ".stat-value { font-size: 1.6rem; font-weight: 800; display: block; line-height: 1.2; }" +
                ".stat-label { font-size: 0.85rem; color: var(--text-muted); font-weight: 600; text-transform: uppercase; letter-spacing: 0.05em; }" +
                "" +
                "/* Quick Actions */" +
                ".section-title { margin-bottom: 1rem; font-weight: 700; }" +
                ".quick-actions-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(240px, 1fr)); gap: 1rem; margin-bottom: 2rem; }" +
                ".quick-card { background: var(--bg-card); border: 1px solid var(--border-color); border-radius: var(--radius); padding: 1.25rem; transition: transform 0.2s ease, opacity 0.2s ease; display: block; }" +
                ".quick-card:hover { transform: translateY(-3px); background: var(--bg-card-hover); border-color: var(--primary); }" +
                ".quick-icon { font-size: 1.8rem; display: block; margin-bottom: 0.5rem; }" +
                ".quick-card h4 { color: #fff; font-size: 1.05rem; margin-bottom: 0.25rem; }" +
                ".quick-card p { color: var(--text-muted); font-size: 0.85rem; line-height: 1.4; }" +
                "" +
                "/* Cards & Tables */" +
                ".card { background: var(--bg-card); border: 1px solid var(--border-color); border-radius: var(--radius); padding: 1.5rem; box-shadow: var(--shadow); }" +
                ".card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.25rem; padding-bottom: 0.75rem; border-bottom: 1px solid var(--border-color); flex-wrap: wrap; gap: 0.5rem; }" +
                ".table-responsive { overflow-x: auto; }" +
                ".table { width: 100%; border-collapse: collapse; font-size: 0.92rem; text-align: left; }" +
                ".table th { color: var(--text-muted); font-weight: 600; padding: 0.75rem 1rem; border-bottom: 1px solid var(--border-color); text-transform: uppercase; font-size: 0.75rem; letter-spacing: 0.05em; }" +
                ".table td { padding: 0.85rem 1rem; border-bottom: 1px solid rgba(255,255,255,0.04); vertical-align: middle; }" +
                ".table tr:hover td { background: rgba(255,255,255,0.02); }" +
                ".actions-cell { display: flex; gap: 0.4rem; align-items: center; }" +
                "" +
                "/* Layout */" +
                ".grid-form-table { display: grid; grid-template-columns: 360px 1fr; gap: 1.5rem; }" +
                ".grid-booking-layout { display: grid; grid-template-columns: 1fr 400px; gap: 1.5rem; }" +
                "@media (max-width: 960px) {" +
                "  .grid-form-table, .grid-booking-layout { grid-template-columns: 1fr; }" +
                "}" +
                "" +
                "/* Forms */" +
                ".form-group { margin-bottom: 1.1rem; }" +
                ".form-group label { display: block; font-size: 0.85rem; font-weight: 600; color: var(--text-muted); margin-bottom: 0.4rem; }" +
                ".form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 0.85rem; }" +
                "input, select, textarea {" +
                "  width: 100%; padding: 0.65rem 0.95rem; background: #0c1322; border: 1px solid var(--border-color); border-radius: 8px; color: #fff; font-size: 0.92rem; outline: none; transition: border-color 0.2s;" +
                "}" +
                "input:focus, select:focus, textarea:focus { border-color: var(--border-focus); box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.2); }" +
                "" +
                "/* Badges */" +
                ".badge { display: inline-block; padding: 0.2rem 0.6rem; border-radius: 9999px; font-size: 0.75rem; font-weight: 700; }" +
                ".badge-confirmed { background: rgba(16, 185, 129, 0.15); color: #34d399; border: 1px solid rgba(16, 185, 129, 0.3); }" +
                ".badge-cancelled { background: rgba(239, 68, 68, 0.15); color: #f87171; border: 1px solid rgba(239, 68, 68, 0.3); }" +
                ".badge-seat { background: rgba(99, 102, 241, 0.15); color: #a5b4fc; }" +
                ".badge-gender { background: rgba(255,255,255,0.08); color: var(--text-main); }" +
                ".badge-outline { border: 1px solid var(--border-color); color: var(--text-muted); text-decoration: none; }" +
                ".badge-outline:hover { border-color: var(--primary); color: #fff; }" +
                ".badge-pwd { background: rgba(16, 185, 129, 0.18); border: 1px solid rgba(16, 185, 129, 0.4); color: #34d399; font-weight: 700; padding: 0.2rem 0.6rem; border-radius: 6px; font-size: 0.78rem; }" +
                ".route-badge { background: rgba(255,255,255,0.05); padding: 0.25rem 0.6rem; border-radius: 6px; font-weight: 600; font-size: 0.85rem; }" +
                "" +
                "/* Stepper Bar */" +
                ".booking-stepper-container { display: flex; align-items: center; justify-content: space-between; background: var(--bg-card); border: 1px solid var(--border-color); border-radius: var(--radius); padding: 1.25rem 2rem; margin-bottom: 1.5rem; flex-wrap: wrap; gap: 0.75rem; }" +
                ".step-item { display: flex; align-items: center; gap: 0.65rem; opacity: 0.45; transition: opacity 0.3s ease; }" +
                ".step-item.active { opacity: 1; }" +
                ".step-item.completed { opacity: 0.85; }" +
                ".step-badge { width: 32px; height: 32px; border-radius: 50%; background: #24324a; color: #fff; display: flex; align-items: center; justify-content: center; font-weight: 700; font-size: 0.85rem; transition: transform 0.2s ease; }" +
                ".step-item.active .step-badge { background: var(--primary); transform: scale(1.1); box-shadow: 0 0 12px rgba(99, 102, 241, 0.5); }" +
                ".step-item.completed .step-badge { background: var(--success); }" +
                ".step-title { font-size: 0.85rem; font-weight: 600; }" +
                ".step-line { flex: 1; height: 2px; background: var(--border-color); margin: 0 1rem; min-width: 20px; }" +
                "" +
                "/* Visual Bus Floorplan & Seat Matrix */" +
                ".bus-floorplan { background: #0c1322; border: 2px dashed var(--border-color); border-radius: 16px; padding: 1.25rem; max-width: 480px; margin: 1rem auto; }" +
                ".bus-front { text-align: center; padding: 0.5rem; background: rgba(255,255,255,0.04); border-radius: 8px; font-size: 0.8rem; font-weight: 700; color: var(--text-muted); margin-bottom: 1rem; border-bottom: 1px solid var(--border-color); }" +
                ".bus-back { text-align: center; padding: 0.4rem; color: var(--text-muted); font-size: 0.75rem; margin-top: 1rem; }" +
                ".seat-matrix { display: grid; grid-template-columns: repeat(4, 1fr); gap: 0.75rem; justify-items: center; }" +
                ".seat-matrix > button:nth-child(4n-2) { margin-right: 1.25rem; } /* Center Aisle */" +
                ".seat-btn {" +
                "  width: 54px; height: 50px; border-radius: 10px; border: 2px solid #2d3e58; background: #162238; color: #fff; font-weight: 700; font-size: 0.9rem; cursor: pointer;" +
                "  display: flex; flex-direction: column; align-items: center; justify-content: center; position: relative;" +
                "  transition: transform 0.2s ease, opacity 0.2s ease, border-color 0.2s ease;" +
                "  will-change: transform, opacity;" +
                "}" +
                ".seat-btn.available:hover {" +
                "  transform: translateY(-2px) scale(1.06);" +
                "  border-color: var(--primary);" +
                "  background: #233554;" +
                "}" +
                ".seat-btn.selected {" +
                "  background: var(--primary);" +
                "  border-color: #818cf8;" +
                "  transform: scale(1.12);" +
                "  box-shadow: 0 0 15px rgba(99, 102, 241, 0.6);" +
                "}" +
                ".seat-btn.occupied {" +
                "  cursor: not-allowed;" +
                "}" +
                ".seat-btn.occupied-male {" +
                "  background: #1e3a8a;" +
                "  border-color: #3b82f6;" +
                "  color: #bfdbfe;" +
                "  opacity: 0.9;" +
                "}" +
                ".seat-btn.occupied-female {" +
                "  background: #831843;" +
                "  border-color: #ec4899;" +
                "  color: #fbcfe8;" +
                "  opacity: 0.9;" +
                "}" +
                ".seat-btn.occupied-other {" +
                "  background: #4c1d95;" +
                "  border-color: #a855f7;" +
                "  color: #e9d5ff;" +
                "  opacity: 0.9;" +
                "}" +
                ".seat-occupant { font-size: 0.72rem; line-height: 1; margin-top: 2px; }" +
                ".seat-legend { display: flex; gap: 1rem; justify-content: center; margin-bottom: 1rem; font-size: 0.82rem; flex-wrap: wrap; }" +
                ".legend-item { display: flex; align-items: center; gap: 0.4rem; }" +
                ".seat-preview { width: 16px; height: 16px; border-radius: 4px; display: inline-block; }" +
                ".seat-preview.available { background: #162238; border: 1px solid #2d3e58; }" +
                ".seat-preview.selected { background: var(--primary); }" +
                ".seat-preview.occupied-male { background: #1e3a8a; border: 1px solid #3b82f6; }" +
                ".seat-preview.occupied-female { background: #831843; border: 1px solid #ec4899; }" +
                ".seat-preview.occupied-other { background: #4c1d95; border: 1px solid #a855f7; }" +
                "" +
                "/* Real-Time Neighbor & Gender Radar Card */" +
                ".neighbor-radar-card { background: #0d1526; border: 1px solid #3b82f6; border-radius: 12px; padding: 0.95rem 1.25rem; margin-top: 1rem; box-shadow: 0 4px 15px rgba(0,0,0,0.3); }" +
                ".radar-header { display: flex; align-items: center; gap: 0.5rem; font-size: 0.92rem; font-weight: 700; color: #93c5fd; margin-bottom: 0.5rem; }" +
                ".radar-pulse { width: 10px; height: 10px; background: #3b82f6; border-radius: 50%; box-shadow: 0 0 8px #60a5fa; animation: pulse 1.5s infinite; }" +
                "@keyframes pulse { 0% { transform: scale(0.9); opacity: 0.7; } 50% { transform: scale(1.2); opacity: 1; } 100% { transform: scale(0.9); opacity: 0.7; } }" +
                ".radar-body { font-size: 0.86rem; color: #cbd5e1; line-height: 1.5; }" +
                ".radar-item { display: flex; align-items: center; gap: 0.5rem; }" +
                ".radar-badge { display: inline-flex; align-items: center; gap: 0.25rem; padding: 0.15rem 0.45rem; border-radius: 6px; font-size: 0.75rem; font-weight: 700; }" +
                ".radar-badge.male { background: rgba(59, 130, 246, 0.2); color: #93c5fd; border: 1px solid #3b82f6; }" +
                ".radar-badge.female { background: rgba(236, 72, 153, 0.2); color: #fbcfe8; border: 1px solid #ec4899; }" +
                ".radar-badge.other { background: rgba(168, 85, 247, 0.2); color: #e9d5ff; border: 1px solid #a855f7; }" +
                ".radar-badge.free { background: rgba(16, 185, 129, 0.2); color: #34d399; border: 1px solid #10b981; }" +
                "" +
                "/* Multiple Booking Selected Badges Row */" +
                ".selected-seats-badge-row { display: flex; flex-wrap: wrap; gap: 0.4rem; min-height: 42px; align-items: center; padding: 0.5rem 0.75rem; background: #0c1322; border: 1px solid var(--border-color); border-radius: 8px; margin-bottom: 0.75rem; }" +
                ".seat-tag { background: var(--primary); color: #fff; padding: 0.2rem 0.6rem; border-radius: 6px; font-size: 0.82rem; font-weight: 700; display: inline-flex; align-items: center; gap: 0.4rem; }" +
                ".seat-tag-remove { cursor: pointer; font-weight: 800; opacity: 0.8; }" +
                ".seat-tag-remove:hover { opacity: 1; color: #fca5a5; }" +
                "" +
                "/* Per-Seat Passenger Cards */" +
                ".per-seat-passengers-section { margin-top: 1rem; margin-bottom: 1.25rem; }" +
                ".section-subheading { display: flex; flex-direction: column; gap: 0.15rem; margin-bottom: 0.75rem; }" +
                ".section-subheading span { font-weight: 700; font-size: 0.95rem; color: #fff; }" +
                ".text-muted-box { padding: 0.85rem 1rem; background: #0c1322; border: 1px dashed var(--border-color); border-radius: 8px; font-size: 0.85rem; color: var(--text-muted); text-align: center; }" +
                ".seat-passenger-card { background: #0c1322; border: 1px solid var(--border-color); border-radius: 10px; padding: 0.85rem 1rem; margin-bottom: 0.85rem; transition: border-color 0.2s ease; }" +
                ".seat-passenger-card:hover { border-color: rgba(99, 102, 241, 0.4); }" +
                ".seat-card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.6rem; flex-wrap: wrap; gap: 0.4rem; }" +
                ".seat-card-title { display: flex; align-items: center; gap: 0.5rem; font-weight: 700; font-size: 0.88rem; color: #a5b4fc; }" +
                ".seat-badge-pill { background: var(--primary); color: #fff; font-size: 0.75rem; padding: 0.15rem 0.5rem; border-radius: 9999px; font-weight: 800; }" +
                ".autofill-select { width: auto; font-size: 0.75rem; padding: 0.25rem 0.5rem; background: #162238; border: 1px solid #2d3e58; border-radius: 6px; color: #cbd5e1; cursor: pointer; }" +
                ".seat-card-grid { display: flex; flex-direction: column; gap: 0.5rem; }" +
                ".seat-card-row { display: flex; gap: 0.5rem; align-items: center; }" +
                ".seat-card-row input, .seat-card-row select { font-size: 0.85rem; padding: 0.45rem 0.65rem; }" +
                ".concession-mini-label { display: inline-flex; align-items: center; gap: 0.35rem; font-size: 0.78rem; color: #34d399; font-weight: 600; cursor: pointer; white-space: nowrap; }" +
                ".concession-mini-label input { width: 15px; height: 15px; cursor: pointer; }" +
                "" +
                "/* Concession & Fare Breakdown */" +
                ".concession-box { background: rgba(16, 185, 129, 0.08); border: 1px solid rgba(16, 185, 129, 0.3); border-radius: 10px; padding: 0.85rem 1rem; margin-bottom: 1.1rem; }" +
                ".concession-toggle-label { display: flex; align-items: center; gap: 0.6rem; cursor: pointer; font-size: 0.95rem; color: #34d399; }" +
                ".concession-toggle-label input { width: 18px; height: 18px; cursor: pointer; }" +
                ".fare-breakdown-card { background: #0c1322; border: 1px solid var(--border-color); border-radius: 10px; padding: 0.85rem 1rem; margin-bottom: 1.25rem; }" +
                ".fare-row { display: flex; justify-content: space-between; font-size: 0.88rem; color: var(--text-muted); margin-bottom: 0.35rem; }" +
                ".fare-row.discount { color: #34d399; font-weight: 600; }" +
                ".fare-row.total { font-size: 1.05rem; font-weight: 800; color: #fff; border-top: 1px solid var(--border-color); padding-top: 0.5rem; margin-top: 0.5rem; }" +
                "" +
                "/* Submit Button Loading Spinner */" +
                ".spinner-svg { width: 18px; height: 18px; animation: spin 0.8s linear infinite; vertical-align: middle; margin-right: 6px; }" +
                "@keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }" +
                "" +
                "/* Error Shake Animation */" +
                ".shake-anim { animation: shake 0.4s ease-in-out; will-change: transform; }" +
                "@keyframes shake { 0%, 100% { transform: translateX(0); } 20%, 60% { transform: translateX(-7px); } 40%, 80% { transform: translateX(7px); } }" +
                "" +
                "/* Animated Success Checkmark */" +
                ".confirmation-card { background: rgba(16, 185, 129, 0.1); border: 1px solid rgba(16, 185, 129, 0.3); border-radius: var(--radius); padding: 1.5rem; display: flex; align-items: center; gap: 1.25rem; margin-bottom: 1.5rem; }" +
                ".checkmark-wrapper { width: 54px; height: 54px; flex-shrink: 0; }" +
                ".checkmark-svg { width: 54px; height: 54px; stroke: #10b981; stroke-width: 3; stroke-miterlimit: 10; }" +
                ".checkmark-circle { stroke-dasharray: 166; stroke-dashoffset: 166; animation: stroke 0.6s cubic-bezier(0.65, 0, 0.45, 1) forwards; }" +
                ".checkmark-check { transform-origin: 50% 50%; stroke-dasharray: 48; stroke-dashoffset: 48; animation: stroke 0.4s cubic-bezier(0.65, 0, 0.45, 1) 0.5s forwards; }" +
                "@keyframes stroke { 100% { stroke-dashoffset: 0; } }" +
                "" +
                "/* Accessibility: prefers-reduced-motion */" +
                "@media (prefers-reduced-motion: reduce) { *, *::before, *::after { animation-duration: 0.01ms !important; animation-iteration-count: 1 !important; transition-duration: 0.01ms !important; scroll-behavior: auto !important; } }" +
                "" +
                "/* Auth Pages */" +
                ".login-body { display: flex; align-items: center; justify-content: center; min-height: 100vh; background: radial-gradient(circle at top, #1e1b4b 0%, var(--bg-primary) 70%); padding: 1.5rem; }" +
                ".login-card { background: var(--bg-card); border: 1px solid var(--border-color); border-radius: 16px; width: 100%; max-width: 440px; padding: 2.25rem; box-shadow: 0 20px 40px rgba(0,0,0,0.5); }" +
                ".login-header { text-align: center; margin-bottom: 1.75rem; }" +
                ".login-logo { font-size: 2.8rem; margin-bottom: 0.4rem; }" +
                ".login-header h1 { font-size: 1.6rem; font-weight: 800; }" +
                ".login-header p { color: var(--text-muted); font-size: 0.88rem; }" +
                ".auth-alt-box { text-align: center; margin-top: 1.25rem; padding-top: 1.25rem; border-top: 1px solid var(--border-color); }" +
                ".auth-alt-text { font-size: 0.88rem; color: var(--text-muted); display: block; }" +
                ".login-footer { text-align: center; margin-top: 1.5rem; }" +
                ".login-copyright { font-size: 0.75rem; color: var(--text-muted); opacity: 0.6; }" +
                "" +
                "/* Strength Meter */" +
                ".strength-meter-container { margin-top: 0.5rem; }" +
                ".strength-meter { height: 6px; width: 100%; background: #0c1322; border-radius: 3px; overflow: hidden; margin-bottom: 0.4rem; }" +
                ".strength-fill { height: 100%; width: 0%; transition: width 0.3s ease; }" +
                ".strength-fill.weak { width: 33%; background: #ef4444; }" +
                ".strength-fill.better { width: 66%; background: #f59e0b; }" +
                ".strength-fill.best { width: 100%; background: #10b981; }" +
                ".strength-label { font-size: 0.78rem; color: var(--text-muted); }" +
                ".strength-badge.weak { color: #ef4444; font-weight: 700; }" +
                ".strength-badge.better { color: #f59e0b; font-weight: 700; }" +
                ".strength-badge.best { color: #10b981; font-weight: 700; }" +
                ".strength-rules { list-style: none; margin-top: 0.4rem; font-size: 0.75rem; color: var(--text-muted); }" +
                ".rule-item { margin-bottom: 0.2rem; display: flex; align-items: center; gap: 0.35rem; }" +
                ".rule-item.valid { color: #10b981; }" +
                ".rule-item.invalid { color: #94a3b8; }" +
                "" +
                "/* Boarding Pass */" +
                ".boarding-pass { background: #fff; color: #1e293b; border-radius: 16px; overflow: hidden; max-width: 680px; margin: 0 auto; box-shadow: 0 20px 40px rgba(0,0,0,0.5); }" +
                ".pass-header { background: linear-gradient(135deg, #1e293b 0%, #0f172a 100%); color: #fff; padding: 1.5rem 2rem; display: flex; justify-content: space-between; align-items: center; }" +
                ".pass-brand { display: flex; align-items: center; gap: 1rem; }" +
                ".pass-icon { font-size: 2.2rem; }" +
                ".pass-brand h2 { font-size: 1.4rem; font-weight: 800; }" +
                ".pass-brand small { color: #94a3b8; font-size: 0.8rem; text-transform: uppercase; letter-spacing: 0.05em; }" +
                ".pass-body { padding: 2rem; background: #fff; }" +
                ".pass-route-banner { display: flex; align-items: center; justify-content: space-between; background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 12px; padding: 1.25rem 1.75rem; margin-bottom: 1.75rem; }" +
                ".route-station { display: flex; flex-direction: column; }" +
                ".station-label { font-size: 0.7rem; color: #64748b; font-weight: 700; letter-spacing: 0.05em; text-transform: uppercase; }" +
                ".station-name { font-size: 1.35rem; font-weight: 800; color: #0f172a; }" +
                ".route-arrow { font-size: 1.2rem; color: #6366f1; font-weight: 700; }" +
                ".pass-details-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 1.25rem; margin-bottom: 1.75rem; }" +
                ".pass-field { display: flex; flex-direction: column; }" +
                ".field-label { font-size: 0.7rem; font-weight: 700; color: #64748b; letter-spacing: 0.05em; text-transform: uppercase; margin-bottom: 0.25rem; }" +
                ".field-value { font-size: 1.05rem; font-weight: 700; color: #0f172a; }" +
                ".field-value.highlight { color: #6366f1; font-size: 1.35rem; }" +
                ".pass-barcode-area { border-top: 2px dashed #cbd5e1; padding-top: 1.5rem; text-align: center; }" +
                ".barcode-lines { font-family: 'Courier New', monospace; font-size: 1.8rem; letter-spacing: 0.2em; color: #1e293b; }" +
                ".barcode-num { font-size: 0.75rem; color: #64748b; font-weight: 600; margin-top: 0.25rem; }" +
                ".pass-actions { padding: 1.25rem 2rem; background: #f1f5f9; display: flex; gap: 0.75rem; justify-content: center; border-top: 1px solid #e2e8f0; }" +
                ".pass-actions .btn-outline, .pass-actions .btn-outline-dark, .btn-outline-dark { background: #ffffff !important; color: #1e293b !important; border: 1px solid #cbd5e1 !important; font-weight: 600; text-decoration: none; display: inline-flex; align-items: center; justify-content: center; }" +
                ".pass-actions .btn-outline:hover, .pass-actions .btn-outline-dark:hover, .btn-outline-dark:hover { background: #e2e8f0 !important; color: #0f172a !important; border-color: #94a3b8 !important; }" +
                "" +
                "/* Print Styles */" +
                "@media print {" +
                "  body { background: #fff !important; color: #000 !important; }" +
                "  .navbar, .footer, .no-print, .page-header, .search-card, .pass-actions, .confirmation-card { display: none !important; }" +
                "  .main-content { padding: 0 !important; max-width: 100% !important; }" +
                "  .boarding-pass { box-shadow: none !important; border: 1px solid #000 !important; max-width: 100% !important; margin: 0 !important; }" +
                "}" +
                "" +
                "/* Footer */" +
                ".footer { background: #111827; border-top: 1px solid var(--border-color); padding: 2rem 1.5rem; margin-top: auto; }" +
                ".footer-container { max-width: 1280px; margin: 0 auto; text-align: center; display: flex; flex-direction: column; align-items: center; gap: 0.75rem; }" +
                ".footer-brand { display: flex; align-items: center; gap: 0.5rem; font-size: 1.05rem; color: #fff; }" +
                ".footer-nav { font-size: 0.88rem; color: var(--text-muted); }" +
                ".footer-copy { font-size: 0.75rem; color: var(--text-muted); opacity: 0.6; }" +
                "";
    }
}
