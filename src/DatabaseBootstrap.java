import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Creates a clean SQLite database with seeded demo users and hashed passwords.
 */
public class DatabaseBootstrap {
    private static final String DB_URL = "jdbc:sqlite:" + Paths.get("built", "classes", "users.db").toString();

    public static void main(String[] args) throws SQLException {
        try (Connection connection = DriverManager.getConnection(DB_URL)) {
            createSchema(connection);
            seedUsers(connection);
        }
    }

    private static void createSchema(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("DROP TABLE IF EXISTS LoginInformation");
            statement.execute("CREATE TABLE LoginInformation ("
                    + "cardId TEXT UNIQUE NOT NULL,"
                    + "username TEXT PRIMARY KEY,"
                    + "passwordHash TEXT NOT NULL,"
                    + "type TEXT NOT NULL,"
                    + "profile TEXT NOT NULL,"
                    + "balance INTEGER NOT NULL,"
                    + "profilePicture TEXT NOT NULL"
                    + ")");
        }
    }

    private static void seedUsers(Connection connection) throws SQLException {
        String insertSql = "INSERT INTO LoginInformation "
                + "(cardId, username, passwordHash, type, profile, balance, profilePicture) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(insertSql)) {
            insertUser(statement, "999999", "admin", "adminPassword", "admin",
                    "Operations administrator account.", 0L);
            insertUser(statement, "111111", "user1", "user1Password", "normal",
                    "Primary account holder for branch demo data.", 6701L);
            insertUser(statement, "222222", "user2", "user2Password", "normal",
                    "Retail banking customer with an active current account.", 5050L);
            insertUser(statement, "333333", "user3", "user3Password", "normal",
                    "Savings customer used for transfer demonstrations.", 950L);
            insertUser(statement, "444444", "user4", "user4Password", "normal",
                    "High balance account used for leaderboard and profile checks.", 162230L);
            insertUser(statement, "555555", "user5", "user5Password", "normal",
                    "Long-standing customer with a large balance sample.", 2147483666L);
            insertUser(statement, "666666", "user6", "user6Password", "normal",
                    "Secondary large balance sample account.", 2147483666L);
        }
    }

    private static void insertUser(PreparedStatement statement, String cardId, String username, String rawPassword,
            String role, String profile, long balance) throws SQLException {
        statement.setString(1, cardId);
        statement.setString(2, username);
        statement.setString(3, PasswordUtil.hashPassword(rawPassword));
        statement.setString(4, role);
        statement.setString(5, profile);
        statement.setLong(6, balance);
        statement.setString(7, "");
        statement.executeUpdate();
    }
}
