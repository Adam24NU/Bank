import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Streams uploaded avatar files. Missing avatars fall back to the bundled
 * default image.
 */
public class AvatarServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        if (WebSecurity.requireAuthenticatedSession(req, res) == null) {
            return;
        }

        String username = req.getParameter("username");
        Database.UserRecord user = ValidationUtil.isSafeUsername(username) ? Database.getUserRecord(username) : null;

        Path imagePath = null;
        if (user != null && user.profilePicture != null && !user.profilePicture.isEmpty()) {
            imagePath = AppConfig.UPLOAD_DIRECTORY.resolve(user.profilePicture).normalize();
            if (!imagePath.startsWith(AppConfig.UPLOAD_DIRECTORY) || !Files.exists(imagePath)) {
                imagePath = null;
            }
        }

        if (imagePath == null) {
            res.sendRedirect("/" + AppConfig.DEFAULT_AVATAR_ASSET);
            return;
        }

        WebSecurity.applySecurityHeaders(res);
        res.setContentType("image/png");
        try (OutputStream outputStream = res.getOutputStream()) {
            Files.copy(imagePath, outputStream);
        }
    }
}
