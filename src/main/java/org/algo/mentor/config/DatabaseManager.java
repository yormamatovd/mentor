package org.algo.mentor.config;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DatabaseManager {
    private static final String DATABASE_URL = "jdbc:sqlite:mentor.db";
    private static Connection connection;

    public static void initialize() {
        try {
            System.out.println("Starting database initialization...");
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(DATABASE_URL);
            System.out.println("Database connection established");
            createTables();
            initializeSampleData();
            System.out.println("Database initialized successfully");
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void createTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            System.out.println("Creating database tables...");
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
                    "point_per_correct REAL DEFAULT 2.0," +
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
                    "point_per_correct REAL DEFAULT 2.0," +
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

            System.out.println("Tables created successfully");
        }
    }

    private static void initializeSampleData() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            System.out.println("Initializing sample data...");



            stmt.execute("INSERT INTO users (username, password, full_name, avatar) VALUES ('1', '1', 'Davronbek', 'AB')");

            System.out.println("Sample data generated successfully");
        }
    }

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("org.sqlite.JDBC");
                connection = DriverManager.getConnection(DATABASE_URL);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return connection;
    }

    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
