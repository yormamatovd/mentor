package org.algo.mentor.services;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.algo.mentor.config.DatabaseManager;
import org.algo.mentor.models.Lesson;
import org.algo.mentor.models.LessonDetail;
import org.algo.mentor.models.Student;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class LessonService {

    public static Lesson createLesson(int groupId, LocalDateTime dateTime) {
        try {
            Connection conn = DatabaseManager.getConnection();
            String insert = "INSERT INTO lessons (group_id, lesson_date) VALUES (?, ?)";
            PreparedStatement insertStmt = conn.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS);
            insertStmt.setInt(1, groupId);
            insertStmt.setString(2, dateTime.toString());
            insertStmt.executeUpdate();
            
            ResultSet generatedKeys = insertStmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                Lesson lesson = new Lesson(generatedKeys.getInt(1), groupId, dateTime);
                generatedKeys.close();
                insertStmt.close();
                return lesson;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ObservableList<LessonDetail> getLessonDetails(int lessonId, int groupId) {
        ObservableList<LessonDetail> details = FXCollections.observableArrayList();
        try {
            Connection conn = DatabaseManager.getConnection();
            
            // First, ensure all students in the group have a record in lesson_details for this lesson
            // We use a subquery to avoid duplicates if this is called multiple times
            String syncQuery = "INSERT INTO lesson_details (lesson_id, student_id) " +
                    "SELECT ?, student_id FROM student_groups sg " +
                    "WHERE sg.group_id = ? AND NOT EXISTS (" +
                    "    SELECT 1 FROM lesson_details ld " +
                    "    WHERE ld.lesson_id = ? AND ld.student_id = sg.student_id" +
                    ")";
            PreparedStatement syncStmt = conn.prepareStatement(syncQuery);
            syncStmt.setInt(1, lessonId);
            syncStmt.setInt(2, groupId);
            syncStmt.setInt(3, lessonId);
            syncStmt.executeUpdate();
            syncStmt.close();

            String query = "SELECT ld.*, s.first_name, s.last_name FROM lesson_details ld " +
                    "INNER JOIN students s ON ld.student_id = s.id " +
                    "WHERE ld.lesson_id = ? " +
                    "GROUP BY ld.student_id " +
                    "ORDER BY s.last_name, s.first_name";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, lessonId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Double homeworkScore = (Double) rs.getObject("homework_score");
                Double testScore = (Double) rs.getObject("test_score");
                
                details.add(new LessonDetail(
                        rs.getInt("id"),
                        rs.getInt("lesson_id"),
                        rs.getInt("student_id"),
                        rs.getString("first_name") + " " + rs.getString("last_name"),
                        rs.getInt("attendance") == 1,
                        homeworkScore,
                        rs.getString("test_name"),
                        testScore
                ));
            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return details;
    }

    public static ObservableList<Lesson> getLessonsByGroup(int groupId) {
        ObservableList<Lesson> lessons = FXCollections.observableArrayList();
        try {
            Connection conn = DatabaseManager.getConnection();
            String query = "SELECT DISTINCT l.* FROM lessons l " +
                    "INNER JOIN lesson_details ld ON l.id = ld.lesson_id " +
                    "WHERE l.group_id = ? AND (ld.attendance = 1 OR ld.homework_score IS NOT NULL OR ld.test_score IS NOT NULL OR (ld.test_name IS NOT NULL AND ld.test_name != '')) " +
                    "ORDER BY l.lesson_date DESC, l.id DESC";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, groupId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                lessons.add(new Lesson(
                        rs.getInt("id"),
                        rs.getInt("group_id"),
                        LocalDateTime.parse(rs.getString("lesson_date"))
                ));
            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lessons;
    }

    public static void deleteLesson(int lessonId) {
        try {
            Connection conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false);
            
            // Delete details first
            String deleteDetails = "DELETE FROM lesson_details WHERE lesson_id = ?";
            PreparedStatement pstmtDetails = conn.prepareStatement(deleteDetails);
            pstmtDetails.setInt(1, lessonId);
            pstmtDetails.executeUpdate();
            pstmtDetails.close();
            
            // Delete lesson
            String deleteLesson = "DELETE FROM lessons WHERE id = ?";
            PreparedStatement pstmtLesson = conn.prepareStatement(deleteLesson);
            pstmtLesson.setInt(1, lessonId);
            pstmtLesson.executeUpdate();
            pstmtLesson.close();
            
            conn.commit();
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void saveLessonDetail(LessonDetail detail) {
        try {
            Connection conn = DatabaseManager.getConnection();
            String query = "UPDATE lesson_details SET attendance = ?, homework_score = ?, test_name = ?, test_score = ? WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, detail.isAttendance() ? 1 : 0);
            pstmt.setObject(2, detail.getHomeworkScore());
            pstmt.setString(3, detail.getTestName());
            pstmt.setObject(4, detail.getTestScore());
            pstmt.setInt(5, detail.getId());
            pstmt.executeUpdate();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void saveAllLessonDetails(List<LessonDetail> details) {
        try {
            Connection conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false);
            String query = "UPDATE lesson_details SET attendance = ?, homework_score = ?, test_name = ?, test_score = ? WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            
            for (LessonDetail detail : details) {
                pstmt.setInt(1, detail.isAttendance() ? 1 : 0);
                pstmt.setObject(2, detail.getHomeworkScore());
                pstmt.setString(3, detail.getTestName());
                pstmt.setObject(4, detail.getTestScore());
                pstmt.setInt(5, detail.getId());
                pstmt.addBatch();
            }
            
            pstmt.executeBatch();
            conn.commit();
            conn.setAutoCommit(true);
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
