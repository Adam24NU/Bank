import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Login entry point for the application.
 */
public class ViewPageServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        HttpSession session = req.getSession(false);
        if (session != null && WebSecurity.currentUsername(session) != null) {
            res.sendRedirect("/account");
            return;
        }

        String error = req.getParameter("error");
        writeHtml(res, loginPage(error));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        String username = safe(req.getParameter("username"));
        String password = req.getParameter("password");

        if (!ValidationUtil.isSafeUsername(username) || password == null || password.isEmpty()) {
            res.sendRedirect("/welcome?error=Please+enter+a+valid+username+and+password.");
            return;
        }

        if (!Database.authenticateUser(username, password)) {
            res.sendRedirect("/welcome?error=Incorrect+username+or+password.");
            return;
        }

        WebSecurity.startAuthenticatedSession(req, username);
        res.sendRedirect("/account");
    }

    private void writeHtml(HttpServletResponse res, String html) throws IOException {
        WebSecurity.applySecurityHeaders(res);
        res.setContentType("text/html; charset=utf-8");
        res.setStatus(HttpServletResponse.SC_OK);
        PrintWriter writer = res.getWriter();
        writer.print(html);
    }

    private String loginPage(String error) {
        StringBuilder body = new StringBuilder();
        body.append("<main class='auth-shell'>");
        body.append("<section class='auth-hero'>");
        body.append("<div class='auth-hero__content'>");
        body.append("<span class='badge'>NorthStar Bank Template</span>");
        body.append("<h1 class='auth-hero__title'>A modern banking experience shaped for teams to adapt and extend.</h1>");
        body.append("<p class='auth-hero__copy'>")
                .append("NorthStar Bank is a reusable banking web app template with a polished customer dashboard, ")
                .append("clear internal workflows, and neutral starter content for local evaluation and customization.")
                .append("</p>");
        body.append("<div class='hero-metrics'>");
        body.append(metricCard("Accounts", "Review balances, profile details, and account information."));
        body.append(metricCard("Transfers", "Move funds across placeholder customer accounts."));
        body.append(metricCard("Directory", "Browse a clean internal customer overview."));
        body.append("</div>");
        body.append("</div>");
        body.append("</section>");

        body.append("<section class='auth-panel'>");
        body.append("<div class='card card--auth'>");
        body.append("<h2 class='section-title'>Sign in</h2>");
        body.append("<p class='section-subtitle'>Use one of the included sample accounts to explore the template.</p>");
        body.append(HtmlUtil.alert("error", error));
        body.append("<form method='POST' action='/welcome' class='stack-lg'>");
        body.append("<div class='field'>");
        body.append("<label class='label' for='username'>Username</label>");
        body.append(HtmlUtil.formInput("text", "username", "username", "", "Enter your username", "username", true));
        body.append("</div>");
        body.append("<div class='field'>");
        body.append("<label class='label' for='password'>Password</label>");
        body.append(HtmlUtil.formInput("password", "password", "password", "", "Enter your password",
                "current-password", true));
        body.append("</div>");
        body.append("<button class='button button--primary button--full' type='submit'>Access Dashboard</button>");
        body.append("</form>");
        body.append("<div class='divider'></div>");
        body.append("<div class='demo-credentials'>");
        body.append("<div><strong>Admin account:</strong> admin / adminPassword</div>");
        body.append("<div><strong>Customer account:</strong> user1 / user1Password</div>");
        body.append("</div>");
        body.append("</div>");
        body.append("</section>");
        body.append("</main>");

        return HtmlUtil.page("Welcome | " + AppConfig.APP_NAME, "", null, null, body.toString());
    }

    private String metricCard(String title, String copy) {
        return "<article class='metric-card'>"
                + "<div class='metric-card__title'>" + HtmlUtil.escapeHtml(title) + "</div>"
                + "<div class='metric-card__copy'>" + HtmlUtil.escapeHtml(copy) + "</div>"
                + "</article>";
    }

    private String safe(String value) {
        return value == null ? null : value.trim();
    }
}
