/**
 * Shared request validation helpers.
 */
public final class ValidationUtil {
    private ValidationUtil() {
    }

    public static boolean isSafeUsername(String username) {
        return username != null && username.matches("[A-Za-z0-9_]{3,32}");
    }

    public static boolean isSafeCardId(String cardId) {
        return cardId != null && cardId.matches("\\d{6,19}");
    }

    public static String normalizeProfile(String profile) {
        if (profile == null) {
            return "";
        }

        String trimmed = profile.replace("\r", "").trim();
        if (trimmed.length() > AppConfig.MAX_PROFILE_LENGTH) {
            trimmed = trimmed.substring(0, AppConfig.MAX_PROFILE_LENGTH);
        }
        return trimmed;
    }

    public static long parseTransferAmount(String amountInput) {
        if (amountInput == null || !amountInput.matches("\\d{1,12}")) {
            return -1L;
        }

        long parsed = Long.parseLong(amountInput);
        if (parsed <= 0L || parsed > AppConfig.MAX_TRANSFER_AMOUNT) {
            return -1L;
        }
        return parsed;
    }
}
