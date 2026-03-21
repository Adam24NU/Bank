import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class Database {

    // The number of users on the ranking
    static final int RANK_NUM = 50;

    /*
     * Create a connection to the SQLite database
     */
    private static Connection getConnection() throws SQLException {
        String url = "jdbc:sqlite:users.db";
        return DriverManager.getConnection(url);
    }

    /*
     * Authenticate the user with username and password
     */
    public static boolean authenticateUser(String username, String password) {
        String query = "SELECT * FROM LoginInformation WHERE username = '" + username + "' AND password = '" + password + "'";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
             ResultSet rs = pstmt.executeQuery();

            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /*
     * Get the balance of the user
     */
    public static int getBalance(String username) {
        String query = "SELECT balance FROM LoginInformation WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("balance");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /*
     * Set the balance for a user
     */
    public static boolean setBalance(String username, int balance) {
        String query = "UPDATE LoginInformation SET balance = balance + ? WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, balance);
            pstmt.setString(2, username);
            int rowsUpdated = pstmt.executeUpdate();

            return rowsUpdated > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /*
     * Transfer balance from one user to another
     */
    public static int transferBalance(String from, String to, int fromBalance, int addBalance) {
        int deductBalance = -addBalance;

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            String updateFrom = "UPDATE LoginInformation SET balance = balance + ? WHERE cardId = ?";
            String updateTo = "UPDATE LoginInformation SET balance = balance + ? WHERE cardId = ?";

            try (PreparedStatement pstmtFrom = conn.prepareStatement(updateFrom);
                 PreparedStatement pstmtTo = conn.prepareStatement(updateTo)) {

                pstmtFrom.setInt(1, deductBalance);
                pstmtFrom.setString(2, from);
                pstmtFrom.executeUpdate();

                pstmtTo.setInt(1, addBalance);
                pstmtTo.setString(2, to);
                pstmtTo.executeUpdate();

                conn.commit();
                return fromBalance - addBalance;
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
                return -1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /*
     * Get customer lists
     */
    public static ArrayList<String[]> getCustomersList() {
        String query = "SELECT username, balance FROM LoginInformation DESC LIMIT ?";
        ArrayList<String[]> customersList = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, RANK_NUM);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String[] user = new String[2];
                user[0] = rs.getString("username");
                user[1] = Integer.toString(rs.getInt("balance"));
                customersList.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return customersList;
    }

    /*
     * Get the profile content of a user
     */
    public static String getProfile(String username) {
        String query = "SELECT profile FROM LoginInformation WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getString("profile");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }

    /*
     * get username by card
     */
    public static String getUsernameByCardId(String cardId) {
        String query = "SELECT username FROM LoginInformation WHERE cardId = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, cardId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getString("username");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }

    /*
     * check is card exist
     */
    public static boolean isCardExist(String cardId) {
        String query = "SELECT username FROM LoginInformation WHERE cardId = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, cardId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /*
     * Get the cardId of a user
     */
    public static String getCardId(String username) {
        String query = "SELECT cardId FROM LoginInformation WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getString("cardId");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }

    /*
     * Get the type of a user
     */
    public static String getType(String username) {
        String query = "SELECT type FROM LoginInformation WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getString("type");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }

    /*
     * Get the profile picture of a user
     */
    public static String getProfilePicture(String username) {
        String query = "SELECT profilePicture FROM LoginInformation WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getString("profilePicture");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }

    /*
     * Add or update profile information
     */
    public static boolean addAccountInfo(String username, String profile) {
        String query = "UPDATE LoginInformation SET profile = ? WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, profile != null ? profile : "");
            pstmt.setString(2, username);

            int rowsUpdated = pstmt.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /*
     * Add or update type information
     */
    public static boolean setUserType(String username, String type) {
        String query = "UPDATE LoginInformation SET type = '" + type + "' WHERE username = '" + username + "'";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            int rowsUpdated = pstmt.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /*
     * Update profile picture
     */
    public static boolean updateProfilePicture(String username, String imageFilePath) {
        if (imageFilePath.endsWith(".exe")) {
            System.out.println("File type .exe is not allowed.");
            return false;
        }

        String query = "UPDATE LoginInformation SET profilePicture = ? WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, imageFilePath);
            pstmt.setString(2, username);
            int rowsUpdated = pstmt.executeUpdate();

            return rowsUpdated > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
