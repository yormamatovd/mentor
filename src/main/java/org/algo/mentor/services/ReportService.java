package org.algo.mentor.services;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.algo.mentor.config.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReportService {

    public record GroupStat(int id, String name, int studentCount, double avgAttendance, double avgScore) {}
    public record StudentStat(int id, String fullName, double attendanceRate, double avgScore, int rank, int missedLessons) {}
    public record AttendanceDetail(String date, boolean present, double score) {}
    public record LessonStat(String date, double avgScore) {}
    public record SummaryStat(int totalStudents, int totalGroups, int lessonsToday) {}

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
            
            try (PreparedStatement pstmt = conn.prepareStatement("SELECT COUNT(*) FROM lessons WHERE lesson_date LIKE ?")) {
                pstmt.setString(1, today + "%");
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
                "COALESCE((SELECT AVG(score) FROM homeworks h JOIN lessons l ON h.lesson_id = l.id WHERE l.group_id = g.id), 0) as avg_score " +
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
                "COALESCE((SELECT AVG(score) FROM homeworks h JOIN lessons l ON h.lesson_id = l.id WHERE h.student_id = s.id AND l.group_id = ?), 0) as avg_score, " +
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
        String query = "SELECT l.lesson_date, a.present, COALESCE(h.score, 0) as score " +
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
                    details.add(new AttendanceDetail(
                            rs.getString("lesson_date"),
                            rs.getInt("present") == 1,
                            rs.getDouble("score")
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
                "LEFT JOIN homeworks h ON l.id = h.lesson_id " +
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
}
