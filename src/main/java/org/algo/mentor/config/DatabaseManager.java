package org.algo.mentor.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

public class DatabaseManager {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);
    private static Connection connection;

    public static void initialize() {
        try {
            logger.info("Starting database initialization");
            Class.forName("org.sqlite.JDBC");
            String databaseUrl = AppDirectoryManager.getDatabaseUrl();
            logger.debug("Using database URL: {}", databaseUrl);
            connection = DriverManager.getConnection(databaseUrl);
            logger.info("Database connection established successfully");
            createTables();
            updateSchema();
            logger.info("Database initialized successfully");
        } catch (ClassNotFoundException e) {
            logger.error("SQLite JDBC driver not found in classpath", e);
            throw new RuntimeException("Failed to load database driver", e);
        } catch (SQLException e) {
            logger.error("SQL error during database initialization", e);
            throw new RuntimeException("Failed to initialize database", e);
        } catch (Exception e) {
            logger.error("Unexpected error during database initialization", e);
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    private static void updateSchema() {
        try (Statement stmt = connection.createStatement()) {
            // Add columns if they don't exist
            addColumnIfNotExists(stmt, "lessons", "homework_total_score", "REAL DEFAULT 0");
            addColumnIfNotExists(stmt, "test_sessions", "total_questions", "INTEGER DEFAULT 0");
            addColumnIfNotExists(stmt, "question_sessions", "total_questions", "INTEGER DEFAULT 0");
        } catch (SQLException e) {
            logger.error("Failed to update database schema", e);
        }
    }

    private static void addColumnIfNotExists(Statement stmt, String table, String column, String type) {
        try {
            stmt.execute("ALTER TABLE " + table + " ADD COLUMN " + column + " " + type);
            logger.info("Added column {} to table {}", column, table);
        } catch (SQLException e) {
            if (e.getMessage().contains("duplicate column name")) {
                logger.debug("Column {} already exists in table {}", column, table);
            } else {
                logger.error("Error adding column {} to table {}", column, table, e);
            }
        }
    }

    private static void createTables() throws SQLException {
        logger.info("Creating database tables");
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "username TEXT UNIQUE NOT NULL," +
                    "password TEXT NOT NULL," +
                    "full_name TEXT NOT NULL," +
                    "avatar TEXT" +
                    ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS groups (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT NOT NULL" +
                    ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS students (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "first_name TEXT NOT NULL," +
                    "last_name TEXT NOT NULL," +
                    "phone TEXT," +
                    "telegram_username TEXT," +
                    "parent_name TEXT," +
                    "parent_phone TEXT," +
                    "parent_telegram TEXT," +
                    "is_active INTEGER DEFAULT 1" +
                    ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS student_groups (" +
                    "student_id INTEGER NOT NULL," +
                    "group_id INTEGER NOT NULL," +
                    "PRIMARY KEY(student_id, group_id)," +
                    "FOREIGN KEY(student_id) REFERENCES students(id)," +
                    "FOREIGN KEY(group_id) REFERENCES groups(id)" +
                    ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS payments (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "student_id INTEGER NOT NULL," +
                    "amount REAL NOT NULL," +
                    "payment_from_date TEXT NOT NULL," +
                    "payment_to_date TEXT NOT NULL," +
                    "created_date TEXT NOT NULL," +
                    "FOREIGN KEY(student_id) REFERENCES students(id)" +
                    ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS lessons (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "group_id INTEGER NOT NULL," +
                    "lesson_date TEXT NOT NULL," +
                    "homework_total_score REAL DEFAULT 0," +
                    "FOREIGN KEY(group_id) REFERENCES groups(id)" +
                    ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS attendance (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "lesson_id INTEGER NOT NULL," +
                    "student_id INTEGER NOT NULL," +
                    "present INTEGER DEFAULT 0," +
                    "FOREIGN KEY(lesson_id) REFERENCES lessons(id)," +
                    "FOREIGN KEY(student_id) REFERENCES students(id)," +
                    "UNIQUE(lesson_id, student_id)" +
                    ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS homeworks (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "lesson_id INTEGER NOT NULL," +
                    "student_id INTEGER NOT NULL," +
                    "score REAL," +
                    "note TEXT," +
                    "FOREIGN KEY(lesson_id) REFERENCES lessons(id)," +
                    "FOREIGN KEY(student_id) REFERENCES students(id)," +
                    "UNIQUE(lesson_id, student_id)" +
                    ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS test_sessions (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "lesson_id INTEGER NOT NULL," +
                    "topic TEXT," +
                    "total_questions INTEGER DEFAULT 0," +
                    "FOREIGN KEY(lesson_id) REFERENCES lessons(id)" +
                    ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS test_results (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "test_session_id INTEGER NOT NULL," +
                    "student_id INTEGER NOT NULL," +
                    "section TEXT," +
                    "correct_count INTEGER DEFAULT 0," +
                    "total_score REAL DEFAULT 0," +
                    "FOREIGN KEY(test_session_id) REFERENCES test_sessions(id)," +
                    "FOREIGN KEY(student_id) REFERENCES students(id)," +
                    "UNIQUE(test_session_id, student_id)" +
                    ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS question_sessions (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "lesson_id INTEGER NOT NULL," +
                    "topic TEXT," +
                    "total_questions INTEGER DEFAULT 0," +
                    "FOREIGN KEY(lesson_id) REFERENCES lessons(id)" +
                    ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS question_results (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "question_session_id INTEGER NOT NULL," +
                    "student_id INTEGER NOT NULL," +
                    "section TEXT," +
                    "correct_count INTEGER DEFAULT 0," +
                    "total_score REAL DEFAULT 0," +
                    "FOREIGN KEY(question_session_id) REFERENCES question_sessions(id)," +
                    "FOREIGN KEY(student_id) REFERENCES students(id)," +
                    "UNIQUE(question_session_id, student_id)" +
                    ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS schedules (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "group_id INTEGER NOT NULL," +
                    "day_of_week INTEGER NOT NULL," +
                    "lesson_time TEXT NOT NULL," +
                    "FOREIGN KEY(group_id) REFERENCES groups(id)," +
                    "UNIQUE(group_id, day_of_week)" +
                    ")");

            logger.info("Database tables created successfully");
        } catch (SQLException e) {
            logger.error("Failed to create database tables", e);
            throw e;
        }
    }

    private static void initializeSampleData() throws SQLException {
        logger.info("Initializing sample data");

        try (PreparedStatement psUser = connection.prepareStatement("INSERT INTO users (username, password, full_name, avatar) VALUES (?, ?, ?, ?)")) {
            psUser.setString(1, "admin");
            psUser.setString(2, "2020");
            psUser.setString(3, "Sardor Ahmadov");
            psUser.setString(4, "SA");
            psUser.execute();
        }

        logger.info("Sample data initialized successfully");
    }

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                logger.debug("Reconnecting to database");
                Class.forName("org.sqlite.JDBC");
                String databaseUrl = AppDirectoryManager.getDatabaseUrl();
                connection = DriverManager.getConnection(databaseUrl);
                logger.debug("Database reconnection successful");
            } catch (ClassNotFoundException e) {
                logger.error("SQLite JDBC driver not found during reconnection", e);
                throw new SQLException("Failed to load database driver", e);
            }
        }
        return connection;
    }

    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                logger.info("Closing database connection");
                connection.close();
                logger.info("Database connection closed successfully");
            }
        } catch (SQLException e) {
            logger.error("Error closing database connection", e);
        }
    }
}
