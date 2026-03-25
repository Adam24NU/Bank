/**
 * Small HTML helpers so individual servlets can focus on route behaviour.
 */
public final class HtmlUtil {
    private HtmlUtil() {
    }

    public static String page(String title, String activeRoute, String username, String csrfToken, String body) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html lang='en'>");
        html.append("<head>");
        html.append("<meta charset='UTF-8'>");
        html.append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        html.append("<title>").append(escapeHtml(title)).append("</title>");
        html.append("<link rel='stylesheet' type='text/css' href='/app.css'>");
        html.append("</head>");
        html.append("<body>");

        if (username != null) {
            html.append(shellHeader(activeRoute, username, csrfToken));
        }

        html.append(body);
        html.append("</body>");
        html.append("</html>");
        return html.toString();
    }

    public static String shellHeader(String activeRoute, String username, String csrfToken) {
        return "<header class='site-header'>"
                + "<div class='site-header__inner'>"
                + "<div class='brand'>"
                + "<img src='/logo.png' alt='NorthStar Bank logo' class='brand__logo'>"
                + "<div>"
                + "<div class='brand__eyebrow'>Trusted Digital Banking</div>"
                + "<div class='brand__title'>" + escapeHtml(AppConfig.APP_NAME) + "</div>"
                + "</div>"
                + "</div>"
                + "<nav class='site-nav'>"
                + navLink("/account", "Account", activeRoute)
                + navLink("/transfer", "Transfers", activeRoute)
                + navLink("/balance", "Customers", activeRoute)
                + "</nav>"
                + "<div class='site-header__actions'>"
                + "<div class='site-header__user'>Signed in as <strong>" + escapeHtml(username) + "</strong></div>"
                + "<form action='/logout' method='POST'>"
                + "<input type='hidden' name='csrfToken' value='" + csrfToken + "'>"
                + "<button class='button button--ghost' type='submit'>Sign out</button>"
                + "</form>"
                + "</div>"
                + "</div>"
                + "</header>";
    }

    public static String navLink(String href, String label, String activeRoute) {
        String className = href.equals(activeRoute) ? "site-nav__link site-nav__link--active" : "site-nav__link";
        return "<a class='" + className + "' href='" + href + "'>" + escapeHtml(label) + "</a>";
    }

    public static String alert(String type, String message) {
        if (message == null || message.trim().isEmpty()) {
            return "";
        }
        return "<div class='alert alert--" + escapeHtml(type) + "'>" + escapeHtml(message) + "</div>";
    }

    public static String formInput(String type, String id, String name, String value, String placeholder,
            String autocomplete, boolean required) {
        return "<input class='input' type='" + escapeHtml(type) + "' id='" + escapeHtml(id) + "' name='"
                + escapeHtml(name) + "' value='" + escapeAttr(value) + "' placeholder='" + escapeAttr(placeholder)
                + "'" + (autocomplete == null ? "" : " autocomplete='" + escapeAttr(autocomplete) + "'")
                + (required ? " required" : "") + ">";
    }

    public static String hiddenInput(String name, String value) {
        return "<input type='hidden' name='" + escapeHtml(name) + "' value='" + escapeAttr(value) + "'>";
    }

    public static String textArea(String id, String name, String value, int rows, String placeholder) {
        return "<textarea class='textarea' id='" + escapeHtml(id) + "' name='" + escapeHtml(name) + "' rows='" + rows
                + "' placeholder='" + escapeAttr(placeholder) + "'>" + escapeHtml(value) + "</textarea>";
    }

    public static String escapeAttr(String input) {
        return escapeHtml(input).replace("\n", "&#10;");
    }

    public static String escapeHtml(String input) {
        if (input == null) {
            return "";
        }
        return input
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    public static String nl2br(String input) {
        return escapeHtml(input).replace("\n", "<br>");
    }

    public static String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "Unavailable";
        }
        return "****" + cardNumber.substring(cardNumber.length() - 4);
    }
}
