# Bus Management & Reservation System (SwiftBus)

A complete, high-performance, 100% pure Java Web Application integrated with MySQL JDBC for bus fleet management, route scheduling, passenger seat reservations, visual seat selection, and official boarding pass printing.

Engineered with zero external web frameworks (no Spring, no Servlets, no external CSS/JS libraries) and specifically tuned for low-resource server hardware (Intel Pentium 1-2 Cores, 4 GB RAM, 500 GB HDD).

---

## 🛠️ Architecture & Tech Stack

- **Language:** Java (JDK 21+) — *100% Pure Java, zero external dependencies!*
- **Web Server:** Standard Java Embedded HTTP Server (`com.sun.net.httpserver.HttpServer`)
- **Worker Concurrency:** Fixed thread pool of 4 workers (`Executors.newFixedThreadPool(4)`) optimized for 1-2 CPU cores
- **Port:** `6969`
- **Database:** MySQL Server 8.0+ exclusively (Port `3307`, schema: `bus_reservation`) via official Oracle JDBC `mysql-connector-j-26.7.0.jar`
- **Database Connection Pooling:** Custom hand-written thread-safe connection pool strictly capping active sockets to 5 with dynamic proxy socket recycling
- **Architecture Pattern:** Strict MVC / DAO Separation
  - **`main`**: Application launcher ([`Main.java`](file:///e:/d%20drive/antigravity%20projects/Bus-Management-System/src/main/Main.java))
  - **`web`**: HTTP routes ([`WebServer.java`](file:///e:/d%20drive/antigravity%20projects/Bus-Management-System/src/web/WebServer.java)), views ([`WebViews.java`](file:///e:/d%20drive/antigravity%20projects/Bus-Management-System/src/web/WebViews.java)), session manager ([`SessionManager.java`](file:///e:/d%20drive/antigravity%20projects/Bus-Management-System/src/web/SessionManager.java)), security utilities ([`SecurityUtils.java`](file:///e:/d%20drive/antigravity%20projects/Bus-Management-System/src/web/SecurityUtils.java)), and helpers ([`WebUtils.java`](file:///e:/d%20drive/antigravity%20projects/Bus-Management-System/src/web/WebUtils.java))
  - **`database`**: Custom 5-connection pool ([`DBConnection.java`](file:///e:/d%20drive/antigravity%20projects/Bus-Management-System/src/database/DBConnection.java))
  - **`model`**: Domain entities ([`User.java`](file:///e:/d%20drive/antigravity%20projects/Bus-Management-System/src/model/User.java), [`Bus.java`](file:///e:/d%20drive/antigravity%20projects/Bus-Management-System/src/model/Bus.java), [`Passenger.java`](file:///e:/d%20drive/antigravity%20projects/Bus-Management-System/src/model/Passenger.java), [`Booking.java`](file:///e:/d%20drive/antigravity%20projects/Bus-Management-System/src/model/Booking.java))
  - **`dao`**: MySQL CRUD ([`UserDAO.java`](file:///e:/d%20drive/antigravity%20projects/Bus-Management-System/src/dao/UserDAO.java), [`BusDAO.java`](file:///e:/d%20drive/antigravity%20projects/Bus-Management-System/src/dao/BusDAO.java), [`PassengerDAO.java`](file:///e:/d%20drive/antigravity%20projects/Bus-Management-System/src/dao/PassengerDAO.java), [`BookingDAO.java`](file:///e:/d%20drive/antigravity%20projects/Bus-Management-System/src/dao/BookingDAO.java))
  - **`test`**: Automated test suite ([`TestSuite.java`](file:///e:/d%20drive/antigravity%20projects/Bus-Management-System/src/test/TestSuite.java))

---

## 🔐 Security, User Accounts & Rate Limiting

- **Authentication Endpoints:**
  - Self-service registration (`/register`) with alphanumeric username (3-30 chars), valid email format, and password validation.
  - Multi-identifier login (`/login`) via username or email with non-leaky error responses (*"Invalid credentials"*).
- **Password Policy & Strength Meter:**
  - **Mandatory Policy:** Minimum **8 characters** AND at least one **special character** (`!@#$%^&*()_+-=[]{};':"|,.<>/?~`).
  - **Live Interactive Strength Meter:** Evaluates password in real-time as the user types:
    - 🔴 **Weak:** Shorter than 8 chars or missing special character (submission blocked with shake animation).
    - 🟡 **Better:** Meets required 8+ chars, special character, and letters/numbers.
    - 🟢 **Best:** 10+ chars with mixed uppercase, lowercase, numbers, and multiple symbols.
- **Pure JDK Cryptography:**
  - Password hashing via `PBKDF2WithHmacSHA256` (`javax.crypto.SecretKeyFactory`).
  - Per-user random 16-byte salt generated via `java.security.SecureRandom`.
  - Constant-time hash verification via `MessageDigest.isEqual` to prevent timing attacks.
  - Configurable iteration count (`65,000`, overridable via `PBKDF2_ITERATIONS` environment variable for slow CPUs).
- **Session Security:**
  - In-memory `ConcurrentHashMap<String, Session>` with 128-bit cryptographically secure random session tokens.
  - Cookies configured with `HttpOnly; SameSite=Strict; Path=/`.
  - 30-minute idle session timeout with lazy expiration cleanup (zero background thread overhead).
- **Multi-Tier Rate Limiting:**
  - **Login Rate Limiter:** Maximum 5 failed login attempts per 5 minutes per IP.
  - **Registration Rate Limiter:** Maximum 5 registration attempts per 10 minutes per IP to prevent bot spam.
- **CSRF & XSS Protection:**
  - Unique per-session 128-bit CSRF token validated on all state-changing POST forms.
  - HTML entity output encoding via [`WebUtils.escapeHtml`](file:///e:/d%20drive/antigravity%20projects/Bus-Management-System/src/web/WebUtils.java).
- **Access Control & Scoping:**
  - Public routes: `/` (dashboard overview) and `/buses` (public route and fare browsing).
  - Protected routes: `/bookings`, `/bookings/add`, `/bookings/cancel`, `/tickets` (redirect to `/login?next=<url>` if unauthenticated).
  - User scoping: Regular users can only inspect and cancel their own bookings; administrators (`ADMIN` role) manage bus routes, the passenger registry, and all bookings.

---

## 🎨 Booking Animations & UI (Vanilla CSS/JS)

- **Interactive Seat Selection Grid:**
  - Visual bus floorplan layout with driver cabin, center aisle, and clickable seat buttons.
  - Smooth hover and selection transitions using exclusively GPU-friendly properties (`transform` and `opacity`).
- **Booking Progress Stepper:**
  - Visual step indicator (*Select Route* → *Pick Seat* → *Passenger Details* → *Confirmation*).
- **Submit Loading State:**
  - Async button spinner on form submission preventing double-clicks.
- **Animated SVG Checkmark:**
  - Animated SVG stroke-dash checkmark (`checkmark-stroke` / `checkmark-circle`) rendered on the confirmed boarding pass page.
- **Accessibility:**
  - Full `@media (prefers-reduced-motion: reduce)` support disabling animations and transitions for users requesting reduced motion.
  - Native system font stack (`-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto...`), eliminating external font requests.

---

## ⚙️ Low-Spec Server Hardware Tuning

Tailored for **Intel Pentium (1-2 Cores), 4 GB System RAM, 500 GB HDD**:

1. **JVM Tuning:**
   `-Xms128m -Xmx384m -XX:+UseSerialGC -XX:TieredStopAtLevel=1 -Xss512k`
   - Keeps memory strictly bounded within 384MB.
   - Serial GC and C1-only compilation minimize background CPU utilization.
2. **HttpServer Threading:**
   - Fixed pool of 4 worker threads (`Executors.newFixedThreadPool(4)`).
3. **Hand-Written JDBC Connection Pool:**
   - Caps connections to 5 sockets (`MAX_POOL_SIZE = 5`) via [`DBConnection.java`](file:///e:/d%20drive/antigravity%20projects/Bus-Management-System/src/database/DBConnection.java).
   - Eliminates per-request TCP connection handshakes.
4. **MySQL 8.0 Tuning ([`docker/mysql/my.cnf`](file:///e:/d%20drive/antigravity%20projects/Bus-Management-System/docker/mysql/my.cnf)):**
   - `innodb_buffer_pool_size=512M`
   - `max_connections=30`
   - `innodb_flush_log_at_trx_commit=2` (minimizes random disk writes on 500GB HDD)
   - `performance_schema=OFF` (saves ~250MB RAM)
   - `skip-name-resolve`
5. **Docker Cgroup & Logging Limits:**
   - App: `mem_limit: 512m`
   - MySQL: `mem_limit: 1g`
   - Log rotation configured (`max-size: 10m`, `max-file: 3`) to prevent HDD bloat.
   - Base image: `eclipse-temurin:21-jre-alpine`

---

## 🧪 Automated Testing

The project includes an automated test suite ([`src/test/TestSuite.java`](file:///e:/d%20drive/antigravity%20projects/Bus-Management-System/src/test/TestSuite.java)) with 60 comprehensive unit and integration tests:

- **Password Policy:** Minimum length, special character detection, Weak/Better/Best classification.
- **PBKDF2 Cryptography:** Key generation, random salt uniqueness, constant-time verification.
- **Rate Limiting:** Login failure limits, registration flood protection, IP reset on success.
- **Session Management:** 128-bit token generation, guest sessions, idle expiration, invalidation.
- **Security:** CSRF token verification, XSS HTML escaping.
- **MySQL Integrity:** Official Oracle JDBC driver class loading and schema verification.

To run the automated tests on Debian Linux:
```bash
chmod +x test-debian.sh
./test-debian.sh
```

---

## 🔑 Authentication & Access Control

SwiftBus provides role-based access control with secure credential handling:
- **Travelers / Users:** Register accounts self-service via the `/register` web portal or the desktop GUI dialog.
- **Administrators:** Oversee bus fleets, schedules, passenger records, and all reservations.
- **Credential Storage:** All user credentials are stored with salted PBKDF2WithHmacSHA256 hashes (`users` table). Default seed accounts are initialized in encrypted form.

---

## 🚀 How to Run

### Option 1: Docker Compose (Debian or any OS)
Run the entire application along with the tuned MySQL 8.0 database in Docker:
```bash
# Start MySQL and SwiftBus (automated tests execute during build)
docker compose up --build
```
Once started, open your web browser and navigate to:
👉 **[http://localhost:6969](http://localhost:6969)**

To stop:
```bash
docker compose down
```

---

### Option 2: Run Natively on Debian Linux

1. **Install OpenJDK 21 & MySQL/MariaDB:**
   ```bash
   sudo apt update
   sudo apt install -y openjdk-21-jdk mariadb-server
   ```

2. **Initialize Database on Debian:**
   ```bash
   sudo mariadb < lib/database.sql
   ```

3. **Compile, Test & Run:**
   ```bash
   chmod +x build-debian.sh run-debian.sh test-debian.sh
   
   # 1. Compile Java sources
   ./build-debian.sh
   
   # 2. Run automated test suite
   ./test-debian.sh
   
   # 3. Launch Web Server
   ./run-debian.sh
   ```
   *Note: Environment variables such as `DB_HOST`, `DB_PORT`, `DB_USER`, and `DB_PASSWORD` can be customized before running `./run-debian.sh`.*

---

### Option 3: Database Migration for Existing Installations
If running against an existing database, apply the migration script:
```sql
source lib/migration_v2.sql;
```

---

## 📝 Self-Service User Registration

Users can create a personal traveler account across both the Web Portal and the Swing Desktop GUI:
- **Web Portal:** Navigate to [http://localhost:6969/register](http://localhost:6969/register) or click **Register** in the top navigation bar or **Create New Account** on the sign-in card.
  - Interactive password strength evaluator with real-time feedback (Weak / Better / Best).
  - Enforces minimum 8 characters and at least one special character (`!@#$%^&*` etc.).
  - Rate limited to 5 attempts per 10 minutes per IP to safeguard against bot registration floods.
- **Desktop GUI:** Launch [`gui.LoginFrame`](file:///e:/d%20drive/antigravity%20projects/Bus-Management-System/src/gui/LoginFrame.java) and click the **➕ Register** button to open the account registration dialog.

---

## 📄 License & Copyright

&copy; 2026 SwiftBus Inc. All Rights Reserved.  
Built with 100% Pure Java & MySQL native architecture. Designed for high reliability on low-spec server infrastructure.
