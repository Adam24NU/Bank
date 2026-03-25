import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

/**
 * Account dashboard with secure profile editing and avatar upload.
 */
public class AccountPageServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        HttpSession session = WebSecurity.requireAuthenticatedSession(req, res);
        if (session == null) {
            return;
        }

        String viewerUsername = WebSecurity.currentUsername(session);
        Database.UserRecord viewer = Database.getUserRecord(viewerUsername);
        String requestedUser = safe(req.getParameter("username"));
        String pageUsername = ValidationUtil.isSafeUsername(requestedUser) ? requestedUser : viewerUsername;
        Database.UserRecord pageUser = Database.getUserRecord(pageUsername);

        if (viewer == null || pageUser == null) {
            WebSecurity.setFlash(session, "error", "Unable to load the requested account.");
            res.sendRedirect("/welcome");
            return;
        }

        boolean isOwnPage = viewer.username.equals(pageUser.username);
        boolean isAdmin = viewer.isAdmin();
        WebSecurity.FlashMessage flash = WebSecurity.consumeFlash(session);

        WebSecurity.applySecurityHeaders(res);
        res.setContentType("text/html; charset=utf-8");
        res.setStatus(HttpServletResponse.SC_OK);
        res.getWriter().print(renderPage(viewer, pageUser, isOwnPage, isAdmin, WebSecurity.ensureCsrfToken(session), flash));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        HttpSession session = WebSecurity.requireAuthenticatedSession(req, res);
        if (session == null) {
            return;
        }

        String username = WebSecurity.currentUsername(session);
        String contentType = req.getContentType();
        if (contentType != null && contentType.toLowerCase().startsWith("multipart/form-data")) {
            handleAvatarUpload(req, res, session, username);
            return;
        }

        if (!WebSecurity.validateCsrfToken(req)) {
            WebSecurity.setFlash(session, "error", "Your session token was invalid. Please try again.");
            res.sendRedirect("/account");
            return;
        }

        String action = safe(req.getParameter("action"));
        if (!"profile".equals(action)) {
            WebSecurity.setFlash(session, "error", "Unknown account action.");
            res.sendRedirect("/account");
            return;
        }

        String profile = ValidationUtil.normalizeProfile(req.getParameter("profile"));
        if (!Database.updateProfile(username, profile)) {
            WebSecurity.setFlash(session, "error", "Profile update failed.");
        } else {
            WebSecurity.setFlash(session, "success", "Profile updated successfully.");
        }
        res.sendRedirect("/account");
    }

    private void handleAvatarUpload(HttpServletRequest req, HttpServletResponse res, HttpSession session, String username)
            throws IOException {
        if (!ServletFileUpload.isMultipartContent(req)) {
            WebSecurity.setFlash(session, "error", "Invalid upload request.");
            res.sendRedirect("/account");
            return;
        }

        DiskFileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(factory);
        upload.setFileSizeMax(AppConfig.MAX_UPLOAD_BYTES);
        upload.setSizeMax(AppConfig.MAX_UPLOAD_BYTES);

        try {
            List<FileItem> items = upload.parseRequest(req);
            String csrfToken = null;
            FileItem avatarItem = null;

            for (FileItem item : items) {
                if (item.isFormField() && "csrfToken".equals(item.getFieldName())) {
                    csrfToken = item.getString("UTF-8");
                } else if (!item.isFormField() && "profilePic".equals(item.getFieldName())) {
                    avatarItem = item;
                }
            }

            String expectedToken = WebSecurity.ensureCsrfToken(session);
            if (csrfToken == null || !expectedToken.equals(csrfToken)) {
                WebSecurity.setFlash(session, "error", "Your session token was invalid. Please try again.");
                res.sendRedirect("/account");
                return;
            }

            if (avatarItem == null || avatarItem.getSize() <= 0L) {
                WebSecurity.setFlash(session, "error", "Choose a PNG or JPEG image to upload.");
                res.sendRedirect("/account");
                return;
            }

            byte[] imageBytes = avatarItem.get();
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
            if (image == null) {
                WebSecurity.setFlash(session, "error", "Only valid PNG or JPEG images are accepted.");
                res.sendRedirect("/account");
                return;
            }
            if (image.getWidth() > 2500 || image.getHeight() > 2500) {
                WebSecurity.setFlash(session, "error", "Uploaded images must be smaller than 2500x2500 pixels.");
                res.sendRedirect("/account");
                return;
            }

            Files.createDirectories(AppConfig.UPLOAD_DIRECTORY);
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ImageIO.write(image, "png", output);

            String fileName = UUID.randomUUID().toString() + ".png";
            Path targetFile = AppConfig.UPLOAD_DIRECTORY.resolve(fileName).normalize();
            Files.write(targetFile, output.toByteArray());

            Database.UserRecord currentUser = Database.getUserRecord(username);
            if (currentUser != null && currentUser.profilePicture != null && !currentUser.profilePicture.isEmpty()) {
                Path oldFile = AppConfig.UPLOAD_DIRECTORY.resolve(currentUser.profilePicture).normalize();
                if (oldFile.startsWith(AppConfig.UPLOAD_DIRECTORY) && Files.exists(oldFile)) {
                    Files.delete(oldFile);
                }
            }

            if (!Database.updateProfilePicture(username, fileName)) {
                Files.deleteIfExists(targetFile);
                WebSecurity.setFlash(session, "error", "Avatar upload failed.");
            } else {
                WebSecurity.setFlash(session, "success", "Avatar uploaded successfully.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            WebSecurity.setFlash(session, "error", "Unable to process the uploaded image.");
        }

        res.sendRedirect("/account");
    }

    private String renderPage(Database.UserRecord viewer, Database.UserRecord pageUser, boolean isOwnPage,
            boolean isAdmin, String csrfToken, WebSecurity.FlashMessage flash) {
        boolean canViewBalance = isOwnPage || isAdmin;
        boolean canViewMaskedCard = isOwnPage || isAdmin;
        String profileText = pageUser.profile.isEmpty() ? "No profile information has been added yet." : pageUser.profile;

        StringBuilder body = new StringBuilder();
        body.append("<main class='page-shell'>");
        body.append("<section class='page-hero'>");
        body.append("<div>");
        body.append("<span class='badge'>Account Overview</span>");
        body.append("<h1 class='page-title'>")
                .append(HtmlUtil.escapeHtml(isOwnPage ? "Your banking profile" : pageUser.username + "'s profile"))
                .append("</h1>");
        body.append("<p class='page-copy'>")
                .append(isOwnPage
                        ? "Review your profile details, keep your avatar current, and manage the information visible across the application."
                        : "Public-facing profile information is limited by role to avoid exposing sensitive account data.")
                .append("</p>");
        body.append("</div>");
        body.append("<div class='hero-actions'>");
        body.append("<a class='button button--ghost' href='/transfer'>New transfer</a>");
        body.append("<a class='button button--secondary' href='/balance'>Browse customers</a>");
        body.append("</div>");
        body.append("</section>");

        if (flash != null) {
            body.append(HtmlUtil.alert(flash.type, flash.message));
        }

        body.append("<section class='dashboard-grid'>");
        body.append("<article class='card profile-card'>");
        body.append("<div class='profile-card__header'>");
        body.append("<img class='avatar avatar--xl' src='/avatar?username=")
                .append(HtmlUtil.escapeAttr(pageUser.username))
                .append("' alt='Account avatar'>");
        body.append("<div>");
        body.append("<div class='eyebrow'>Role</div>");
        body.append("<h2 class='section-title section-title--tight'>").append(HtmlUtil.escapeHtml(pageUser.username))
                .append("</h2>");
        body.append("<p class='section-subtitle'>").append(HtmlUtil.escapeHtml(pageUser.role)).append(" account</p>");
        body.append("</div>");
        body.append("</div>");
        body.append("<div class='profile-copy'>").append(HtmlUtil.nl2br(profileText)).append("</div>");
        body.append("</article>");

        body.append(summaryCard("Account owner", pageUser.username));
        body.append(summaryCard("Available balance", canViewBalance ? formatCurrency(pageUser.balance) : "Restricted"));
        body.append(summaryCard("Card number", isOwnPage ? pageUser.cardId
                : (canViewMaskedCard ? HtmlUtil.maskCardNumber(pageUser.cardId) : "Restricted")));
        body.append("</section>");

        body.append("<section class='content-grid'>");
        body.append("<article class='card'>");
        body.append("<h2 class='section-title'>Access policy</h2>");
        body.append("<ul class='simple-list'>");
        body.append("<li>Only the signed-in account owner can edit their profile and avatar.</li>");
        body.append("<li>Balance details are visible to the owner and administrators only.</li>");
        body.append("<li>Card numbers are masked for any profile view outside the owner account.</li>");
        body.append("</ul>");
        body.append("</article>");

        if (isOwnPage) {
            body.append("<article class='card card--form'>");
            body.append("<h2 class='section-title'>Update profile</h2>");
            body.append("<p class='section-subtitle'>Keep this short and professional. Maximum ")
                    .append(AppConfig.MAX_PROFILE_LENGTH)
                    .append(" characters.</p>");
            body.append("<form method='POST' action='/account' class='stack-lg'>");
            body.append(HtmlUtil.hiddenInput("csrfToken", csrfToken));
            body.append(HtmlUtil.hiddenInput("action", "profile"));
            body.append("<div class='field'>");
            body.append("<label class='label' for='profile'>Profile description</label>");
            body.append(HtmlUtil.textArea("profile", "profile", pageUser.profile, 7,
                    "Describe the account or responsibilities visible to other signed-in users."));
            body.append("</div>");
            body.append("<button class='button button--primary' type='submit'>Save profile</button>");
            body.append("</form>");
            body.append("</article>");

            body.append("<article class='card card--form'>");
            body.append("<h2 class='section-title'>Upload avatar</h2>");
            body.append("<p class='section-subtitle'>JPEG and PNG only. Files are re-encoded to PNG and limited to 1 MB.</p>");
            body.append("<form method='POST' action='/account' enctype='multipart/form-data' class='stack-lg'>");
            body.append(HtmlUtil.hiddenInput("csrfToken", csrfToken));
            body.append("<div class='field'>");
            body.append("<label class='label' for='profilePic'>Choose image</label>");
            body.append("<input class='input input--file' type='file' id='profilePic' name='profilePic' accept='.png,.jpg,.jpeg,image/png,image/jpeg' required>");
            body.append("</div>");
            body.append("<div class='form-actions'>");
            body.append("<button class='button button--secondary' type='submit'>Upload avatar</button>");
            body.append("</div>");
            body.append("</form>");
            body.append("</article>");
        } else {
            body.append("<article class='card'>");
            body.append("<h2 class='section-title'>Viewing another account</h2>");
            body.append("<p class='section-subtitle'>Sensitive fields stay restricted while still allowing internal profile discovery and staff navigation.</p>");
            body.append("<a class='button button--secondary' href='/account'>Return to your profile</a>");
            body.append("</article>");
        }

        body.append("</section>");
        body.append("</main>");

        return HtmlUtil.page("Account | " + AppConfig.APP_NAME, "/account", viewer.username, csrfToken, body.toString());
    }

    private String summaryCard(String label, String value) {
        return "<article class='card stat-card'>"
                + "<div class='eyebrow'>" + HtmlUtil.escapeHtml(label) + "</div>"
                + "<div class='stat-card__value'>" + HtmlUtil.escapeHtml(value) + "</div>"
                + "</article>";
    }

    private String formatCurrency(long amount) {
        return "$" + amount;
    }

    private String safe(String value) {
        return value == null ? null : value.trim();
    }
}
