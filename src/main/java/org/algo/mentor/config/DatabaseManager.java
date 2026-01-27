package org.algo.mentor.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
//            initializeSampleData();
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

            logger.info("Database tables created successfully");
        } catch (SQLException e) {
            logger.error("Failed to create database tables", e);
            throw e;
        }
    }

    private static void initializeSampleData() throws SQLException {
        logger.info("Initializing sample data");
        Random random = new Random();

        try (PreparedStatement psUser = connection.prepareStatement("INSERT INTO users (username, password, full_name, avatar) VALUES (?, ?, ?, ?)")) {
            psUser.setString(1, "admin");
            psUser.setString(2, "admin123");
            psUser.setString(3, "Alisher Karimov");
            psUser.setString(4, "AK");
            psUser.execute();
            
            psUser.setString(1, "teacher1");
            psUser.setString(2, "pass123");
            psUser.setString(3, "Dilshod Rahmonov");
            psUser.setString(4, "DR");
            psUser.execute();
            
            psUser.setString(1, "teacher2");
            psUser.setString(2, "pass123");
            psUser.setString(3, "Nilufar Azimova");
            psUser.setString(4, "NA");
            psUser.execute();
            
            psUser.setString(1, "1");
            psUser.setString(2, "1");
            psUser.setString(3, "Davronbek Yormamatov");
            psUser.setString(4, "DY");
            psUser.execute();
        }

        try (PreparedStatement psGroup = connection.prepareStatement("INSERT INTO groups (name) VALUES (?)")) {
            psGroup.setString(1, "Algebra - 8-sinf");
            psGroup.execute();
            psGroup.setString(1, "Geometriya - 9-sinf");
            psGroup.execute();
            psGroup.setString(1, "Matematika - 7-sinf");
            psGroup.execute();
            psGroup.setString(1, "Algebra - 10-sinf");
            psGroup.execute();
            psGroup.setString(1, "Geometriya - 11-sinf");
            psGroup.execute();
            psGroup.setString(1, "DTM tayyorlov");
            psGroup.execute();
            psGroup.setString(1, "Olimpiada guruhi");
            psGroup.execute();
        }

        String[] firstNames = {"Ali", "Sardor", "Jasur", "Bobur", "Farxod", "Aziz", "Umid", "Shohruh", "Davron", "Rustam",
                "Malika", "Madina", "Zilola", "Nodira", "Sevara", "Nigora", "Dilfuza", "Kamola", "Feruza", "Mohira",
                "Akmal", "Otabek", "Sanjar", "Timur", "Jamshid", "Eldor", "Nuriddin", "Abdulla", "Muhammadali", "Islom",
                "Zarina", "Gulnora", "Shahzoda", "Munisa", "Lola", "Aziza", "Guzal", "Sevinch", "Yulduz", "Charos"};
        
        String[] lastNames = {"Karimov", "Rahmonov", "Azimov", "Toshmatov", "Mahmudov", "Alimov", "Yusupov", "Hasanov", 
                "Nazarov", "Saidov", "Ismoilov", "Abdullayev", "Ahmadov", "Hamidov", "Juraev", "Qodirov", "Sharipov",
                "Usmanov", "Vohidov", "Ergashev", "Mirzayev", "Normatov", "Otabekov", "Pardaev", "Rajabov"};
        
        String[] phonePrefix = {"90", "91", "93", "94", "95", "97", "98", "99"};
        String[] telegramUsers = {"math_student", "study_hard", "clever_one", "top_student", "genius", "learner", 
                "smart_kid", "future_star", "math_lover", "academy_student"};

        try (PreparedStatement psStudent = connection.prepareStatement(
                "INSERT INTO students (first_name, last_name, phone, telegram_username, parent_name, parent_phone, parent_telegram, is_active) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
            for (int i = 0; i < 50; i++) {
                psStudent.setString(1, firstNames[random.nextInt(firstNames.length)]);
                psStudent.setString(2, lastNames[random.nextInt(lastNames.length)]);
                psStudent.setString(3, "+998" + phonePrefix[random.nextInt(phonePrefix.length)] + String.format("%07d", random.nextInt(10000000)));
                psStudent.setString(4, "@" + telegramUsers[random.nextInt(telegramUsers.length)] + random.nextInt(1000));
                psStudent.setString(5, firstNames[random.nextInt(firstNames.length)] + " " + lastNames[random.nextInt(lastNames.length)]);
                psStudent.setString(6, "+998" + phonePrefix[random.nextInt(phonePrefix.length)] + String.format("%07d", random.nextInt(10000000)));
                psStudent.setString(7, "@parent_" + random.nextInt(10000));
                psStudent.setInt(8, random.nextInt(10) < 9 ? 1 : 0);
                psStudent.execute();
            }
        }

        try (PreparedStatement psSG = connection.prepareStatement("INSERT INTO student_groups (student_id, group_id) VALUES (?, ?)")) {
            for (int studentId = 1; studentId <= 50; studentId++) {
                int numGroups = random.nextInt(2) + 1;
                List<Integer> assignedGroups = new ArrayList<>();
                for (int i = 0; i < numGroups; i++) {
                    int groupId = random.nextInt(7) + 1;
                    if (!assignedGroups.contains(groupId)) {
                        assignedGroups.add(groupId);
                        psSG.setInt(1, studentId);
                        psSG.setInt(2, groupId);
                        psSG.execute();
                    }
                }
            }
        }

        String[] months = {"2024-09", "2024-10", "2024-11", "2024-12", "2025-01"};
        try (PreparedStatement psPayment = connection.prepareStatement(
                "INSERT INTO payments (student_id, amount, payment_from_date, payment_to_date, created_date) VALUES (?, ?, ?, ?, ?)")) {
            for (int studentId = 1; studentId <= 50; studentId++) {
                int numPayments = random.nextInt(4) + 1;
                for (int i = 0; i < numPayments; i++) {
                    String month = months[random.nextInt(months.length)];
                    psPayment.setInt(1, studentId);
                    psPayment.setDouble(2, (random.nextInt(10) + 10) * 10000);
                    psPayment.setString(3, month + "-01");
                    psPayment.setString(4, month + "-30");
                    psPayment.setString(5, month + "-" + String.format("%02d", random.nextInt(28) + 1));
                    psPayment.execute();
                }
            }
        }

        int lessonId = 1;
        try (PreparedStatement psLesson = connection.prepareStatement("INSERT INTO lessons (group_id, lesson_date) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS);
             PreparedStatement psAttendance = connection.prepareStatement("INSERT INTO attendance (lesson_id, student_id, present) VALUES (?, ?, ?)");
             PreparedStatement psHomework = connection.prepareStatement("INSERT INTO homeworks (lesson_id, student_id, score, note) VALUES (?, ?, ?, ?)");
             PreparedStatement psTestSession = connection.prepareStatement("INSERT INTO test_sessions (lesson_id, topic, point_per_correct) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
             PreparedStatement psTestResult = connection.prepareStatement("INSERT INTO test_results (test_session_id, student_id, section, correct_count, total_score) VALUES (?, ?, ?, ?, ?)");
             PreparedStatement psQuestionSession = connection.prepareStatement("INSERT INTO question_sessions (lesson_id, topic, point_per_correct) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
             PreparedStatement psQuestionResult = connection.prepareStatement("INSERT INTO question_results (question_session_id, student_id, section, correct_count, total_score) VALUES (?, ?, ?, ?, ?)");
             PreparedStatement psSelectStudents = connection.prepareStatement("SELECT student_id FROM student_groups WHERE group_id = ?")) {
            
            String[] notes = {"Juda yaxshi", "Yaxshi", "Ortacha", "Yaxshiroq qilish mumkin", "Takrorlash kerak", ""};
            String[] testTopics = {"Kvadrat tenglamalar", "Trigonometriya", "Logarifmlar", "Uchburchaklar", 
                    "Doiralar", "Funksiyalar", "Hosilalar", "Integrallar", "Kombinatorika", "Ehtimollik"};
            String[] questionTopics = {"Amaliy masalalar", "Mantiqiy topshiriqlar", "Olimpiada masalalari", 
                    "DTM testlari", "Takrorlash", "Yangi mavzu"};
            String[] sections = {"A", "B", "C", "D"};
            String[] questionSections = {"Algebra", "Geometriya", "Aralash"};
            
            String[][] groupTimes = {
                {"14:00"}, {"16:00"}, {"10:00"}, {"18:00"}, {"18:00"}, {"16:00"}, {"10:00"}
            };
            
            LocalDate startDate = LocalDate.of(2024, 10, 1);
            for (int groupId = 1; groupId <= 7; groupId++) {
                String timeStr = groupTimes[groupId - 1][0];
                LocalTime lessonTime = LocalTime.parse(timeStr);
                
                for (int day = 0; day < 90; day++) {
                    if (random.nextInt(10) < 4) {
                        LocalDate lessonDate = startDate.plusDays(day);
                        LocalDateTime lessonDateTime = LocalDateTime.of(lessonDate, lessonTime);
                        psLesson.setInt(1, groupId);
                        psLesson.setString(2, lessonDateTime.toString());
                        psLesson.executeUpdate();
                        
                        ResultSet rsLesson = psLesson.getGeneratedKeys();
                        if (rsLesson.next()) {
                            lessonId = rsLesson.getInt(1);
                        }
                        rsLesson.close();
                        
                        psSelectStudents.setInt(1, groupId);
                        ResultSet rs = psSelectStudents.executeQuery();
                        
                        while (rs.next()) {
                            int studentId = rs.getInt("student_id");
                            int present = random.nextInt(10) < 8 ? 1 : 0;
                            psAttendance.setInt(1, lessonId);
                            psAttendance.setInt(2, studentId);
                            psAttendance.setInt(3, present);
                            psAttendance.execute();
                            
                            if (random.nextInt(10) < 6) {
                                double score = random.nextDouble() * 5;
                                String note = notes[random.nextInt(notes.length)];
                                psHomework.setInt(1, lessonId);
                                psHomework.setInt(2, studentId);
                                psHomework.setDouble(3, score);
                                psHomework.setString(4, note);
                                psHomework.execute();
                            }
                        }
                        rs.close();
                        
                        if (random.nextInt(10) < 3) {
                            String topic = testTopics[random.nextInt(testTopics.length)];
                            double pointPerCorrect = 2.0 + random.nextDouble() * 3.0;
                            psTestSession.setInt(1, lessonId);
                            psTestSession.setString(2, topic);
                            psTestSession.setDouble(3, pointPerCorrect);
                            psTestSession.executeUpdate();
                            
                            ResultSet rsTest = psTestSession.getGeneratedKeys();
                            int testSessionId = 0;
                            if (rsTest.next()) {
                                testSessionId = rsTest.getInt(1);
                            }
                            rsTest.close();
                            
                            psSelectStudents.setInt(1, groupId);
                            rs = psSelectStudents.executeQuery();
                            
                            while (rs.next()) {
                                int studentId = rs.getInt("student_id");
                                String section = sections[random.nextInt(sections.length)];
                                int correctCount = random.nextInt(21);
                                double totalScore = correctCount * pointPerCorrect;
                                
                                psTestResult.setInt(1, testSessionId);
                                psTestResult.setInt(2, studentId);
                                psTestResult.setString(3, section);
                                psTestResult.setInt(4, correctCount);
                                psTestResult.setDouble(5, totalScore);
                                psTestResult.execute();
                            }
                            rs.close();
                        }
                        
                        if (random.nextInt(10) < 4) {
                            String topic = questionTopics[random.nextInt(questionTopics.length)];
                            double pointPerCorrect = 1.5 + random.nextDouble() * 2.5;
                            psQuestionSession.setInt(1, lessonId);
                            psQuestionSession.setString(2, topic);
                            psQuestionSession.setDouble(3, pointPerCorrect);
                            psQuestionSession.executeUpdate();
                            
                            ResultSet rsQuestion = psQuestionSession.getGeneratedKeys();
                            int questionSessionId = 0;
                            if (rsQuestion.next()) {
                                questionSessionId = rsQuestion.getInt(1);
                            }
                            rsQuestion.close();
                            
                            psSelectStudents.setInt(1, groupId);
                            rs = psSelectStudents.executeQuery();
                            
                            while (rs.next()) {
                                int studentId = rs.getInt("student_id");
                                String section = questionSections[random.nextInt(questionSections.length)];
                                int correctCount = random.nextInt(16);
                                double totalScore = correctCount * pointPerCorrect;
                                
                                psQuestionResult.setInt(1, questionSessionId);
                                psQuestionResult.setInt(2, studentId);
                                psQuestionResult.setString(3, section);
                                psQuestionResult.setInt(4, correctCount);
                                psQuestionResult.setDouble(5, totalScore);
                                psQuestionResult.execute();
                            }
                            rs.close();
                        }
                    }
                }
            }
        }

        try (PreparedStatement psSchedule = connection.prepareStatement("INSERT INTO schedules (group_id, day_of_week, lesson_time) VALUES (?, ?, ?)")) {
            int[][] scheduleData = {
                {1, 1, 0}, {1, 3, 0}, {1, 5, 0},
                {2, 2, 1}, {2, 4, 1}, {2, 6, 1},
                {3, 1, 2}, {3, 4, 2},
                {4, 2, 3}, {4, 5, 3},
                {5, 3, 3}, {5, 6, 3},
                {6, 1, 1}, {6, 2, 1}, {6, 3, 1}, {6, 4, 1}, {6, 5, 1},
                {7, 6, 4}, {7, 7, 4}
            };
            String[] times = {"14:00", "16:00", "10:00", "18:00", "10:00"};
            
            for (int[] schedule : scheduleData) {
                psSchedule.setInt(1, schedule[0]);
                psSchedule.setInt(2, schedule[1]);
                psSchedule.setString(3, times[schedule[2]]);
                psSchedule.execute();
            }
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
