import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Shared session, CSRF, and flash-message behaviour for the web application.
 */
public final class WebSecurity {
    private static final String USERNAME_KEY = "username";
    private static final String CSRF_TOKEN_KEY = "csrfToken";
    private static final String FLASH_TYPE_KEY = "flash.type";
    private static final String FLASH_MESSAGE_KEY = "flash.message";
    private static final SecureRandom RANDOM = new SecureRandom();

    private WebSecurity() {
    }

    public static HttpSession requireAuthenticatedSession(HttpServletRequest req, HttpServletResponse res)
            throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute(USERNAME_KEY) == null) {
            res.sendRedirect("/welcome");
            return null;
        }
        ensureCsrfToken(session);
        return session;
    }

    public static void startAuthenticatedSession(HttpServletRequest req, String username) {
        HttpSession existing = req.getSession(false);
        if (existing != null) {
            existing.invalidate();
        }

        HttpSession newSession = req.getSession(true);
        newSession.setAttribute(USERNAME_KEY, username);
        newSession.setMaxInactiveInterval(AppConfig.SESSION_TIMEOUT_SECONDS);
        ensureCsrfToken(newSession);
    }

    public static void logout(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    public static String currentUsername(HttpSession session) {
        return session == null ? null : (String) session.getAttribute(USERNAME_KEY);
    }

    public static String ensureCsrfToken(HttpSession session) {
        String token = (String) session.getAttribute(CSRF_TOKEN_KEY);
        if (token == null || token.isEmpty()) {
            byte[] random = new byte[24];
            RANDOM.nextBytes(random);
            token = Base64.getUrlEncoder().withoutPadding().encodeToString(random);
            session.setAttribute(CSRF_TOKEN_KEY, token);
        }
        return token;
    }

    public static boolean validateCsrfToken(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) {
            return false;
        }

        String expected = (String) session.getAttribute(CSRF_TOKEN_KEY);
        String actual = req.getParameter("csrfToken");
        return expected != null && expected.equals(actual);
    }

    public static void applySecurityHeaders(HttpServletResponse res) {
        res.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        res.setHeader("Pragma", "no-cache");
        res.setHeader("X-Content-Type-Options", "nosniff");
        res.setHeader("X-Frame-Options", "DENY");
        res.setHeader("Referrer-Policy", "no-referrer");
        res.setHeader("Content-Security-Policy",
                "default-src 'self'; img-src 'self' data:; style-src 'self'; form-action 'self'; base-uri 'self'; frame-ancestors 'none'");
    }

    public static void setFlash(HttpSession session, String type, String message) {
        if (session == null) {
            return;
        }
        session.setAttribute(FLASH_TYPE_KEY, type);
        session.setAttribute(FLASH_MESSAGE_KEY, message);
    }

    public static FlashMessage consumeFlash(HttpSession session) {
        if (session == null) {
            return null;
        }

        String type = (String) session.getAttribute(FLASH_TYPE_KEY);
        String message = (String) session.getAttribute(FLASH_MESSAGE_KEY);
        session.removeAttribute(FLASH_TYPE_KEY);
        session.removeAttribute(FLASH_MESSAGE_KEY);

        if (message == null || message.trim().isEmpty()) {
            return null;
        }
        return new FlashMessage(type == null ? "info" : type, message);
    }

    public static final class FlashMessage {
        public final String type;
        public final String message;

        FlashMessage(String type, String message) {
            this.type = type;
            this.message = message;
        }
    }
}
