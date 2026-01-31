package org.algo.mentor.services;

import org.algo.mentor.config.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReportService {

    public record GroupStat(int id, String name, int studentCount, double avgAttendance, double avgScore) {}
    public record StudentStat(int id, String fullName, double attendanceRate, double avgScore, int rank, int missedLessons) {}
    public record AttendanceDetail(String date, boolean present, double score, String scoreBreakdown) {}
    public record LessonStat(String date, double avgScore) {}
    public record SummaryStat(int totalStudents, int totalGroups, int lessonsToday) {}
    public record UpcomingLesson(int id, String groupName, String time) {}
    public record RiskStudent(int id, String fullName, double attendanceRate, double performanceRate, String groupName) {}
    
    public record TestScore(String topic, double score, int total) {}
    public record HomeworkScore(double score, double total) {}
    public record QuestionScore(String topic, double score, int total) {}
    public record DetailedLessonScore(String date, boolean present, 
            List<TestScore> tests, List<HomeworkScore> homeworks, List<QuestionScore> questions, double totalScore, double totalValue) {}
    
    public record LessonScoreRow(String date, String status, String scoreType, String topic, Double score, Double totalValue) {}

    public static SummaryStat getSummaryStatistics() {
        int students = 0, groups = 0, lessons = 0;
        String today = java.time.LocalDate.now().toString();
        
        try (Connection conn = DatabaseManager.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM students");
                if (rs.next()) students = rs.getInt(1);
                
                rs = stmt.executeQuery("SELECT COUNT(*) FROM groups");
                if (rs.next()) groups = rs.getInt(1);
            }
            
            try (PreparedStatement pstmt = conn.prepareStatement("SELECT COUNT(*) FROM schedules WHERE day_of_week = ?")) {
                pstmt.setInt(1, java.time.LocalDate.now().getDayOfWeek().getValue());
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) lessons = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new SummaryStat(students, groups, lessons);
    }

    public static List<GroupStat> getGroupStatistics() {
        List<GroupStat> stats = new ArrayList<>();
        String query = "SELECT g.id, g.name, " +
                "(SELECT COUNT(*) FROM student_groups WHERE group_id = g.id) as student_count, " +
                "COALESCE((SELECT AVG(CAST(present AS DOUBLE)) * 100 FROM attendance a JOIN lessons l ON a.lesson_id = l.id WHERE l.group_id = g.id), 0) as avg_att, " +
                "COALESCE((" +
                "  (COALESCE((SELECT SUM(h.score) FROM homeworks h JOIN lessons l ON h.lesson_id = l.id WHERE l.group_id = g.id), 0) + " +
                "   COALESCE((SELECT SUM(tr.total_score) FROM test_results tr JOIN test_sessions ts ON tr.test_session_id = ts.id JOIN lessons l ON ts.lesson_id = l.id WHERE l.group_id = g.id), 0) + " +
                "   COALESCE((SELECT SUM(qr.total_score) FROM question_results qr JOIN question_sessions qs ON qr.question_session_id = qs.id JOIN lessons l ON qs.lesson_id = l.id WHERE l.group_id = g.id), 0)" +
                "  ) * 100.0 / NULLIF(" +
                "   COALESCE((SELECT SUM(l.homework_total_score) FROM homeworks h JOIN lessons l ON h.lesson_id = l.id WHERE l.group_id = g.id), 0) + " +
                "   COALESCE((SELECT SUM(ts.total_questions) FROM test_results tr JOIN test_sessions ts ON tr.test_session_id = ts.id JOIN lessons l ON ts.lesson_id = l.id WHERE l.group_id = g.id), 0) + " +
                "   COALESCE((SELECT SUM(qs.total_questions) FROM question_results qr JOIN question_sessions qs ON qr.question_session_id = qs.id JOIN lessons l ON qs.lesson_id = l.id WHERE l.group_id = g.id), 0)" +
                "  , 0)" +
                "), 0) as avg_score " +
                "FROM groups g";
        
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                stats.add(new GroupStat(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("student_count"),
                        rs.getDouble("avg_att"),
                        rs.getDouble("avg_score")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stats;
    }

    public static List<StudentStat> getStudentStatistics(int groupId) {
        List<StudentStat> stats = new ArrayList<>();
        String query = "SELECT s.id, s.first_name || ' ' || s.last_name as full_name, " +
                "COALESCE((SELECT AVG(CAST(present AS DOUBLE)) * 100 FROM attendance a JOIN lessons l ON a.lesson_id = l.id WHERE a.student_id = s.id AND l.group_id = ?), 0) as att_rate, " +
                "COALESCE(" +
                " (SELECT (SUM(earned) * 100.0 / NULLIF(SUM(total), 0)) FROM (" +
                "   SELECT SUM(h.score) as earned, SUM(l.homework_total_score) as total FROM homeworks h JOIN lessons l ON h.lesson_id = l.id WHERE h.student_id = s.id AND l.group_id = ?" +
                "   UNION ALL " +
                "   SELECT SUM(tr.total_score) as earned, SUM(ts.total_questions) as total FROM test_results tr JOIN test_sessions ts ON tr.test_session_id = ts.id JOIN lessons l ON ts.lesson_id = l.id WHERE tr.student_id = s.id AND l.group_id = ?" +
                "   UNION ALL " +
                "   SELECT SUM(qr.total_score) as earned, SUM(qs.total_questions) as total FROM question_results qr JOIN question_sessions qs ON qr.question_session_id = qs.id JOIN lessons l ON qs.lesson_id = l.id WHERE qr.student_id = s.id AND l.group_id = ?" +
                " )" +
                "), 0) as avg_score, " +
                "COALESCE((SELECT COUNT(*) FROM attendance a JOIN lessons l ON a.lesson_id = l.id WHERE a.student_id = s.id AND l.group_id = ? AND a.present = 0), 0) as missed_lessons " +
                "FROM students s " +
                "JOIN student_groups sg ON s.id = sg.student_id " +
                "WHERE sg.group_id = ? " +
                "ORDER BY avg_score DESC";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, groupId);
            pstmt.setInt(2, groupId);
            pstmt.setInt(3, groupId);
            pstmt.setInt(4, groupId);
            pstmt.setInt(5, groupId);
            pstmt.setInt(6, groupId);
            try (ResultSet rs = pstmt.executeQuery()) {
                int rank = 1;
                while (rs.next()) {
                    stats.add(new StudentStat(
                            rs.getInt("id"),
                            rs.getString("full_name"),
                            rs.getDouble("att_rate"),
                            rs.getDouble("avg_score"),
                            rank++,
                            rs.getInt("missed_lessons")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stats;
    }

    public static List<AttendanceDetail> getIndividualStudentAttendance(int studentId, int groupId) {
        List<AttendanceDetail> details = new ArrayList<>();
        String query = "SELECT l.lesson_date, a.present, " +
                "h.score as hw_score, " +
                "(SELECT GROUP_CONCAT(COALESCE(qs.topic, 'Savol') || ': ' || qr.total_score, '; ') " +
                " FROM question_sessions qs JOIN question_results qr ON qs.id = qr.question_session_id " +
                " WHERE qs.lesson_id = l.id AND qr.student_id = a.student_id) as qs_info, " +
                "(SELECT GROUP_CONCAT(COALESCE(ts.topic, 'Test') || ': ' || tr.total_score, '; ') " +
                " FROM test_sessions ts JOIN test_results tr ON ts.id = tr.test_session_id " +
                " WHERE ts.lesson_id = l.id AND tr.student_id = a.student_id) as ts_info " +
                "FROM lessons l " +
                "JOIN attendance a ON l.id = a.lesson_id " +
                "LEFT JOIN homeworks h ON l.id = h.lesson_id AND h.student_id = a.student_id " +
                "WHERE a.student_id = ? AND l.group_id = ? " +
                "ORDER BY l.lesson_date DESC";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, studentId);
            pstmt.setInt(2, groupId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Double hwScore = (Double) rs.getObject("hw_score");
                    String qsInfo = rs.getString("qs_info");
                    String tsInfo = rs.getString("ts_info");
                    
                    StringBuilder breakdown = new StringBuilder();
                    double totalScore = 0;
                    
                    if (hwScore != null) {
                        totalScore += hwScore;
                        breakdown.append("Uy vazifa: ").append(hwScore);
                    } else {
                        breakdown.append("Uy vazifa: -");
                    }
                    
                    if (qsInfo != null && !qsInfo.isEmpty()) {
                        breakdown.append(", ").append(qsInfo);
                        // Extract scores to add to total
                        for (String s : qsInfo.split("; ")) {
                            try {
                                totalScore += Double.parseDouble(s.substring(s.lastIndexOf(":") + 1).trim());
                            } catch (Exception ignored) {}
                        }
                    }
                    
                    if (tsInfo != null && !tsInfo.isEmpty()) {
                        breakdown.append(", ").append(tsInfo);
                        // Extract scores to add to total
                        for (String s : tsInfo.split("; ")) {
                            try {
                                totalScore += Double.parseDouble(s.substring(s.lastIndexOf(":") + 1).trim());
                            } catch (Exception ignored) {}
                        }
                    }

                    details.add(new AttendanceDetail(
                            rs.getString("lesson_date"),
                            rs.getInt("present") == 1,
                            totalScore,
                            breakdown.toString()
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return details;
    }

    public static List<LessonStat> getGroupLessonStatistics(int groupId) {
        List<LessonStat> stats = new ArrayList<>();
        String query = "SELECT l.lesson_date, COALESCE(AVG(h.score), 0) as avg_score " +
                "FROM lessons l " +
                "LEFT JOIN homeworks h ON l.id = h.lesson_id AND h.score IS NOT NULL " +
                "WHERE l.group_id = ? " +
                "GROUP BY l.lesson_date " +
                "ORDER BY l.lesson_date DESC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, groupId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    stats.add(new LessonStat(
                            rs.getString("lesson_date"),
                            rs.getDouble("avg_score")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stats;
    }

    public static List<UpcomingLesson> getUpcomingLessons() {
        List<UpcomingLesson> lessons = new ArrayList<>();
        int dayOfWeek = java.time.LocalDate.now().getDayOfWeek().getValue();
        // SQLite dayofweek is different if we use strftime, but we have day_of_week as INTEGER (1-7)
        // In Java Monday=1, Sunday=7. Let's assume our DB also uses 1-7 for Mon-Sun.
        
        String query = "SELECT s.id, g.name as group_name, s.lesson_time " +
                "FROM schedules s " +
                "JOIN groups g ON s.group_id = g.id " +
                "WHERE s.day_of_week = ? " +
                "ORDER BY s.lesson_time ASC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, dayOfWeek);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    lessons.add(new UpcomingLesson(
                            rs.getInt("id"),
                            rs.getString("group_name"),
                            rs.getString("lesson_time")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lessons;
    }

    public static List<RiskStudent> getAtRiskStudents() {
        List<RiskStudent> students = new ArrayList<>();
        String query = "SELECT s.id, s.first_name || ' ' || s.last_name as full_name, g.name as group_name, " +
                "COALESCE((SELECT AVG(CAST(present AS DOUBLE)) * 100 FROM attendance a JOIN lessons l ON a.lesson_id = l.id WHERE a.student_id = s.id), 0) as attendance_rate, " +
                "COALESCE((" +
                "  COALESCE((SELECT SUM(h.score) FROM homeworks h JOIN lessons l ON h.lesson_id = l.id WHERE h.student_id = s.id), 0) + " +
                "  COALESCE((SELECT SUM(tr.total_score) FROM test_results tr JOIN test_sessions ts ON tr.test_session_id = ts.id JOIN lessons l ON ts.lesson_id = l.id WHERE tr.student_id = s.id), 0) + " +
                "  COALESCE((SELECT SUM(qr.total_score) FROM question_results qr JOIN question_sessions qs ON qr.question_session_id = qs.id JOIN lessons l ON qs.lesson_id = l.id WHERE qr.student_id = s.id), 0)" +
                ") * 100.0 / NULLIF(" +
                "  COALESCE((SELECT SUM(l.homework_total_score) FROM homeworks h JOIN lessons l ON h.lesson_id = l.id WHERE h.student_id = s.id), 0) + " +
                "  COALESCE((SELECT SUM(ts.total_questions) FROM test_results tr JOIN test_sessions ts ON tr.test_session_id = ts.id JOIN lessons l ON ts.lesson_id = l.id WHERE tr.student_id = s.id), 0) + " +
                "  COALESCE((SELECT SUM(qs.total_questions) FROM question_results qr JOIN question_sessions qs ON qr.question_session_id = qs.id JOIN lessons l ON qs.lesson_id = l.id WHERE qr.student_id = s.id), 0)" +
                ", 0), 0) as performance_rate " +
                "FROM students s " +
                "JOIN student_groups sg ON s.id = sg.student_id " +
                "JOIN groups g ON sg.group_id = g.id " +
                "WHERE (SELECT COUNT(*) FROM lessons WHERE group_id = g.id) > 0 " +
                "GROUP BY s.id, g.id " +
                "HAVING attendance_rate < 50 OR performance_rate < 50 " +
                "ORDER BY attendance_rate ASC, performance_rate ASC " +
                "LIMIT 10";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                students.add(new RiskStudent(
                        rs.getInt("id"),
                        rs.getString("full_name"),
                        rs.getDouble("attendance_rate"),
                        rs.getDouble("performance_rate"),
                        rs.getString("group_name")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return students;
    }

    public static List<DetailedLessonScore> getDetailedLessonScores(int studentId, int groupId) {
        List<DetailedLessonScore> details = new ArrayList<>();
        
        record LessonBasic(int id, String date, boolean present) {}
        List<LessonBasic> lessonBasics = new ArrayList<>();
        
        String query = "SELECT l.id, l.lesson_date, a.present " +
                "FROM lessons l " +
                "JOIN attendance a ON l.id = a.lesson_id " +
                "WHERE a.student_id = ? AND l.group_id = ? " +
                "ORDER BY l.lesson_date DESC";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, studentId);
            pstmt.setInt(2, groupId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    lessonBasics.add(new LessonBasic(
                            rs.getInt("id"),
                            rs.getString("lesson_date"),
                            rs.getInt("present") == 1
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        for (LessonBasic lesson : lessonBasics) {
            List<TestScore> tests = getTestScores(lesson.id, studentId);
            List<HomeworkScore> homeworks = getHomeworkScores(lesson.id, studentId);
            List<QuestionScore> questions = getQuestionScores(lesson.id, studentId);
            
            double totalScore = 0;
            double totalValue = 0;
            for (TestScore t : tests) {
                totalScore += t.score();
                totalValue += t.total();
            }
            for (HomeworkScore h : homeworks) {
                totalScore += h.score();
                totalValue += h.total();
            }
            for (QuestionScore q : questions) {
                totalScore += q.score();
                totalValue += q.total();
            }
            
            details.add(new DetailedLessonScore(lesson.date, lesson.present, tests, homeworks, questions, totalScore, totalValue));
        }
        
        return details;
    }
    
    public static List<LessonScoreRow> getLessonScoreRows(int studentId, int groupId) {
        List<LessonScoreRow> rows = new ArrayList<>();
        List<DetailedLessonScore> details = getDetailedLessonScores(studentId, groupId);
        
        for (DetailedLessonScore detail : details) {
            String dateStr = detail.date();
            String statusStr = detail.present() ? "Kelgan" : "Kelmagan";
            
            if (detail.tests().isEmpty() && detail.homeworks().isEmpty() && detail.questions().isEmpty()) {
                rows.add(new LessonScoreRow(dateStr, statusStr, "-", "-", 0.0, 0.0));
            } else {
                for (TestScore test : detail.tests()) {
                    Double score = detail.present() ? test.score() : null;
                    rows.add(new LessonScoreRow(dateStr, statusStr, "Test", test.topic(), score, (double) test.total()));
                }
                
                for (HomeworkScore hw : detail.homeworks()) {
                    Double score = detail.present() ? hw.score() : null;
                    rows.add(new LessonScoreRow(dateStr, statusStr, "Uy vazifa", "-", score, hw.total()));
                }
                
                for (QuestionScore q : detail.questions()) {
                    Double score = detail.present() ? q.score() : null;
                    rows.add(new LessonScoreRow(dateStr, statusStr, "Savol", q.topic(), score, (double) q.total()));
                }
            }
        }
        
        return rows;
    }
    
    private static List<TestScore> getTestScores(int lessonId, int studentId) {
        List<TestScore> scores = new ArrayList<>();
        String query = "SELECT ts.topic, tr.total_score, ts.total_questions " +
                "FROM test_sessions ts " +
                "LEFT JOIN test_results tr ON ts.id = tr.test_session_id AND tr.student_id = ? " +
                "WHERE ts.lesson_id = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, studentId);
            pstmt.setInt(2, lessonId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String topic = rs.getString("topic");
                    if (topic == null || topic.isEmpty()) topic = "Test";
                    double score = rs.getDouble("total_score");
                    if (rs.wasNull()) score = 0; // Use 0 for calculations, but we'll handle null for display later
                    scores.add(new TestScore(topic, score, rs.getInt("total_questions")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return scores;
    }
    
    private static List<HomeworkScore> getHomeworkScores(int lessonId, int studentId) {
        List<HomeworkScore> scores = new ArrayList<>();
        String query = "SELECT h.score, l.homework_total_score " +
                "FROM lessons l " +
                "LEFT JOIN homeworks h ON l.id = h.lesson_id AND h.student_id = ? " +
                "WHERE l.id = ? AND l.homework_total_score > 0";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, studentId);
            pstmt.setInt(2, lessonId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    double total = rs.getDouble("homework_total_score");
                    double score = rs.getDouble("score");
                    if (rs.wasNull()) score = 0;
                    scores.add(new HomeworkScore(score, total));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return scores;
    }
    
    private static List<QuestionScore> getQuestionScores(int lessonId, int studentId) {
        List<QuestionScore> scores = new ArrayList<>();
        String query = "SELECT qs.topic, qr.total_score, qs.total_questions " +
                "FROM question_sessions qs " +
                "LEFT JOIN question_results qr ON qs.id = qr.question_session_id AND qr.student_id = ? " +
                "WHERE qs.lesson_id = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, studentId);
            pstmt.setInt(2, lessonId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String topic = rs.getString("topic");
                    if (topic == null || topic.isEmpty()) topic = "Savol";
                    double score = rs.getDouble("total_score");
                    if (rs.wasNull()) score = 0;
                    scores.add(new QuestionScore(topic, score, rs.getInt("total_questions")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return scores;
    }
}
