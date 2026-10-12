package web;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class WebUtils {

    public static Map<String, String> parseQueryParams(String query) {
        if (query == null || query.trim().isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, String> map = new HashMap<>();
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] kv = pair.split("=", 2);
            String key = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
            String val = kv.length > 1 ? URLDecoder.decode(kv[1], StandardCharsets.UTF_8) : "";
            map.put(key, val);
        }
        return map;
    }

    public static Map<String, String> parseFormData(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody()) {
            String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            return parseQueryParams(body);
        }
    }

    public static void sendHtml(HttpExchange exchange, int statusCode, String html) throws IOException {
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.getResponseHeaders().set("Cache-Control", "no-cache, no-store, must-revalidate");
        exchange.getResponseHeaders().set("X-Content-Type-Options", "nosniff");
        exchange.getResponseHeaders().set("X-Frame-Options", "DENY");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    public static void sendCss(HttpExchange exchange, String css) throws IOException {
        byte[] bytes = css.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/css; charset=UTF-8");
        // High caching for static CSS to minimize HDD reads on low-spec server
        exchange.getResponseHeaders().set("Cache-Control", "public, max-age=86400");
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    public static void sendPlainText(HttpExchange exchange, String text, String downloadFilename) throws IOException {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
        if (downloadFilename != null) {
            exchange.getResponseHeaders().set("Content-Disposition", "attachment; filename=\"" + downloadFilename + "\"");
        }
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    public static void redirect(HttpExchange exchange, String location) throws IOException {
        exchange.getResponseHeaders().set("Location", location);
        exchange.sendResponseHeaders(302, -1);
        exchange.close();
    }

    public static String getSessionToken(HttpExchange exchange) {
        String cookieHeader = exchange.getRequestHeaders().getFirst("Cookie");
        if (cookieHeader != null) {
            String[] cookies = cookieHeader.split(";");
            for (String cookie : cookies) {
                String[] parts = cookie.trim().split("=", 2);
                if (parts.length == 2 && SessionManager.SESSION_COOKIE_NAME.equals(parts[0])) {
                    return parts[1];
                }
            }
        }
        return null;
    }

    public static void setSessionCookie(HttpExchange exchange, String sessionId) {
        String cookieVal = SessionManager.SESSION_COOKIE_NAME + "=" + sessionId + "; Path=/; HttpOnly; SameSite=Strict";
        exchange.getResponseHeaders().add("Set-Cookie", cookieVal);
    }

    public static void clearSessionCookie(HttpExchange exchange) {
        String cookieVal = SessionManager.SESSION_COOKIE_NAME + "=; Path=/; Max-Age=0; HttpOnly; SameSite=Strict";
        exchange.getResponseHeaders().add("Set-Cookie", cookieVal);
    }

    public static String getClientIp(HttpExchange exchange) {
        String xForwarded = exchange.getRequestHeaders().getFirst("X-Forwarded-For");
        if (xForwarded != null && !xForwarded.trim().isEmpty()) {
            return xForwarded.split(",")[0].trim();
        }
        String xReal = exchange.getRequestHeaders().getFirst("X-Real-IP");
        if (xReal != null && !xReal.trim().isEmpty()) {
            return xReal.trim();
        }
        InetSocketAddress remote = exchange.getRemoteAddress();
        return remote != null ? remote.getAddress().getHostAddress() : "127.0.0.1";
    }

    public static boolean validateCsrfToken(Map<String, String> form, SessionManager.Session session) {
        if (session == null || session.getCsrfToken() == null) return false;
        String token = form.get("csrf_token");
        return token != null && token.equals(session.getCsrfToken());
    }

    public static String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }
}
