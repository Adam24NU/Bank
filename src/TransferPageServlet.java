import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Secure balance transfer page.
 */
public class TransferPageServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        HttpSession session = WebSecurity.requireAuthenticatedSession(req, res);
        if (session == null) {
            return;
        }

        String username = WebSecurity.currentUsername(session);
        Database.UserRecord user = Database.getUserRecord(username);
        WebSecurity.FlashMessage flash = WebSecurity.consumeFlash(session);
        String csrfToken = WebSecurity.ensureCsrfToken(session);

        StringBuilder body = new StringBuilder();
        body.append("<main class='page-shell'>");
        body.append("<section class='page-hero'>");
        body.append("<div>");
        body.append("<span class='badge'>Payments</span>");
        body.append("<h1 class='page-title'>Move funds between sample accounts</h1>");
        body.append("<p class='page-copy'>Use the transfer flow to explore a clean payment journey with clear validation and feedback.</p>");
        body.append("</div>");
        body.append("</section>");

        if (flash != null) {
            body.append(HtmlUtil.alert(flash.type, flash.message));
        }

        body.append("<section class='content-grid'>");
        body.append("<article class='card stat-card'>");
        body.append("<div class='eyebrow'>Available balance</div>");
        body.append("<div class='stat-card__value'>$").append(user == null ? "0" : user.balance).append("</div>");
        body.append("</article>");
        body.append("<article class='card stat-card'>");
        body.append("<div class='eyebrow'>Your card number</div>");
        body.append("<div class='stat-card__value'>").append(user == null ? "Unavailable" : user.cardId).append("</div>");
        body.append("</article>");
        body.append("</section>");

        body.append("<section class='content-grid'>");
        body.append("<article class='card'>");
        body.append("<h2 class='section-title'>Create transfer</h2>");
        body.append("<p class='section-subtitle'>Transfers are limited to whole currency units and a maximum of $")
                .append(AppConfig.MAX_TRANSFER_AMOUNT).append(" per request.</p>");
        body.append("<form method='POST' action='/transfer' class='stack-lg'>");
        body.append(HtmlUtil.hiddenInput("csrfToken", csrfToken));
        body.append("<div class='field'>");
        body.append("<label class='label' for='to'>Destination card number</label>");
        body.append(HtmlUtil.formInput("text", "to", "to", "", "Enter the recipient card number", "off", true));
        body.append("</div>");
        body.append("<div class='field'>");
        body.append("<label class='label' for='transferAmount'>Amount</label>");
        body.append(HtmlUtil.formInput("text", "transferAmount", "transferAmount", "", "Enter a whole-number amount",
                "off", true));
        body.append("</div>");
        body.append("<button class='button button--primary' type='submit'>Submit transfer</button>");
        body.append("</form>");
        body.append("</article>");

        body.append("<article class='card'>");
        body.append("<h2 class='section-title'>Before you send</h2>");
        body.append("<ul class='simple-list'>");
        body.append("<li>Transfers are available to signed-in users only.</li>");
        body.append("<li>Enter the recipient card number exactly as shown on the destination account.</li>");
        body.append("<li>Use whole-number amounts and submit one payment at a time.</li>");
        body.append("<li>Transfers to the same account are not allowed.</li>");
        body.append("</ul>");
        body.append("</article>");
        body.append("</section>");
        body.append("</main>");

        WebSecurity.applySecurityHeaders(res);
        res.setContentType("text/html; charset=utf-8");
        res.setStatus(HttpServletResponse.SC_OK);
        res.getWriter().print(HtmlUtil.page("Transfers | " + AppConfig.APP_NAME, "/transfer", username, csrfToken,
                body.toString()));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        HttpSession session = WebSecurity.requireAuthenticatedSession(req, res);
        if (session == null) {
            return;
        }

        if (!WebSecurity.validateCsrfToken(req)) {
            WebSecurity.setFlash(session, "error", "Your session token was invalid. Please try again.");
            res.sendRedirect("/transfer");
            return;
        }

        String destinationCard = safe(req.getParameter("to"));
        long amount = ValidationUtil.parseTransferAmount(req.getParameter("transferAmount"));
        if (!ValidationUtil.isSafeCardId(destinationCard) || amount < 0L) {
            WebSecurity.setFlash(session, "error", "Enter a valid destination card and transfer amount.");
            res.sendRedirect("/transfer");
            return;
        }

        Database.TransferResult result = Database.transferBalance(WebSecurity.currentUsername(session), destinationCard,
                amount);
        WebSecurity.setFlash(session, result.success ? "success" : "error", result.message);
        res.sendRedirect("/transfer");
    }

    private String safe(String value) {
        return value == null ? null : value.trim();
    }
}
