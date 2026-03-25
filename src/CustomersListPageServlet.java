import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Customer directory page.
 *
 * Normal users get a safe directory view, while administrators also see
 * balances for operational oversight.
 */
public class CustomersListPageServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        HttpSession session = WebSecurity.requireAuthenticatedSession(req, res);
        if (session == null) {
            return;
        }

        String username = WebSecurity.currentUsername(session);
        Database.UserRecord viewer = Database.getUserRecord(username);
        List<Database.CustomerSummary> customers = Database.getCustomerSummaries();
        String csrfToken = WebSecurity.ensureCsrfToken(session);

        StringBuilder body = new StringBuilder();
        body.append("<main class='page-shell'>");
        body.append("<section class='page-hero'>");
        body.append("<div>");
        body.append("<span class='badge'>Customer Directory</span>");
        body.append("<h1 class='page-title'>Internal customer overview</h1>");
        body.append("<p class='page-copy'>The directory keeps normal browsing useful without exposing sensitive account information to standard users.</p>");
        body.append("</div>");
        body.append("</section>");

        body.append("<section class='card'>");
        body.append("<div class='section-header'>");
        body.append("<div>");
        body.append("<h2 class='section-title'>Customer accounts</h2>");
        body.append("<p class='section-subtitle'>Sorted by balance for quick operational review.</p>");
        body.append("</div>");
        body.append("</div>");
        body.append("<div class='table-wrap'>");
        body.append("<table class='data-table'>");
        body.append("<thead><tr>");
        body.append("<th>User</th>");
        body.append("<th>Role</th>");
        body.append("<th>Card</th>");
        if (viewer != null && viewer.isAdmin()) {
            body.append("<th>Balance</th>");
        }
        body.append("<th></th>");
        body.append("</tr></thead>");
        body.append("<tbody>");

        for (Database.CustomerSummary customer : customers) {
            body.append("<tr>");
            body.append("<td>").append(HtmlUtil.escapeHtml(customer.username)).append("</td>");
            body.append("<td>").append(HtmlUtil.escapeHtml(customer.role)).append("</td>");
            body.append("<td>").append(viewer != null && viewer.isAdmin()
                    ? HtmlUtil.escapeHtml(HtmlUtil.maskCardNumber(customer.cardId))
                    : "Restricted").append("</td>");
            if (viewer != null && viewer.isAdmin()) {
                body.append("<td>$").append(customer.balance).append("</td>");
            }
            body.append("<td><a class='button button--table' href='/account?username=")
                    .append(HtmlUtil.escapeAttr(customer.username))
                    .append("'>View profile</a></td>");
            body.append("</tr>");
        }

        body.append("</tbody></table></div></section></main>");

        WebSecurity.applySecurityHeaders(res);
        res.setContentType("text/html; charset=utf-8");
        res.setStatus(HttpServletResponse.SC_OK);
        res.getWriter().print(HtmlUtil.page("Customers | " + AppConfig.APP_NAME, "/balance", username, csrfToken,
                body.toString()));
    }
}
