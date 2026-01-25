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
            stmt.execute("DELETE FROM payments");
            stmt.execute("DELETE FROM student_groups");
            stmt.execute("DELETE FROM attendance");
            stmt.execute("DELETE FROM homeworks");
            stmt.execute("DELETE FROM test_results");
            stmt.execute("DELETE FROM test_sessions");
            stmt.execute("DELETE FROM question_results");
            stmt.execute("DELETE FROM question_sessions");
            stmt.execute("DELETE FROM lessons");
            stmt.execute("DELETE FROM schedules");
            stmt.execute("DELETE FROM students");
            stmt.execute("DELETE FROM users");
            stmt.execute("DELETE FROM groups");
            
            stmt.execute("DELETE FROM sqlite_sequence WHERE name IN ('payments', 'student_groups', 'students', 'users', 'groups', 'lessons', 'attendance', 'homeworks', 'test_sessions', 'test_results', 'question_sessions', 'question_results', 'schedules')");

            stmt.execute("INSERT INTO users (username, password, full_name, avatar) VALUES ('1', '1', 'Foydalanuvchi', 'AB')");
            stmt.execute("INSERT INTO users (username, password, full_name, avatar) VALUES ('admin', 'admin123', 'Administrator', 'AD')");

            stmt.execute("INSERT INTO groups (name) VALUES ('Matimatika 5-7 sinflar')");
            stmt.execute("INSERT INTO groups (name) VALUES ('Matimatika 10-B')");
            stmt.execute("INSERT INTO groups (name) VALUES ('Matimatika 11-A')");
            stmt.execute("INSERT INTO groups (name) VALUES ('Test gurux')");
            stmt.execute("INSERT INTO groups (name) VALUES ('Yuqori gurux')");

            stmt.execute("INSERT INTO students (first_name, last_name, phone, telegram_username, parent_name, parent_phone, parent_telegram, is_active) VALUES ('Ali', 'Karimov', '+998901234567', 'alikarimov', 'Karim Karimov', '+998901111111', 'karimov_father', 1)");
            stmt.execute("INSERT INTO students (first_name, last_name, phone, telegram_username, parent_name, parent_phone, parent_telegram, is_active) VALUES ('Fatima', 'Abdullayeva', '+998902345678', 'fatima_ab', 'Abdulla Abdullayev', '+998902222222', 'abdullayev_father', 1)");
            stmt.execute("INSERT INTO students (first_name, last_name, phone, telegram_username, parent_name, parent_phone, parent_telegram, is_active) VALUES ('Otabek', 'Shodmonov', '+998903456789', 'otabek_shod', 'Shodmon Shodmonov', '+998903333333', 'shodmonov_father', 1)");
            stmt.execute("INSERT INTO students (first_name, last_name, phone, telegram_username, parent_name, parent_phone, parent_telegram, is_active) VALUES ('Zarina', 'Mirovaliyeva', '+998904567890', 'zarina_mir', 'Mirovat Mirovaliyev', '+998904444444', 'mirovaliyev_father', 1)");
            stmt.execute("INSERT INTO students (first_name, last_name, phone, telegram_username, parent_name, parent_phone, parent_telegram, is_active) VALUES ('Sarvar', 'Normamatov', '+998905678901', 'sarvar_norm', 'Normat Normamatov', '+998905555555', 'normamatov_father', 1)");
            stmt.execute("INSERT INTO students (first_name, last_name, phone, telegram_username, parent_name, parent_phone, parent_telegram, is_active) VALUES ('Dilshod', 'Rahimov', '+998906789012', 'dilshod_rah', 'Rahimjon Rahimov', '+998906666666', 'rahimov_father', 1)");
            stmt.execute("INSERT INTO students (first_name, last_name, phone, telegram_username, parent_name, parent_phone, parent_telegram, is_active) VALUES ('Nodira', 'Isomiddinova', '+998907890123', 'nodira_iso', 'Isomiddin Isomiddinov', '+998907777777', 'isomiddinov_father', 1)");
            stmt.execute("INSERT INTO students (first_name, last_name, phone, telegram_username, parent_name, parent_phone, parent_telegram, is_active) VALUES ('Akmal', 'Tursunov', '+998908901234', 'akmal_tur', 'Tursun Tursunov', '+998908888888', 'tursunov_father', 1)");
            stmt.execute("INSERT INTO students (first_name, last_name, phone, telegram_username, parent_name, parent_phone, parent_telegram, is_active) VALUES ('Mariya', 'Sobirova', '+998909012345', 'mariya_sob', 'Sobir Sobirov', '+998909999999', 'sobirov_father', 1)");
            stmt.execute("INSERT INTO students (first_name, last_name, phone, telegram_username, parent_name, parent_phone, parent_telegram, is_active) VALUES ('Rashid', 'Xudaybekov', '+998910123456', 'rashid_xud', 'Xudayberdi Xudaybekov', '+998910101010', 'xudaybekov_father', 1)");
            stmt.execute("INSERT INTO students (first_name, last_name, phone, telegram_username, parent_name, parent_phone, parent_telegram, is_active) VALUES ('Gulnoza', 'Yusupova', '+998911234567', 'gulnoza_yus', 'Yusup Yusupov', '+998911111111', 'yusupov_father', 1)");
            stmt.execute("INSERT INTO students (first_name, last_name, phone, telegram_username, parent_name, parent_phone, parent_telegram, is_active) VALUES ('Farkhod', 'Abdullayev', '+998912345678', 'farkhod_abd', 'Abdulla Abdullayev', '+998912121212', 'abdullayev_father', 1)");
            stmt.execute("INSERT INTO students (first_name, last_name, phone, telegram_username, parent_name, parent_phone, parent_telegram, is_active) VALUES ('Javlon', 'Ergashev', '+998913456789', 'javlon_erg', 'Ergash Ergashev', '+998913131313', 'ergashev_father', 1)");
            stmt.execute("INSERT INTO students (first_name, last_name, phone, telegram_username, parent_name, parent_phone, parent_telegram, is_active) VALUES ('Oinara', 'Usmanova', '+998914567890', 'oinara_usm', 'Usman Usmanov', '+998914141414', 'usmanov_father', 1)");
            stmt.execute("INSERT INTO students (first_name, last_name, phone, telegram_username, parent_name, parent_phone, parent_telegram, is_active) VALUES ('Timur', 'Karimov', '+998915678901', 'timur_kar', 'Karim Karimov', '+998915151515', 'karimov_father', 1)");
            stmt.execute("INSERT INTO students (first_name, last_name, phone, telegram_username, parent_name, parent_phone, parent_telegram, is_active) VALUES ('Anvar', 'Siddiqov', '+998916789012', 'anvar_sid', 'Siddiq Anvarov', '+998916161616', 'siddiqov_father', 1)");
            stmt.execute("INSERT INTO students (first_name, last_name, phone, telegram_username, parent_name, parent_phone, parent_telegram, is_active) VALUES ('Lola', 'Karimova', '+998917890123', 'lola_kar', 'Karim Lolov', '+998917171717', 'karimova_father', 1)");

            stmt.execute("INSERT INTO student_groups (student_id, group_id) VALUES (1, 1), (2, 1), (3, 1), (4, 1), (5, 1), (6, 1)");
            stmt.execute("INSERT INTO student_groups (student_id, group_id) VALUES (7, 2), (8, 2), (9, 2), (10, 2), (11, 2), (12, 2)");
            stmt.execute("INSERT INTO student_groups (student_id, group_id) VALUES (13, 3), (14, 3), (15, 3), (16, 3), (17, 3)");
            stmt.execute("INSERT INTO student_groups (student_id, group_id) VALUES (1, 4), (2, 4), (3, 4), (4, 4), (5, 4)");
            stmt.execute("INSERT INTO student_groups (student_id, group_id) VALUES (10, 5), (11, 5), (12, 5), (13, 5), (14, 5), (15, 5)");

            stmt.execute("INSERT INTO payments (student_id, amount, payment_from_date, payment_to_date, created_date) VALUES (1, 500000, '2026-01-01', '2026-02-01', '2026-01-01')");
            stmt.execute("INSERT INTO payments (student_id, amount, payment_from_date, payment_to_date, created_date) VALUES (4, 600000, '2026-01-01', '2026-02-01', '2026-01-01')");

            // Random data generation
            System.out.println("Generating random sample data for all groups...");
            Random random = new Random();
            List<Integer> groupIds = new ArrayList<>();
            try (ResultSet rs = stmt.executeQuery("SELECT id FROM groups")) {
                while (rs.next()) groupIds.add(rs.getInt("id"));
            }

            String lessonInsert = "INSERT INTO lessons (group_id, lesson_date) VALUES (?, ?)";
            String attInsert = "INSERT INTO attendance (lesson_id, student_id, present) VALUES (?, ?, ?)";
            String hwInsert = "INSERT INTO homeworks (lesson_id, student_id, score, note) VALUES (?, ?, ?, ?)";
            String scheduleInsert = "INSERT INTO schedules (group_id, day_of_week, lesson_time) VALUES (?, ?, ?)";

            try (PreparedStatement lStmt = connection.prepareStatement(lessonInsert, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement aStmt = connection.prepareStatement(attInsert);
                 PreparedStatement hStmt = connection.prepareStatement(hwInsert);
                 PreparedStatement sStmtAdd = connection.prepareStatement(scheduleInsert)) {
                
                for (int groupId : groupIds) {
                    // Add some weekly recurring schedules for each group (Monday=1, Wednesday=3, Friday=5)
                    sStmtAdd.setInt(1, groupId);
                    sStmtAdd.setInt(2, 1);
                    sStmtAdd.setString(3, "15:00");
                    sStmtAdd.executeUpdate();

                    sStmtAdd.setInt(1, groupId);
                    sStmtAdd.setInt(2, 3);
                    sStmtAdd.setString(3, "15:00");
                    sStmtAdd.executeUpdate();

                    sStmtAdd.setInt(1, groupId);
                    sStmtAdd.setInt(2, 5);
                    sStmtAdd.setString(3, "15:00");
                    sStmtAdd.executeUpdate();

                    int lessonCount = 5 + random.nextInt(4);
                    for (int i = 1; i <= lessonCount; i++) {
                        String date = String.format("2026-01-%02dT15:00:00", 10 + (i * 2));
                        lStmt.setInt(1, groupId);
                        lStmt.setString(2, date);
                        lStmt.executeUpdate();
                        
                        int lessonId;
                        try (ResultSet rs = lStmt.getGeneratedKeys()) {
                            rs.next();
                            lessonId = rs.getInt(1);
                        }

                        List<Integer> studentIds = new ArrayList<>();
                        try (Statement sStmt = connection.createStatement();
                             ResultSet rs = sStmt.executeQuery("SELECT student_id FROM student_groups WHERE group_id = " + groupId)) {
                            while (rs.next()) studentIds.add(rs.getInt("student_id"));
                        }

                        for (int studentId : studentIds) {
                            boolean present = random.nextDouble() < 0.85;
                            aStmt.setInt(1, lessonId);
                            aStmt.setInt(2, studentId);
                            aStmt.setInt(3, present ? 1 : 0);
                            aStmt.executeUpdate();

                            if (present) {
                                double score = 40 + random.nextDouble() * 60;
                                hStmt.setInt(1, lessonId);
                                hStmt.setInt(2, studentId);
                                hStmt.setDouble(3, Math.round(score * 10.0) / 10.0);
                                hStmt.setString(4, score > 80 ? "Yaxshi" : "O'rtacha");
                                hStmt.executeUpdate();
                            }
                        }
                    }
                }
            }
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
