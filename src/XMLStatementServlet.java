import java.io.IOException;
import java.io.PrintWriter;
import java.io.File;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.StringReader;

/**
 * XML Statement Processor page.
 *
 * Vulnerable version for coursework demonstration.
 * Accepts XML from textarea or sample file and processes both expected
 * and unexpected nodes.
 */
public class XMLStatementServlet extends HttpServlet {
    private static final String SAMPLE_DIR = "xml-samples";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        HttpSession session = req.getSession(false);
        if (session == null) {
            res.sendRedirect("/welcome");
            return;
        }

        String username = (String) session.getAttribute("username");
        renderPage(res, username, "", "", "", null);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        HttpSession session = req.getSession(false);
        if (session == null) {
            res.sendRedirect("/welcome");
            return;
        }

        String username = (String) session.getAttribute("username");
        String statementXml = req.getParameter("statementXml");
        String fileName = req.getParameter("fileName");

        String xmlInput = "";
        String message;

        try {
            if (statementXml != null && !statementXml.trim().isEmpty()) {
                xmlInput = statementXml;
                message = "Parsed XML from pasted input.";
            } else if (fileName != null && !fileName.trim().isEmpty()) {
                xmlInput = loadSampleXml(fileName);
                message = "Loaded and parsed XML sample: " + fileName;
            } else {
                renderPage(res, username, "", "", "Please paste XML or choose a sample file.", null);
                return;
            }

            StatementResult result = parseAndProcessVulnerable(xmlInput);
            renderPage(res, username, xmlInput, fileName, message, result);
        } catch (Exception e) {
            renderPage(res, username, xmlInput, fileName, "Failed to parse XML: " + escapeHtml(e.getMessage()), null);
        }
    }

    private String loadSampleXml(String fileName) throws IOException {
        String safeName = fileName.replaceAll("[^a-zA-Z0-9._-]", "");
        File sampleFile = new File(System.getProperty("user.dir") + File.separator + "WebContext"
                + File.separator + SAMPLE_DIR + File.separator + safeName);

        if (!sampleFile.exists() || !sampleFile.isFile()) {
            throw new IOException("Sample file not found: " + safeName);
        }

        return Files.readString(sampleFile.toPath(), StandardCharsets.UTF_8);
    }

    private StatementResult parseAndProcessVulnerable(String xmlInput) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(new InputSource(new StringReader(xmlInput)));
        doc.getDocumentElement().normalize();

        StatementResult result = new StatementResult();

        result.accountId = readTag(doc, "accountId", "unknown-account");
        result.currency = readTag(doc, "currency", "USD");
        result.feePolicy = readTag(doc, "feePolicy", "STANDARD");
        result.openingBalance = parseDouble(readTag(doc, "openingBalance", "0"));

        NodeList txns = doc.getElementsByTagName("transaction");
        for (int i = 0; i < txns.getLength(); i++) {
            Node txnNode = txns.item(i);
            if (txnNode.getNodeType() == Node.ELEMENT_NODE) {
                Element txn = (Element) txnNode;
                String type = getTagFromParent(txn, "type", "debit").toLowerCase();
                double amount = parseDouble(getTagFromParent(txn, "amount", "0"));
                String description = getTagFromParent(txn, "description", "(no description)");
                result.transactions.add(type + " " + amount + " - " + description);

                if ("credit".equals(type)) {
                    result.creditTotal += amount;
                } else {
                    result.debitTotal += amount;
                }
            }
        }

        // Vulnerable behavior: process unexpected custom XML fields if provided.
        String bonusMultiplier = readTag(doc, "bonusMultiplier", "1");
        result.bonusMultiplier = parseDouble(bonusMultiplier);
        if (result.bonusMultiplier != 1.0) {
            result.notes.add("Applied bonusMultiplier=" + result.bonusMultiplier + " from XML input.");
        }

        double feeAmount = 2.0;
        if ("PREMIUM".equalsIgnoreCase(result.feePolicy)) {
            feeAmount = 0.0;
            result.notes.add("feePolicy PREMIUM applied (fee waived).");
        } else if (!"STANDARD".equalsIgnoreCase(result.feePolicy)) {
            // Vulnerable behavior: unknown policy still accepted and treated as fee waived.
            feeAmount = 0.0;
            result.notes.add("Unknown feePolicy '" + result.feePolicy + "' accepted and fee waived.");
        }

        result.calculatedCredit = result.creditTotal * result.bonusMultiplier;
        result.feeApplied = feeAmount;
        result.finalBalance = result.openingBalance + result.calculatedCredit - result.debitTotal - feeAmount;

        return result;
    }

    private String readTag(Document doc, String tag, String fallback) {
        NodeList nodes = doc.getElementsByTagName(tag);
        if (nodes.getLength() == 0 || nodes.item(0) == null) {
            return fallback;
        }
        return nodes.item(0).getTextContent().trim();
    }

    private String getTagFromParent(Element parent, String tag, String fallback) {
        NodeList nodes = parent.getElementsByTagName(tag);
        if (nodes.getLength() == 0 || nodes.item(0) == null) {
            return fallback;
        }
        return nodes.item(0).getTextContent().trim();
    }

    private double parseDouble(String value) {
        try {
            return Double.parseDouble(value);
        } catch (Exception e) {
            return 0.0;
        }
    }

    private void renderPage(HttpServletResponse res, String username, String xmlInput, String fileName,
            String message, StatementResult result) throws IOException {
        PrintWriter content = res.getWriter();
        res.setContentType("text/html; charset=utf-8");
        res.setStatus(HttpServletResponse.SC_OK);

        content.println("<!DOCTYPE html>");
        content.println("<html lang='en'>");
        content.println("<head>");
        content.println("<meta charset='UTF-8'>");
        content.println("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        content.println("<title>XML Statement Processor</title>");
        content.println("<link rel='stylesheet' type='text/css' href='./Navbar.css'>");
        content.println("<link rel='stylesheet' type='text/css' href='./XMLStatement.css'>");
        content.println("</head>");
        content.println("<body>");

        content.println("<header id='headerNav'>");
        content.println("<div class='header-container'>");
        content.println("<img src='./logo.png' alt='Logo' class='logo' width='150' height='50'>");
        content.println("<span class='hello'>Welcome, " + escapeHtml(username) + "</span>");
        content.println("<nav class='navbar'>");
        content.println("<a class='nav' href='account'>My Account</a>");
        content.println("<a class='nav' href='transfer'>Transfer</a>");
        content.println("<a class='nav' href='balance'>Customers</a>");
        content.println("<a class='nav active-link' href='xml-statement'>XML Statement</a>");
        content.println("<form action='logout' method='POST' class='logoutForm'>");
        content.println("<input value='Log Out' type='submit' class='logoutInput nav'>");
        content.println("</form>");
        content.println("</nav>");
        content.println("</div>");
        content.println("</header>");

        content.println("<main class='main-content'>");
        content.println("<h1>XML Statement Processor</h1>");
        content.println("<p class='subtitle'>Vulnerable demo: unexpected XML fields can affect the final statement result.</p>");

        content.println("<section class='panel'>");
        content.println("<form action='xml-statement' method='POST'>");

        content.println("<label for='fileName'>Load sample XML file</label>");
        content.println("<select name='fileName' id='fileName'>");
        content.println(option("", "-- choose sample --", fileName));
        content.println(option("valid-statement.xml", "valid-statement.xml", fileName));
        content.println(option("attack-bonus-multiplier.xml", "attack-bonus-multiplier.xml", fileName));
        content.println(option("attack-invalid-fee-policy.xml", "attack-invalid-fee-policy.xml", fileName));
        content.println("</select>");

        content.println("<label for='statementXml'>Or paste XML</label>");
        content.println("<textarea id='statementXml' name='statementXml' rows='14' placeholder='Paste statement XML here...'>"
                + escapeHtml(xmlInput == null ? "" : xmlInput) + "</textarea>");

        content.println("<button type='submit' class='submit-btn'>Process Statement XML</button>");
        content.println("</form>");
        content.println("</section>");

        if (message != null && !message.isEmpty()) {
            content.println("<section class='panel message-panel'><strong>Status:</strong> " + escapeHtml(message) + "</section>");
        }

        if (result != null) {
            content.println("<section class='panel'>");
            content.println("<h2>Processed Statement Result</h2>");
            content.println("<table class='result-table'>");
            content.println("<tr><th>Account ID</th><td>" + escapeHtml(result.accountId) + "</td></tr>");
            content.println("<tr><th>Currency</th><td>" + escapeHtml(result.currency) + "</td></tr>");
            content.println("<tr><th>Opening Balance</th><td>" + formatMoney(result.openingBalance) + "</td></tr>");
            content.println("<tr><th>Total Credits</th><td>" + formatMoney(result.creditTotal) + "</td></tr>");
            content.println("<tr><th>Total Debits</th><td>" + formatMoney(result.debitTotal) + "</td></tr>");
            content.println("<tr><th>bonusMultiplier (XML)</th><td>" + result.bonusMultiplier + "</td></tr>");
            content.println("<tr><th>Calculated Credits</th><td>" + formatMoney(result.calculatedCredit) + "</td></tr>");
            content.println("<tr><th>feePolicy (XML)</th><td>" + escapeHtml(result.feePolicy) + "</td></tr>");
            content.println("<tr><th>Fee Applied</th><td>" + formatMoney(result.feeApplied) + "</td></tr>");
            content.println("<tr class='highlight'><th>Final Balance</th><td>" + formatMoney(result.finalBalance) + "</td></tr>");
            content.println("</table>");

            content.println("<h3>Transactions</h3>");
            content.println("<ul class='tx-list'>");
            for (String tx : result.transactions) {
                content.println("<li>" + escapeHtml(tx) + "</li>");
            }
            content.println("</ul>");

            if (!result.notes.isEmpty()) {
                content.println("<h3>Parser Notes</h3>");
                content.println("<ul class='note-list'>");
                for (String note : result.notes) {
                    content.println("<li>" + escapeHtml(note) + "</li>");
                }
                content.println("</ul>");
            }

            content.println("</section>");
        }

        content.println("</main>");
        content.println("</body>");
        content.println("</html>");
    }

    private String option(String value, String label, String selected) {
        boolean isSelected = selected != null && selected.equals(value);
        return "<option value='" + value + "'" + (isSelected ? " selected" : "") + ">" + label + "</option>";
    }

    private String formatMoney(double amount) {
        return String.format("%.2f", amount);
    }

    private String escapeHtml(String input) {
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

    private static class StatementResult {
        String accountId;
        String currency;
        String feePolicy;
        double openingBalance;
        double creditTotal;
        double debitTotal;
        double calculatedCredit;
        double feeApplied;
        double finalBalance;
        double bonusMultiplier = 1.0;
        List<String> transactions = new ArrayList<>();
        List<String> notes = new ArrayList<>();
    }
}
