import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Ends the current authenticated session.
 */
public class LogoutServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        HttpSession session = req.getSession(false);
        if (session != null && !WebSecurity.validateCsrfToken(req)) {
            WebSecurity.setFlash(session, "error", "Your session token was invalid. Please try again.");
            res.sendRedirect("/account");
            return;
        }

        WebSecurity.logout(req);
        res.sendRedirect("/welcome");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.sendRedirect("/welcome");
    }
}
