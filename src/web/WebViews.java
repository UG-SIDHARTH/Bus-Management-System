package web;

import model.Booking;
import model.Bus;
import model.Passenger;

import java.time.LocalDate;
import java.util.List;

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
                "        <input type=\"text\" id=\"username\" name=\"username\" placeholder=\"admin or your@email.com\" required autofocus />" +
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
                "      <small>Default credentials: <code>admin</code> / <code>1234</code></small>" +
                "      <p class=\"login-copyright\">&copy; 2026 SwiftBus Inc. All Rights Reserved.</p>" +
                "    </div>" +
                "  </div>" +
                "</body>" +
                "</html>";
    }

    public static String renderRegister(String err, String csrfToken, String prefUser, String prefEmail) {
        boolean hasError = err != null && !err.isEmpty();
        return "<!DOCTYPE html>" +
                "<html lang=\"en\">" +
                "<head>" +
                "  <meta charset=\"UTF-8\" />" +
                "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />" +
                "  <title>Create Account - SwiftBus Portal</title>" +
                "  <link rel=\"stylesheet\" href=\"/static/style.css\" />" +
                "</head>" +
                "<body class=\"login-body\">" +
                "  <div class=\"login-card" + (hasError ? " shake-anim" : "") + "\">" +
                "    <div class=\"login-header\">" +
                "      <div class=\"login-logo\">🚍</div>" +
                "      <h1>Create User Account</h1>" +
                "      <p>Join SwiftBus to book and manage travel seats</p>" +
                "    </div>" +
                (hasError ? "<div class=\"alert alert-danger\">⚠️ " + WebUtils.escapeHtml(err) + "</div>" : "") +
                "    <form action=\"/register\" method=\"POST\" class=\"login-form\" id=\"regForm\" onsubmit=\"return validateRegister();\">" +
                "      <input type=\"hidden\" name=\"csrf_token\" value=\"" + WebUtils.escapeHtml(csrfToken) + "\" />" +
                "      <div class=\"form-group\">" +
                "        <label for=\"username\">Username (3-30 chars, alphanumeric/underscore)</label>" +
                "        <input type=\"text\" id=\"username\" name=\"username\" pattern=\"^[a-zA-Z0-9_]{3,30}$\" minlength=\"3\" maxlength=\"30\" value=\"" + WebUtils.escapeHtml(prefUser != null ? prefUser : "") + "\" placeholder=\"e.g. john_doe\" required autofocus />" +
                "      </div>" +
                "      <div class=\"form-group\">" +
                "        <label for=\"email\">Email Address</label>" +
                "        <input type=\"email\" id=\"email\" name=\"email\" maxlength=\"100\" value=\"" + WebUtils.escapeHtml(prefEmail != null ? prefEmail : "") + "\" placeholder=\"john@example.com\" required />" +
                "      </div>" +
                "      <div class=\"form-group\">" +
                "        <label for=\"password\">Password</label>" +
                "        <input type=\"password\" id=\"password\" name=\"password\" minlength=\"8\" placeholder=\"Min 8 chars, 1 special symbol\" oninput=\"updateStrength();\" required />" +
                "        <div class=\"strength-container\" id=\"strengthContainer\" style=\"display:none;\">" +
                "          <div class=\"strength-header\">" +
                "            <span class=\"strength-label\">Password Strength:</span>" +
                "            <span id=\"strengthText\" class=\"strength-badge weak\">Weak</span>" +
                "          </div>" +
                "          <div class=\"strength-meter-bar\">" +
                "            <div id=\"strengthFill\" class=\"strength-fill weak\"></div>" +
                "          </div>" +
                "          <ul class=\"strength-rules\">" +
                "            <li id=\"rule-len\" class=\"rule-item invalid\"><span class=\"rule-icon\">○</span> At least 8 characters</li>" +
                "            <li id=\"rule-spec\" class=\"rule-item invalid\"><span class=\"rule-icon\">○</span> Special character required (!@#$%^&*...)</li>" +
                "            <li id=\"rule-bonus\" class=\"rule-item invalid\"><span class=\"rule-icon\">○</span> Mixed letters & numbers for \"Best\"</li>" +
                "          </ul>" +
                "        </div>" +
                "      </div>" +
                "      <div class=\"form-group\">" +
                "        <label for=\"confirm_password\">Confirm Password</label>" +
                "        <input type=\"password\" id=\"confirm_password\" name=\"confirm_password\" minlength=\"8\" placeholder=\"Re-enter password\" oninput=\"checkMatch();\" required />" +
                "        <small id=\"passMismatch\" class=\"text-danger\" style=\"display:none; margin-top:4px;\">Passwords do not match!</small>" +
                "      </div>" +
                "      <button type=\"submit\" class=\"btn btn-primary btn-block\" id=\"regBtn\">Register Account &rarr;</button>" +
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
        sb.append("    <p>Select your preferred seat on visual bus floorplan</p>");
        sb.append("  </a>");
        sb.append("  <a href=\"/tickets\" class=\"quick-card\">");
        sb.append("    <span class=\"quick-icon\">🎫</span>");
        sb.append("    <h4>Boarding Passes</h4>");
        sb.append("    <p>View, download, and print your confirmed tickets</p>");
        sb.append("  </a>");
        if (isAdmin) {
            sb.append("  <a href=\"/passengers\" class=\"quick-card\">");
            sb.append("    <span class=\"quick-icon\">👥</span>");
            sb.append("    <h4>Passenger Registry</h4>");
            sb.append("    <p>Manage customer contact data and demographic records</p>");
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
        sb.append("        <tr><th>ID</th><th>Passenger</th><th>Route</th><th>Seat #</th><th>Date</th><th>Fare</th><th>Status</th><th>Action</th></tr>");
        sb.append("      </thead><tbody>");
        if (recentBookings == null || recentBookings.isEmpty()) {
            sb.append("      <tr><td colspan=\"8\" class=\"text-center text-muted\">No bookings recorded yet.</td></tr>");
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
                sb.append("        <td><span class=\"badge badge-").append(confirmed ? "confirmed" : "cancelled").append("\">").append(WebUtils.escapeHtml(b.getStatus())).append("</span></td>");
                sb.append("        <td><a href=\"/tickets?id=").append(b.getBookingId()).append("\" class=\"btn btn-xs btn-primary\">Pass</a></td>");
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
            sb.append("        <label for=\"bus_name\">Bus Name / Model</label>");
            sb.append("        <input type=\"text\" id=\"bus_name\" name=\"bus_name\" value=\"").append(editBus != null ? WebUtils.escapeHtml(editBus.getBusName()) : "").append("\" placeholder=\"e.g. Express Sleeper 2026\" required />");
            sb.append("      </div>");
            sb.append("      <div class=\"form-row\">");
            sb.append("        <div class=\"form-group\">");
            sb.append("          <label for=\"source\">Origin City</label>");
            sb.append("          <input type=\"text\" id=\"source\" name=\"source\" value=\"").append(editBus != null ? WebUtils.escapeHtml(editBus.getSource()) : "").append("\" placeholder=\"e.g. New York\" required />");
            sb.append("        </div>");
            sb.append("        <div class=\"form-group\">");
            sb.append("          <label for=\"destination\">Destination City</label>");
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
            sb.append("          <input type=\"number\" id=\"fare\" name=\"fare\" step=\"0.01\" value=\"").append(editBus != null ? editBus.getFare() : 45.0).append("\" min=\"0\" required />");
            sb.append("        </div>");
            sb.append("      </div>");
            sb.append("      <button type=\"submit\" class=\"btn btn-primary btn-block\">").append(editBus != null ? "Save Changes" : "Register Bus").append("</button>");
            sb.append("    </form>");
            sb.append("  </div>");
        }

        // Table Card
        sb.append("  <div class=\"card").append(isAdmin ? "" : " full-width").append("\">");
        sb.append("    <div class=\"card-header\">");
        sb.append("      <h3>Active Routes & Schedule (").append(buses.size()).append(")</h3>");
        sb.append("      <a href=\"/bookings\" class=\"btn btn-sm btn-primary\">Book a Seat &rarr;</a>");
        sb.append("    </div>");
        sb.append("    <div class=\"table-responsive\">");
        sb.append("      <table class=\"table\">");
        sb.append("        <thead>");
        sb.append("          <tr><th>ID</th><th>Bus Name</th><th>Origin &rarr; Destination</th><th>Seats</th><th>Fare</th>").append(isAdmin ? "<th>Admin Actions</th>" : "<th>Action</th>").append("</tr>");
        sb.append("        </thead><tbody>");
        if (buses.isEmpty()) {
            sb.append("        <tr><td colspan=\"6\" class=\"text-center text-muted\">No buses registered yet.</td></tr>");
        } else {
            for (Bus b : buses) {
                sb.append("        <tr>");
                sb.append("          <td><strong>#").append(b.getBusId()).append("</strong></td>");
                sb.append("          <td><strong>").append(WebUtils.escapeHtml(b.getBusName())).append("</strong></td>");
                sb.append("          <td><span class=\"route-badge\">").append(WebUtils.escapeHtml(b.getSource())).append(" &rarr; ").append(WebUtils.escapeHtml(b.getDestination())).append("</span></td>");
                sb.append("          <td>").append(b.getTotalSeats()).append(" seats</td>");
                sb.append("          <td><strong class=\"text-success\">$").append(String.format("%.2f", b.getFare())).append("</strong></td>");
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
                    sb.append("          <td><a href=\"/bookings?bus_id=").append(b.getBusId()).append("\" class=\"btn btn-xs btn-primary\">Select</a></td>");
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
    // PASSENGERS VIEW (Admin only)
    // -------------------------------------------------------------------------

    public static String renderPassengers(SessionManager.Session session, List<Passenger> passengers, Passenger editPassenger, String csrfToken, String msg, String err) {
        StringBuilder sb = new StringBuilder();

        sb.append("<div class=\"page-header\">");
        sb.append("  <div>");
        sb.append("    <h1>Passenger Directory</h1>");
        sb.append("    <p class=\"subtitle\">Manage customer traveler records, identity, and contact information</p>");
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

        return htmlDoc("Passengers", "passengers", session, sb.toString(), msg, err);
    }

    // -------------------------------------------------------------------------
    // BOOKINGS VIEW WITH STEP PROGRESS & INTERACTIVE SEAT GRID
    // -------------------------------------------------------------------------

    public static String renderBookings(SessionManager.Session session, List<Booking> bookings, List<Bus> buses, List<Passenger> passengers, List<Integer> bookedSeats, Integer selectedBusId, String csrfToken, String msg, String err) {
        StringBuilder sb = new StringBuilder();
        boolean isAdmin = session != null && session.isAdmin();

        sb.append("<div class=\"page-header\">");
        sb.append("  <div>");
        sb.append("    <h1>Seat Reservations</h1>");
        sb.append("    <p class=\"subtitle\">Select your seat on the interactive floorplan, fill traveler details, and reserve instantly</p>");
        sb.append("  </div>");
        sb.append("</div>");

        // Step Progress Indicator (NEW FEATURE 2)
        sb.append("<div class=\"booking-stepper-container\">");
        sb.append("  <div class=\"step-item completed\">");
        sb.append("    <div class=\"step-badge\">1</div>");
        sb.append("    <div class=\"step-title\">Select Route</div>");
        sb.append("  </div>");
        sb.append("  <div class=\"step-line\"></div>");
        sb.append("  <div class=\"step-item active\">");
        sb.append("    <div class=\"step-badge\">2</div>");
        sb.append("    <div class=\"step-title\">Pick Seat</div>");
        sb.append("  </div>");
        sb.append("  <div class=\"step-line\"></div>");
        sb.append("  <div class=\"step-item\">");
        sb.append("    <div class=\"step-badge\">3</div>");
        sb.append("    <div class=\"step-title\">Passenger Details</div>");
        sb.append("  </div>");
        sb.append("  <div class=\"step-line\"></div>");
        sb.append("  <div class=\"step-item\">");
        sb.append("    <div class=\"step-badge\">4</div>");
        sb.append("    <div class=\"step-title\">Confirmation</div>");
        sb.append("  </div>");
        sb.append("</div>");

        sb.append("<div class=\"grid-booking-layout\">");

        // Left Panel: Route & Seat Selector Grid
        sb.append("  <div class=\"card booking-grid-card\">");
        sb.append("    <div class=\"card-header\">");
        sb.append("      <h3>💺 Interactive Seat Selection</h3>");
        sb.append("    </div>");

        // Bus Route Dropdown
        sb.append("    <div class=\"form-group\" style=\"margin-bottom: 1.25rem;\">");
        sb.append("      <label for=\"route_selector\"><strong>Step 1: Choose Bus & Route</strong></label>");
        sb.append("      <select id=\"route_selector\" class=\"form-control\" onchange=\"location.href='/bookings?bus_id=' + this.value;\">");
        sb.append("        <option value=\"\">-- Select Bus Route to View Seating Grid --</option>");
        Bus currentBus = null;
        for (Bus b : buses) {
            boolean sel = selectedBusId != null && selectedBusId == b.getBusId();
            if (sel) currentBus = b;
            sb.append("        <option value=\"").append(b.getBusId()).append("\" ").append(sel ? "selected" : "").append(">");
            sb.append(b.getBusId()).append(" - ").append(WebUtils.escapeHtml(b.getBusName())).append(" (").append(WebUtils.escapeHtml(b.getSource())).append(" → ").append(WebUtils.escapeHtml(b.getDestination())).append(") &bull; $").append(String.format("%.2f", b.getFare()));
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
            sb.append("      <div class=\"legend-item\"><span class=\"seat-preview occupied\"></span> Occupied</div>");
            sb.append("    </div>");

            sb.append("    <div class=\"bus-floorplan\">");
            sb.append("      <div class=\"bus-front\"><span class=\"driver-icon\">💺 Driver</span> &nbsp;|&nbsp; <span>Front Door 🚪</span></div>");
            sb.append("      <div class=\"seat-matrix\">");

            for (int s = 1; s <= totalSeats; s++) {
                boolean isOccupied = bookedSeats != null && bookedSeats.contains(s);
                sb.append("        <button type=\"button\" ");
                sb.append("          class=\"seat-btn ").append(isOccupied ? "occupied" : "available").append("\" ");
                sb.append("          id=\"seat-btn-").append(s).append("\" ");
                sb.append("          ").append(isOccupied ? "disabled" : "onclick=\"selectSeat(" + s + ")\"").append(">");
                sb.append("          <span class=\"seat-num\">").append(s).append("</span>");
                sb.append("        </button>");
            }

            sb.append("      </div>");
            sb.append("      <div class=\"bus-back\">Rear</div>");
            sb.append("    </div>");
        } else {
            sb.append("    <div class=\"empty-seat-state\">");
            sb.append("      <p>👈 Please select a bus from the dropdown above to load the visual seat layout.</p>");
            sb.append("    </div>");
        }
        sb.append("  </div>");

        // Right Panel: Reservation Form
        sb.append("  <div class=\"card form-card\">");
        sb.append("    <div class=\"card-header\">");
        sb.append("      <h3>🎟️ Reservation Details</h3>");
        sb.append("    </div>");
        sb.append("    <form action=\"/bookings/add\" method=\"POST\" id=\"bookingForm\" onsubmit=\"return handleBookingSubmit(event);\">");
        sb.append("      <input type=\"hidden\" name=\"csrf_token\" value=\"").append(WebUtils.escapeHtml(csrfToken)).append("\" />");

        // Selected Bus ID hidden or select
        sb.append("      <div class=\"form-group\">");
        sb.append("        <label for=\"bus_id\">Bus Selection</label>");
        sb.append("        <select id=\"bus_id\" name=\"bus_id\" required onchange=\"location.href='/bookings?bus_id=' + this.value;\">");
        sb.append("          <option value=\"\">-- Select Bus --</option>");
        for (Bus b : buses) {
            boolean sel = selectedBusId != null && selectedBusId == b.getBusId();
            sb.append("          <option value=\"").append(b.getBusId()).append("\" ").append(sel ? "selected" : "").append(">");
            sb.append(b.getBusId()).append(" - ").append(WebUtils.escapeHtml(b.getBusName())).append(" ($").append(String.format("%.2f", b.getFare())).append(")");
            sb.append("</option>");
        }
        sb.append("        </select>");
        sb.append("      </div>");

        // Passenger selector or input
        sb.append("      <div class=\"form-group\">");
        sb.append("        <label for=\"passenger_id\">Passenger Identity</label>");
        if (!passengers.isEmpty()) {
            sb.append("        <select id=\"passenger_id\" name=\"passenger_id\" required>");
            sb.append("          <option value=\"\">-- Choose Passenger --</option>");
            for (Passenger p : passengers) {
                sb.append("          <option value=\"").append(p.getPassengerId()).append("\">#").append(p.getPassengerId()).append(" - ").append(WebUtils.escapeHtml(p.getName())).append(" (").append(WebUtils.escapeHtml(p.getPhoneNumber())).append(")</option>");
            }
            sb.append("        </select>");
        } else {
            sb.append("        <input type=\"number\" id=\"passenger_id\" name=\"passenger_id\" placeholder=\"Enter Passenger ID (e.g. 101)\" required />");
        }
        sb.append("      </div>");

        // Seat Number & Date
        sb.append("      <div class=\"form-row\">");
        sb.append("        <div class=\"form-group\">");
        sb.append("          <label for=\"seat_number\">Selected Seat Number</label>");
        sb.append("          <input type=\"number\" id=\"seat_number\" name=\"seat_number\" min=\"1\" max=\"100\" placeholder=\"Click a seat on map\" required />");
        sb.append("        </div>");
        sb.append("        <div class=\"form-group\">");
        sb.append("          <label for=\"booking_date\">Travel Date</label>");
        sb.append("          <input type=\"date\" id=\"booking_date\" name=\"booking_date\" value=\"").append(LocalDate.now().toString()).append("\" required />");
        sb.append("        </div>");
        sb.append("      </div>");

        // Submit Button with animated spinner on submission (NEW FEATURE 2)
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
        sb.append("        <tr><th>ID</th><th>Passenger</th><th>Route</th><th>Seat #</th><th>Date</th><th>Fare</th><th>Status</th><th>Actions</th></tr>");
        sb.append("      </thead><tbody>");
        if (bookings.isEmpty()) {
            sb.append("      <tr><td colspan=\"8\" class=\"text-center text-muted\">No seat reservations found.</td></tr>");
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

        // Client-side JavaScript for Seat Grid selection and Form submission animation
        sb.append("<script>");
        sb.append("  var currentSelectedSeat = null;");
        sb.append("  function selectSeat(seatNum) {");
        sb.append("    if (currentSelectedSeat) {");
        sb.append("      var prev = document.getElementById('seat-btn-' + currentSelectedSeat);");
        sb.append("      if (prev) prev.classList.remove('selected');");
        sb.append("    }");
        sb.append("    currentSelectedSeat = seatNum;");
        sb.append("    var btn = document.getElementById('seat-btn-' + seatNum);");
        sb.append("    if (btn) btn.classList.add('selected');");
        sb.append("    var input = document.getElementById('seat_number');");
        sb.append("    if (input) input.value = seatNum;");
        sb.append("  }");
        sb.append("  function handleBookingSubmit(e) {");
        sb.append("    var sNum = document.getElementById('seat_number').value;");
        sb.append("    if (!sNum) {");
        sb.append("      alert('Please select a seat from the visual map first.');");
        sb.append("      return false;");
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
    // TICKETS & BOARDING PASS VIEW (With Animated SVG Checkmark)
    // -------------------------------------------------------------------------

    public static String renderTickets(SessionManager.Session session, Booking ticket, List<Booking> userBookings, String msg, String err) {
        StringBuilder sb = new StringBuilder();

        sb.append("<div class=\"page-header no-print\">");
        sb.append("  <div>");
        sb.append("    <h1>Boarding Pass & Confirmation</h1>");
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

            // Success Confirmation Banner with Animated Checkmark (NEW FEATURE 2)
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
            sb.append("      <div class=\"route-arrow\">✈ ➔ 🚍</div>");
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
            sb.append("    <a href=\"/bookings\" class=\"btn btn-outline\">&larr; Back to Reservations</a>");
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
                ".nav-links { display: flex; gap: 0.5rem; align-items: center; }" +
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
                ".card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.25rem; padding-bottom: 0.75rem; border-bottom: 1px solid var(--border-color); }" +
                ".table-responsive { overflow-x: auto; }" +
                ".table { width: 100%; border-collapse: collapse; font-size: 0.92rem; text-align: left; }" +
                ".table th { color: var(--text-muted); font-weight: 600; padding: 0.75rem 1rem; border-bottom: 1px solid var(--border-color); text-transform: uppercase; font-size: 0.75rem; letter-spacing: 0.05em; }" +
                ".table td { padding: 0.85rem 1rem; border-bottom: 1px solid rgba(255,255,255,0.04); vertical-align: middle; }" +
                ".table tr:hover td { background: rgba(255,255,255,0.02); }" +
                ".actions-cell { display: flex; gap: 0.4rem; align-items: center; }" +
                "" +
                "/* Layout */" +
                ".grid-form-table { display: grid; grid-template-columns: 340px 1fr; gap: 1.5rem; }" +
                ".grid-booking-layout { display: grid; grid-template-columns: 1fr 380px; gap: 1.5rem; }" +
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
                ".route-badge { background: rgba(255,255,255,0.05); padding: 0.25rem 0.6rem; border-radius: 6px; font-weight: 600; font-size: 0.85rem; }" +
                "" +
                "/* ===========================================================" +
                "   NEW FEATURE 1 & 2: ANIMATIONS & STEPPER (GPU-Friendly Only)" +
                "   =========================================================== */" +
                "" +
                "/* Stepper Bar */" +
                ".booking-stepper-container { display: flex; align-items: center; justify-content: space-between; background: var(--bg-card); border: 1px solid var(--border-color); border-radius: var(--radius); padding: 1.25rem 2rem; margin-bottom: 1.5rem; }" +
                ".step-item { display: flex; align-items: center; gap: 0.65rem; opacity: 0.45; transition: opacity 0.3s ease; }" +
                ".step-item.active { opacity: 1; }" +
                ".step-item.completed { opacity: 0.85; }" +
                ".step-badge { width: 32px; height: 32px; border-radius: 50%; background: #24324a; color: #fff; display: flex; align-items: center; justify-content: center; font-weight: 700; font-size: 0.85rem; transition: transform 0.2s ease; }" +
                ".step-item.active .step-badge { background: var(--primary); transform: scale(1.1); box-shadow: 0 0 12px rgba(99, 102, 241, 0.5); }" +
                ".step-item.completed .step-badge { background: var(--success); }" +
                ".step-title { font-size: 0.85rem; font-weight: 600; }" +
                ".step-line { flex: 1; height: 2px; background: var(--border-color); margin: 0 1rem; }" +
                "" +
                "/* Visual Bus Floorplan & Seat Matrix */" +
                ".bus-floorplan { background: #0c1322; border: 2px dashed var(--border-color); border-radius: 16px; padding: 1.25rem; max-width: 480px; margin: 1rem auto; }" +
                ".bus-front { text-align: center; padding: 0.5rem; background: rgba(255,255,255,0.04); border-radius: 8px; font-size: 0.8rem; font-weight: 700; color: var(--text-muted); margin-bottom: 1rem; border-bottom: 1px solid var(--border-color); }" +
                ".bus-back { text-align: center; padding: 0.4rem; color: var(--text-muted); font-size: 0.75rem; margin-top: 1rem; }" +
                ".seat-matrix { display: grid; grid-template-columns: repeat(4, 1fr); gap: 0.75rem; justify-items: center; }" +
                ".seat-matrix > button:nth-child(4n-2) { margin-right: 1.25rem; } /* Center Aisle */" +
                ".seat-btn {" +
                "  width: 52px; height: 48px; border-radius: 10px; border: 2px solid #2d3e58; background: #162238; color: #fff; font-weight: 700; font-size: 0.9rem; cursor: pointer;" +
                "  display: flex; flex-direction: column; align-items: center; justify-content: center; position: relative;" +
                "  /* GPU accelerated transitions only */" +
                "  transition: transform 0.2s ease, opacity 0.2s ease;" +
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
                "  background: #111827;" +
                "  border-color: #1f2937;" +
                "  color: #4b5563;" +
                "  opacity: 0.45;" +
                "  cursor: not-allowed;" +
                "}" +
                ".seat-legend { display: flex; gap: 1.25rem; justify-content: center; margin-bottom: 1rem; font-size: 0.85rem; }" +
                ".legend-item { display: flex; align-items: center; gap: 0.4rem; }" +
                ".seat-preview { width: 16px; height: 16px; border-radius: 4px; display: inline-block; }" +
                ".seat-preview.available { background: #162238; border: 1px solid #2d3e58; }" +
                ".seat-preview.selected { background: var(--primary); }" +
                ".seat-preview.occupied { background: #111827; border: 1px solid #1f2937; opacity: 0.45; }" +
                "" +
                "/* Submit Button Loading Spinner */" +
                ".spinner-svg { width: 18px; height: 18px; animation: spin 0.8s linear infinite; vertical-align: middle; margin-right: 6px; }" +
                "@keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }" +
                "" +
                "/* Error Shake Animation (GPU transform only) */" +
                ".shake-anim {" +
                "  animation: shake 0.4s ease-in-out;" +
                "  will-change: transform;" +
                "}" +
                "@keyframes shake {" +
                "  0%, 100% { transform: translateX(0); }" +
                "  20%, 60% { transform: translateX(-7px); }" +
                "  40%, 80% { transform: translateX(7px); }" +
                "}" +
                "" +
                "/* Animated Success Checkmark (SVG Stroke) */" +
                ".confirmation-card { background: rgba(16, 185, 129, 0.1); border: 1px solid rgba(16, 185, 129, 0.3); border-radius: var(--radius); padding: 1.5rem; display: flex; align-items: center; gap: 1.25rem; margin-bottom: 1.5rem; }" +
                ".checkmark-wrapper { width: 54px; height: 54px; flex-shrink: 0; }" +
                ".checkmark-svg { width: 54px; height: 54px; stroke: #10b981; stroke-width: 3; stroke-miterlimit: 10; }" +
                ".checkmark-circle {" +
                "  stroke-dasharray: 166;" +
                "  stroke-dashoffset: 166;" +
                "  animation: stroke 0.6s cubic-bezier(0.65, 0, 0.45, 1) forwards;" +
                "}" +
                ".checkmark-check {" +
                "  transform-origin: 50% 50%;" +
                "  stroke-dasharray: 48;" +
                "  stroke-dashoffset: 48;" +
                "  animation: stroke 0.4s cubic-bezier(0.65, 0, 0.45, 1) 0.5s forwards;" +
                "}" +
                "@keyframes stroke {" +
                "  100% { stroke-dashoffset: 0; }" +
                "}" +
                "" +
                "/* Accessibility: prefers-reduced-motion */" +
                "@media (prefers-reduced-motion: reduce) {" +
                "  *, *::before, *::after {" +
                "    animation-duration: 0.01ms !important;" +
                "    animation-iteration-count: 1 !important;" +
                "    transition-duration: 0.01ms !important;" +
                "    scroll-behavior: auto !important;" +
                "  }" +
                "}" +
                "" +
                "/* Auth Pages */" +
                ".login-body { display: flex; align-items: center; justify-content: center; min-height: 100vh; background: radial-gradient(circle at top, #1e1b4b 0%, var(--bg-primary) 70%); padding: 1.5rem; }" +
                ".login-card { background: var(--bg-card); border: 1px solid var(--border-color); border-radius: 16px; width: 100%; max-width: 440px; padding: 2.25rem; box-shadow: 0 20px 40px rgba(0,0,0,0.5); }" +
                ".login-header { text-align: center; margin-bottom: 1.75rem; }" +
                ".login-logo { font-size: 2.8rem; margin-bottom: 0.4rem; }" +
                ".login-header h1 { font-size: 1.6rem; font-weight: 800; }" +
                ".login-header p { color: var(--text-muted); font-size: 0.88rem; }" +
                ".auth-alt-link { text-align: center; margin-top: 1.25rem; font-size: 0.88rem; color: var(--text-muted); }" +
                ".login-footer { margin-top: 1.5rem; text-align: center; border-top: 1px solid var(--border-color); padding-top: 1rem; color: var(--text-muted); font-size: 0.8rem; }" +
                "" +
                "/* Password Strength Meter */" +
                ".strength-container { margin-top: 0.6rem; padding: 0.65rem 0.85rem; background: rgba(0,0,0,0.25); border-radius: 8px; border: 1px solid var(--border-color); }" +
                ".strength-header { display: flex; justify-content: space-between; align-items: center; font-size: 0.8rem; margin-bottom: 0.35rem; }" +
                ".strength-label { color: var(--text-muted); font-weight: 600; }" +
                ".strength-badge { font-weight: 700; font-size: 0.75rem; padding: 0.1rem 0.5rem; border-radius: 4px; text-transform: uppercase; letter-spacing: 0.04em; }" +
                ".strength-badge.weak { background: rgba(239, 68, 68, 0.2); color: #f87171; border: 1px solid rgba(239, 68, 68, 0.4); }" +
                ".strength-badge.better { background: rgba(245, 158, 11, 0.2); color: #fbbf24; border: 1px solid rgba(245, 158, 11, 0.4); }" +
                ".strength-badge.best { background: rgba(16, 185, 129, 0.2); color: #34d399; border: 1px solid rgba(16, 185, 129, 0.4); }" +
                ".strength-meter-bar { width: 100%; height: 6px; background: rgba(255,255,255,0.08); border-radius: 999px; overflow: hidden; margin-bottom: 0.5rem; }" +
                ".strength-fill { height: 100%; border-radius: 999px; transition: width 0.25s ease, background-color 0.25s ease; will-change: width, background-color; }" +
                ".strength-fill.weak { width: 33%; background-color: #ef4444; }" +
                ".strength-fill.better { width: 66%; background-color: #f59e0b; }" +
                ".strength-fill.best { width: 100%; background-color: #10b981; }" +
                ".strength-rules { list-style: none; padding: 0; margin: 0; font-size: 0.75rem; }" +
                ".rule-item { margin-top: 0.2rem; transition: color 0.2s ease; display: flex; align-items: center; gap: 0.35rem; }" +
                ".rule-item.invalid { color: var(--text-muted); opacity: 0.7; }" +
                ".rule-item.valid { color: #34d399; font-weight: 600; }" +
                ".rule-icon { font-weight: 700; font-size: 0.8rem; }" +
                "" +
                "/* Boarding Pass */" +
                ".boarding-pass { max-width: 720px; margin: 0 auto 2rem; background: #fff; color: #111827; border-radius: 16px; overflow: hidden; box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.6); }" +
                ".pass-header { background: #0f172a; color: #fff; padding: 1.5rem 2rem; display: flex; justify-content: space-between; align-items: center; }" +
                ".pass-brand { display: flex; align-items: center; gap: 1rem; }" +
                ".pass-icon { font-size: 2.2rem; }" +
                ".pass-body { padding: 2rem; }" +
                ".pass-route-banner { display: flex; align-items: center; justify-content: space-between; background: #f1f5f9; padding: 1.25rem 2rem; border-radius: 12px; margin-bottom: 1.75rem; }" +
                ".station-label { font-size: 0.75rem; color: #64748b; font-weight: 700; display: block; }" +
                ".station-name { font-size: 1.4rem; font-weight: 800; color: #0f172a; }" +
                ".route-arrow { font-size: 1.5rem; color: #6366f1; }" +
                ".pass-details-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 1.25rem; margin-bottom: 1.75rem; }" +
                ".field-label { font-size: 0.72rem; color: #64748b; font-weight: 700; display: block; text-transform: uppercase; margin-bottom: 0.2rem; }" +
                ".field-value { font-size: 1.05rem; font-weight: 700; color: #1e293b; }" +
                ".field-value.highlight { color: #4338ca; font-size: 1.2rem; }" +
                ".pass-barcode-area { text-align: center; border-top: 2px dashed #cbd5e1; padding-top: 1.25rem; }" +
                ".barcode-lines { font-family: monospace; font-size: 1.4rem; letter-spacing: 4px; color: #334155; }" +
                ".barcode-num { font-size: 0.75rem; color: #94a3b8; font-weight: 600; margin-top: 0.25rem; }" +
                ".pass-actions { padding: 1.25rem 2rem; background: #f8fafc; border-top: 1px solid #e2e8f0; display: flex; gap: 0.75rem; justify-content: flex-end; }" +
                "@media print {" +
                "  .no-print, .navbar, .footer { display: none !important; }" +
                "  body { background: #fff !important; color: #000 !important; }" +
                "  .boarding-pass { box-shadow: none !important; margin: 0 !important; max-width: 100% !important; }" +
                "}" +
                "" +
                "/* Auth Box & Register Callouts */" +
                ".auth-alt-box { margin-top: 1.25rem; padding: 1rem; background: rgba(255,255,255,0.03); border: 1px dashed var(--border-color); border-radius: 10px; text-align: center; }" +
                ".auth-alt-text { font-size: 0.88rem; color: var(--text-muted); display: block; margin-bottom: 0.35rem; }" +
                ".btn-outline-primary { background: transparent; border: 1px solid var(--primary); color: #818cf8; }" +
                ".btn-outline-primary:hover { background: rgba(99, 102, 241, 0.15); color: #fff; border-color: var(--primary-hover); }" +
                ".login-copyright { margin-top: 0.85rem; font-size: 0.75rem; color: #64748b; }" +
                ".pass-copyright { margin-top: 0.5rem; font-size: 0.72rem; color: #94a3b8; font-style: italic; }" +
                "" +
                "/* Guest Registration Banner */" +
                ".guest-banner { display: flex; justify-content: space-between; align-items: center; background: linear-gradient(135deg, rgba(99, 102, 241, 0.12) 0%, rgba(59, 130, 246, 0.08) 100%); border: 1px solid rgba(99, 102, 241, 0.35); border-radius: var(--radius); padding: 1.5rem 1.75rem; margin-bottom: 2rem; gap: 1.5rem; flex-wrap: wrap; }" +
                ".guest-banner-info h3 { font-size: 1.15rem; font-weight: 700; color: #fff; margin-bottom: 0.35rem; }" +
                ".guest-banner-info p { color: var(--text-muted); font-size: 0.9rem; }" +
                ".guest-banner-actions { display: flex; gap: 0.75rem; align-items: center; }" +
                "" +
                "/* Footer with Copyright */" +
                ".footer { border-top: 1px solid var(--border-color); padding: 2rem 1.5rem; color: var(--text-muted); font-size: 0.85rem; margin-top: auto; background: rgba(10, 15, 29, 0.95); }" +
                ".footer-container { max-width: 1280px; margin: 0 auto; display: flex; flex-direction: column; align-items: center; gap: 0.75rem; text-align: center; }" +
                ".footer-brand { display: flex; align-items: center; gap: 0.5rem; color: var(--text-main); font-size: 0.95rem; }" +
                ".footer-nav { display: flex; gap: 0.6rem; flex-wrap: wrap; justify-content: center; font-size: 0.85rem; }" +
                ".footer-nav a { color: var(--text-muted); text-decoration: none; }" +
                ".footer-nav a:hover { color: var(--primary); }" +
                ".footer-copy { color: #64748b; font-size: 0.8rem; margin-top: 0.25rem; }" +
                "";
    }
}
