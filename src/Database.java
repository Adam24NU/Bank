import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data-access layer for the banking demo.
 *
 * The application is intentionally small, so the DAO stays lightweight but all
 * SQL is centralized here to keep servlet code focused on HTTP behaviour.
 */
public final class Database {
    private static final String DB_URL = "jdbc:sqlite:users.db";

    private Database() {
    }

    public static boolean authenticateUser(String username, String password) {
        String sql = "SELECT passwordHash FROM LoginInformation WHERE username = ?";
        try (Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return false;
                }
                return PasswordUtil.verifyPassword(password, resultSet.getString("passwordHash"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static UserRecord getUserRecord(String username) {
        String sql = "SELECT username, cardId, type, profile, balance, profilePicture "
                + "FROM LoginInformation WHERE username = ?";
        try (Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return mapUserRecord(resultSet);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<CustomerSummary> getCustomerSummaries() {
        List<CustomerSummary> customers = new ArrayList<>();
        String sql = "SELECT username, cardId, type, balance FROM LoginInformation ORDER BY balance DESC, username ASC";
        try (Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                customers.add(new CustomerSummary(
                        resultSet.getString("username"),
                        resultSet.getString("cardId"),
                        resultSet.getString("type"),
                        resultSet.getLong("balance")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return customers;
    }

    public static boolean updateProfile(String username, String profile) {
        String sql = "UPDATE LoginInformation SET profile = ? WHERE username = ?";
        try (Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, profile);
            statement.setString(2, username);
            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateProfilePicture(String username, String fileName) {
        String sql = "UPDATE LoginInformation SET profilePicture = ? WHERE username = ?";
        try (Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, fileName);
            statement.setString(2, username);
            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static TransferResult transferBalance(String fromUsername, String destinationCardId, long amount) {
        String sourceSql = "SELECT username, cardId, balance FROM LoginInformation WHERE username = ?";
        String destinationSql = "SELECT username, balance FROM LoginInformation WHERE cardId = ?";
        String updateSql = "UPDATE LoginInformation SET balance = ? WHERE username = ?";

        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);

            try (PreparedStatement sourceStatement = connection.prepareStatement(sourceSql);
                    PreparedStatement destinationStatement = connection.prepareStatement(destinationSql);
                    PreparedStatement updateStatement = connection.prepareStatement(updateSql)) {
                sourceStatement.setString(1, fromUsername);
                destinationStatement.setString(1, destinationCardId);

                try (ResultSet source = sourceStatement.executeQuery();
                        ResultSet destination = destinationStatement.executeQuery()) {
                    if (!source.next()) {
                        connection.rollback();
                        return TransferResult.failure("Source account was not found.");
                    }
                    if (!destination.next()) {
                        connection.rollback();
                        return TransferResult.failure("Destination account was not found.");
                    }

                    String sourceCardId = source.getString("cardId");
                    String destinationUsername = destination.getString("username");
                    long sourceBalance = source.getLong("balance");
                    long destinationBalance = destination.getLong("balance");

                    if (sourceCardId.equals(destinationCardId)) {
                        connection.rollback();
                        return TransferResult.failure("You cannot transfer funds to the same account.");
                    }
                    if (amount <= 0L) {
                        connection.rollback();
                        return TransferResult.failure("Transfer amount must be greater than zero.");
                    }
                    if (sourceBalance < amount) {
                        connection.rollback();
                        return TransferResult.failure("Insufficient funds for this transfer.");
                    }

                    updateStatement.setLong(1, sourceBalance - amount);
                    updateStatement.setString(2, fromUsername);
                    updateStatement.executeUpdate();

                    updateStatement.setLong(1, destinationBalance + amount);
                    updateStatement.setString(2, destinationUsername);
                    updateStatement.executeUpdate();

                    connection.commit();
                    return TransferResult.success(sourceBalance - amount);
                }
            } catch (SQLException e) {
                connection.rollback();
                e.printStackTrace();
                return TransferResult.failure("Unable to complete the transfer right now.");
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return TransferResult.failure("Unable to connect to the database.");
        }
    }

    private static UserRecord mapUserRecord(ResultSet resultSet) throws SQLException {
        return new UserRecord(
                resultSet.getString("username"),
                resultSet.getString("cardId"),
                resultSet.getString("type"),
                resultSet.getString("profile"),
                resultSet.getLong("balance"),
                resultSet.getString("profilePicture"));
    }

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public static final class UserRecord {
        public final String username;
        public final String cardId;
        public final String role;
        public final String profile;
        public final long balance;
        public final String profilePicture;

        UserRecord(String username, String cardId, String role, String profile, long balance, String profilePicture) {
            this.username = username;
            this.cardId = cardId;
            this.role = role;
            this.profile = profile == null ? "" : profile;
            this.balance = balance;
            this.profilePicture = profilePicture == null ? "" : profilePicture;
        }

        public boolean isAdmin() {
            return "admin".equalsIgnoreCase(role);
        }
    }

    public static final class CustomerSummary {
        public final String username;
        public final String cardId;
        public final String role;
        public final long balance;

        CustomerSummary(String username, String cardId, String role, long balance) {
            this.username = username;
            this.cardId = cardId;
            this.role = role;
            this.balance = balance;
        }
    }

    public static final class TransferResult {
        public final boolean success;
        public final String message;
        public final long updatedSourceBalance;

        private TransferResult(boolean success, String message, long updatedSourceBalance) {
            this.success = success;
            this.message = message;
            this.updatedSourceBalance = updatedSourceBalance;
        }

        public static TransferResult success(long updatedSourceBalance) {
            return new TransferResult(true, "Transfer completed successfully.", updatedSourceBalance);
        }

        public static TransferResult failure(String message) {
            return new TransferResult(false, message, -1L);
        }
    }
}
