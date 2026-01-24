package org.algo.mentor.services;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.algo.mentor.config.DatabaseManager;
import org.algo.mentor.models.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Darslar bilan bog'liq biznes logikani boshqaruvchi servis
 */
public class LessonService {

    /**
     * Yangi dars yaratish
     */
    public static Lesson createLesson(int groupId, LocalDateTime dateTime) {
        String insert = "INSERT INTO lessons (group_id, lesson_date) VALUES (?, ?)";
        try {
            Connection conn = DatabaseManager.getConnection();
            try (PreparedStatement insertStmt = conn.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
                insertStmt.setInt(1, groupId);
                insertStmt.setString(2, dateTime.toString());
                insertStmt.executeUpdate();
                
                try (ResultSet generatedKeys = insertStmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return new Lesson(generatedKeys.getInt(1), groupId, dateTime);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Dars uchun davomat ma'lumotlarini olish
     */
    public static List<Attendance> getAttendances(int lessonId, int groupId) {
        List<Attendance> list = new ArrayList<>();
        try {
            Connection conn = DatabaseManager.getConnection();
            // Guruhdagi barcha o'quvchilarni davomat jadvaliga sinxronlash (present = 0 default)
            String sync = "INSERT INTO attendance (lesson_id, student_id, present) " +
                    "SELECT ?, student_id, 0 FROM student_groups sg WHERE sg.group_id = ? " +
                    "AND NOT EXISTS (SELECT 1 FROM attendance a WHERE a.lesson_id = ? AND a.student_id = sg.student_id)";
            try (PreparedStatement syncStmt = conn.prepareStatement(sync)) {
                syncStmt.setInt(1, lessonId);
                syncStmt.setInt(2, groupId);
                syncStmt.setInt(3, lessonId);
                syncStmt.executeUpdate();
            }

            String query = "SELECT a.*, COALESCE(s.first_name, '') || ' ' || COALESCE(s.last_name, '') as full_name FROM attendance a " +
                    "JOIN students s ON a.student_id = s.id WHERE a.lesson_id = ? ORDER BY s.last_name";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, lessonId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        String fullName = rs.getString("full_name");
                        list.add(new Attendance(rs.getInt("id"), rs.getInt("lesson_id"), rs.getInt("student_id"), 
                                fullName, rs.getInt("present") == 1));
                    }
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    /**
     * Dars uchun uy vazifalarini olish
     */
    public static List<Homework> getHomeworks(int lessonId, int groupId) {
        List<Homework> list = new ArrayList<>();
        try {
            Connection conn = DatabaseManager.getConnection();
            String sync = "INSERT INTO homeworks (lesson_id, student_id) " +
                    "SELECT ?, student_id FROM student_groups sg WHERE sg.group_id = ? " +
                    "AND NOT EXISTS (SELECT 1 FROM homeworks h WHERE h.lesson_id = ? AND h.student_id = sg.student_id)";
            try (PreparedStatement syncStmt = conn.prepareStatement(sync)) {
                syncStmt.setInt(1, lessonId);
                syncStmt.setInt(2, groupId);
                syncStmt.setInt(3, lessonId);
                syncStmt.executeUpdate();
            }

            String query = "SELECT h.*, COALESCE(s.first_name, '') || ' ' || COALESCE(s.last_name, '') as full_name FROM homeworks h " +
                    "JOIN students s ON h.student_id = s.id WHERE h.lesson_id = ? ORDER BY s.last_name";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, lessonId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        list.add(new Homework(rs.getInt("id"), rs.getInt("lesson_id"), rs.getInt("student_id"), 
                                rs.getString("full_name"), rs.getDouble("score"), rs.getString("note")));
                    }
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    /**
     * Test sessiyalari va natijalarini olish
     */
    public static List<TestSession> getTestSessions(int lessonId, int groupId) {
        List<TestSession> sessions = new ArrayList<>();
        try {
            Connection conn = DatabaseManager.getConnection();
            String query = "SELECT * FROM test_sessions WHERE lesson_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, lessonId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        sessions.add(new TestSession(rs.getInt("id"), rs.getInt("lesson_id"), 
                                rs.getString("topic"), rs.getDouble("point_per_correct")));
                    }
                }
            }
            for (TestSession session : sessions) {
                session.setResults(getTestResults(session.getId(), groupId));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return sessions;
    }

    private static List<TestResult> getTestResults(int sessionId, int groupId) {
        List<TestResult> results = new ArrayList<>();
        try {
            Connection conn = DatabaseManager.getConnection();
            String sync = "INSERT INTO test_results (test_session_id, student_id) " +
                    "SELECT ?, student_id FROM student_groups sg WHERE sg.group_id = ? " +
                    "AND NOT EXISTS (SELECT 1 FROM test_results tr WHERE tr.test_session_id = ? AND tr.student_id = sg.student_id)";
            try (PreparedStatement syncStmt = conn.prepareStatement(sync)) {
                syncStmt.setInt(1, sessionId);
                syncStmt.setInt(2, groupId);
                syncStmt.setInt(3, sessionId);
                syncStmt.executeUpdate();
            }

            String query = "SELECT tr.*, COALESCE(s.first_name, '') || ' ' || COALESCE(s.last_name, '') as full_name FROM test_results tr " +
                    "JOIN students s ON tr.student_id = s.id WHERE tr.test_session_id = ? ORDER BY s.last_name";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, sessionId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        results.add(new TestResult(rs.getInt("id"), rs.getInt("test_session_id"), rs.getInt("student_id"),
                                rs.getString("full_name"), rs.getString("section"), rs.getInt("correct_count"), rs.getDouble("total_score")));
                    }
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return results;
    }

    /**
     * Savol sessiyalarini olish (Test bilan bir xil logika)
     */
    public static List<QuestionSession> getQuestionSessions(int lessonId, int groupId) {
        List<QuestionSession> sessions = new ArrayList<>();
        try {
            Connection conn = DatabaseManager.getConnection();
            String query = "SELECT * FROM question_sessions WHERE lesson_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, lessonId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        sessions.add(new QuestionSession(rs.getInt("id"), rs.getInt("lesson_id"), 
                                rs.getString("topic"), rs.getDouble("point_per_correct")));
                    }
                }
            }
            for (QuestionSession session : sessions) {
                session.setResults(getQuestionResults(session.getId(), groupId));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return sessions;
    }

    private static List<QuestionResult> getQuestionResults(int sessionId, int groupId) {
        List<QuestionResult> results = new ArrayList<>();
        try {
            Connection conn = DatabaseManager.getConnection();
            String sync = "INSERT INTO question_results (question_session_id, student_id) " +
                    "SELECT ?, student_id FROM student_groups sg WHERE sg.group_id = ? " +
                    "AND NOT EXISTS (SELECT 1 FROM question_results qr WHERE qr.question_session_id = ? AND qr.student_id = sg.student_id)";
            try (PreparedStatement syncStmt = conn.prepareStatement(sync)) {
                syncStmt.setInt(1, sessionId);
                syncStmt.setInt(2, groupId);
                syncStmt.setInt(3, sessionId);
                syncStmt.executeUpdate();
            }

            String query = "SELECT qr.*, COALESCE(s.first_name, '') || ' ' || COALESCE(s.last_name, '') as full_name FROM question_results qr " +
                    "JOIN students s ON qr.student_id = s.id WHERE qr.question_session_id = ? ORDER BY s.last_name";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, sessionId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        results.add(new QuestionResult(rs.getInt("id"), rs.getInt("question_session_id"), rs.getInt("student_id"),
                                rs.getString("full_name"), rs.getString("section"), rs.getInt("correct_count"), rs.getDouble("total_score")));
                    }
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return results;
    }

    /**
     * Barcha ma'lumotlarni saqlash (Auto-save uchun)
     */
    public static void saveAllData(List<Attendance> attendances, List<Homework> homeworks, 
                                 List<TestSession> testSessions, List<QuestionSession> questionSessions) {
        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false);

            // Davomatni saqlash
            String attSql = "UPDATE attendance SET present = ? WHERE id = ?";
            try (PreparedStatement attPstmt = conn.prepareStatement(attSql)) {
                for (Attendance a : attendances) {
                    attPstmt.setInt(1, a.isPresent() ? 1 : 0);
                    attPstmt.setInt(2, a.getId());
                    attPstmt.addBatch();
                }
                attPstmt.executeBatch();
            }

            // Uy vazifasini saqlash
            String hwSql = "UPDATE homeworks SET score = ?, note = ? WHERE id = ?";
            try (PreparedStatement hwPstmt = conn.prepareStatement(hwSql)) {
                for (Homework h : homeworks) {
                    hwPstmt.setDouble(1, h.getScore());
                    hwPstmt.setString(2, h.getNote());
                    hwPstmt.setInt(3, h.getId());
                    hwPstmt.addBatch();
                }
                hwPstmt.executeBatch();
            }

            // Test sessiyalari va natijalari
            String tsSql = "UPDATE test_sessions SET topic = ?, point_per_correct = ? WHERE id = ?";
            String trSql = "UPDATE test_results SET section = ?, correct_count = ?, total_score = ? WHERE id = ?";
            try (PreparedStatement tsPstmt = conn.prepareStatement(tsSql);
                 PreparedStatement trPstmt = conn.prepareStatement(trSql)) {
                for (TestSession ts : testSessions) {
                    tsPstmt.setString(1, ts.getTopic());
                    tsPstmt.setDouble(2, ts.getPointPerCorrect());
                    tsPstmt.setInt(3, ts.getId());
                    tsPstmt.addBatch();

                    for (TestResult tr : ts.getResults()) {
                        trPstmt.setString(1, tr.getSection());
                        trPstmt.setInt(2, tr.getCorrectCount());
                        trPstmt.setDouble(3, tr.getTotalScore());
                        trPstmt.setInt(4, tr.getId());
                        trPstmt.addBatch();
                    }
                }
                tsPstmt.executeBatch();
                trPstmt.executeBatch();
            }

            // Savol sessiyalari va natijalari
            String qsSql = "UPDATE question_sessions SET topic = ?, point_per_correct = ? WHERE id = ?";
            String qrSql = "UPDATE question_results SET section = ?, correct_count = ?, total_score = ? WHERE id = ?";
            try (PreparedStatement qsPstmt = conn.prepareStatement(qsSql);
                 PreparedStatement qrPstmt = conn.prepareStatement(qrSql)) {
                for (QuestionSession qs : questionSessions) {
                    qsPstmt.setString(1, qs.getTopic());
                    qsPstmt.setDouble(2, qs.getPointPerCorrect());
                    qsPstmt.setInt(3, qs.getId());
                    qsPstmt.addBatch();

                    for (QuestionResult qr : qs.getResults()) {
                        qrPstmt.setString(1, qr.getSection());
                        qrPstmt.setInt(2, qr.getCorrectCount());
                        qrPstmt.setDouble(3, qr.getTotalScore());
                        qrPstmt.setInt(4, qr.getId());
                        qrPstmt.addBatch();
                    }
                }
                qsPstmt.executeBatch();
                qrPstmt.executeBatch();
            }

            // cleanupEmptySessions(conn); // Vaqtincha o'chirildi

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    private static void cleanupEmptySessions(Connection conn) throws SQLException {
        // 1. Bo'sh test sessiyalarini aniqlash
        String findEmptyTests = "SELECT id FROM test_sessions WHERE id NOT IN (" +
                "SELECT DISTINCT test_session_id FROM test_results WHERE correct_count > 0 OR (section IS NOT NULL AND section != '')" +
                ") AND (topic IS NULL OR topic = '')";
        ResultSet rs = conn.createStatement().executeQuery(findEmptyTests);
        List<Integer> emptyTestIds = new ArrayList<>();
        while (rs.next()) emptyTestIds.add(rs.getInt("id"));

        if (!emptyTestIds.isEmpty()) {
            String ids = emptyTestIds.toString().replace("[", "(").replace("]", ")");
            conn.createStatement().executeUpdate("DELETE FROM test_results WHERE test_session_id IN " + ids);
            conn.createStatement().executeUpdate("DELETE FROM test_sessions WHERE id IN " + ids);
        }

        // 2. Bo'sh savol sessiyalarini aniqlash
        String findEmptyQuestions = "SELECT id FROM question_sessions WHERE id NOT IN (" +
                "SELECT DISTINCT question_session_id FROM question_results WHERE correct_count > 0 OR (section IS NOT NULL AND section != '')" +
                ") AND (topic IS NULL OR topic = '')";
        rs = conn.createStatement().executeQuery(findEmptyQuestions);
        List<Integer> emptyQuestionIds = new ArrayList<>();
        while (rs.next()) emptyQuestionIds.add(rs.getInt("id"));

        if (!emptyQuestionIds.isEmpty()) {
            String ids = emptyQuestionIds.toString().replace("[", "(").replace("]", ")");
            conn.createStatement().executeUpdate("DELETE FROM question_results WHERE question_session_id IN " + ids);
            conn.createStatement().executeUpdate("DELETE FROM question_sessions WHERE id IN " + ids);
        }
    }

    /**
     * Yangi test sessiyasi yaratish
     */
    public static TestSession createTestSession(int lessonId) {
        String sql = "INSERT INTO test_sessions (lesson_id, point_per_correct) VALUES (?, 2.0)";
        try {
            Connection conn = DatabaseManager.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, lessonId);
                pstmt.executeUpdate();
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) return new TestSession(rs.getInt(1), lessonId, "", 2.0);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    /**
     * Yangi savol sessiyasi yaratish
     */
    public static QuestionSession createQuestionSession(int lessonId) {
        String sql = "INSERT INTO question_sessions (lesson_id, point_per_correct) VALUES (?, 2.0)";
        try {
            Connection conn = DatabaseManager.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, lessonId);
                pstmt.executeUpdate();
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) return new QuestionSession(rs.getInt(1), lessonId, "", 2.0);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public static ObservableList<Lesson> getLessonsByGroup(int groupId) {
        ObservableList<Lesson> lessons = FXCollections.observableArrayList();
        try {
            Connection conn = DatabaseManager.getConnection();
            String query = "SELECT l.*, (" +
                    "  SELECT GROUP_CONCAT(topic, ', ') FROM (" +
                    "    SELECT topic FROM test_sessions WHERE lesson_id = l.id AND topic IS NOT NULL AND topic != '' " +
                    "    UNION ALL " +
                    "    SELECT topic FROM question_sessions WHERE lesson_id = l.id AND topic IS NOT NULL AND topic != ''" +
                    "  )" +
                    ") as topics " +
                    "FROM lessons l WHERE l.group_id = ? ORDER BY l.lesson_date DESC";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, groupId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        lessons.add(new Lesson(rs.getInt("id"), rs.getInt("group_id"), 
                                LocalDateTime.parse(rs.getString("lesson_date")), rs.getString("topics")));
                    }
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return lessons;
    }

    public static void deleteTestSession(int sessionId) {
        try {
            Connection conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false);
            try (PreparedStatement resStmt = conn.prepareStatement("DELETE FROM test_results WHERE test_session_id = ?");
                 PreparedStatement sesStmt = conn.prepareStatement("DELETE FROM test_sessions WHERE id = ?")) {
                resStmt.setInt(1, sessionId);
                resStmt.executeUpdate();
                sesStmt.setInt(1, sessionId);
                sesStmt.executeUpdate();
            }
            conn.commit();
            conn.setAutoCommit(true);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public static void deleteQuestionSession(int sessionId) {
        try {
            Connection conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false);
            try (PreparedStatement resStmt = conn.prepareStatement("DELETE FROM question_results WHERE question_session_id = ?");
                 PreparedStatement sesStmt = conn.prepareStatement("DELETE FROM question_sessions WHERE id = ?")) {
                resStmt.setInt(1, sessionId);
                resStmt.executeUpdate();
                sesStmt.setInt(1, sessionId);
                sesStmt.executeUpdate();
            }
            conn.commit();
            conn.setAutoCommit(true);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public static void deleteLesson(int lessonId) {
        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false);
            String[] queries = {
                "DELETE FROM test_results WHERE test_session_id IN (SELECT id FROM test_sessions WHERE lesson_id = ?)",
                "DELETE FROM test_sessions WHERE lesson_id = ?",
                "DELETE FROM question_results WHERE question_session_id IN (SELECT id FROM question_sessions WHERE lesson_id = ?)",
                "DELETE FROM question_sessions WHERE lesson_id = ?",
                "DELETE FROM attendance WHERE lesson_id = ?",
                "DELETE FROM homeworks WHERE lesson_id = ?",
                "DELETE FROM lessons WHERE id = ?"
            };
            for (String q : queries) {
                try (PreparedStatement pstmt = conn.prepareStatement(q)) {
                    pstmt.setInt(1, lessonId);
                    pstmt.executeUpdate();
                }
            }
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }
}
