import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Central application settings used across servlets and bootstrap code.
 */
public final class AppConfig {
    public static final String HOST = "localhost";
    public static final int PORT = 15000;
    public static final int SESSION_TIMEOUT_SECONDS = 30 * 60;
    public static final int SESSION_SCAVENGE_INTERVAL_SECONDS = 20;
    public static final int MAX_PROFILE_LENGTH = 400;
    public static final long MAX_TRANSFER_AMOUNT = 1_000_000L;
    public static final long MAX_UPLOAD_BYTES = 1_024 * 1_024;
    public static final Path UPLOAD_DIRECTORY = Paths.get(System.getProperty("user.dir"), "uploads");
    public static final String DEFAULT_AVATAR_ASSET = "avatar.png";
    public static final String APP_NAME = "NorthStar Bank";

    private AppConfig() {
    }
}
