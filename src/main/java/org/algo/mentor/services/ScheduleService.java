package org.algo.mentor.services;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.algo.mentor.config.DatabaseManager;
import org.algo.mentor.models.Schedule;

import java.sql.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;

public class ScheduleService {
    public static ObservableList<Schedule> getAllSchedules() {
        return searchSchedules(-1);
    }

    public static ObservableList<Schedule> searchSchedules(int groupId) {
        ObservableList<Schedule> schedules = FXCollections.observableArrayList();
        try {
            Connection conn = DatabaseManager.getConnection();
            StringBuilder query = new StringBuilder(
                "SELECT s.id, s.group_id, g.name as group_name, s.day_of_week, s.lesson_time " +
                "FROM schedules s " +
                "JOIN groups g ON s.group_id = g.id "
            );
            
            if (groupId != -1) {
                query.append("WHERE s.group_id = ? ");
            }
            
            PreparedStatement pstmt = conn.prepareStatement(query.toString());
            if (groupId != -1) {
                pstmt.setInt(1, groupId);
            }
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                schedules.add(new Schedule(
                    rs.getInt("id"),
                    rs.getInt("group_id"),
                    rs.getString("group_name"),
                    rs.getInt("day_of_week"),
                    rs.getString("lesson_time")
                ));
            }
            rs.close();
            pstmt.close();
            
            // Sort by next occurrence
            schedules.sort(Comparator.comparing(ScheduleService::getNextOccurrence));
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return schedules;
    }

    public static LocalDateTime getNextOccurrence(Schedule s) {
        LocalDate now = LocalDate.now();
        LocalTime time = LocalTime.parse(s.getLessonTime());
        DayOfWeek targetDay = DayOfWeek.of(s.getDayOfWeek());
        
        LocalDate nextDate = now.with(TemporalAdjusters.nextOrSame(targetDay));
        LocalDateTime next = LocalDateTime.of(nextDate, time);
        
        if (next.isBefore(LocalDateTime.now())) {
            nextDate = now.with(TemporalAdjusters.next(targetDay));
            next = LocalDateTime.of(nextDate, time);
        }
        
        return next;
    }

    public static boolean addSchedule(int groupId, int dayOfWeek, String time) {
        try {
            Connection conn = DatabaseManager.getConnection();
            String query = "INSERT OR REPLACE INTO schedules (group_id, day_of_week, lesson_time) VALUES (?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, groupId);
            pstmt.setInt(2, dayOfWeek);
            pstmt.setString(3, time);
            
            int affected = pstmt.executeUpdate();
            pstmt.close();
            return affected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void deleteSchedule(int id) {
        try {
            Connection conn = DatabaseManager.getConnection();
            String query = "DELETE FROM schedules WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void deleteSchedulesByGroup(int groupId) {
        try {
            Connection conn = DatabaseManager.getConnection();
            String query = "DELETE FROM schedules WHERE group_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, groupId);
            pstmt.executeUpdate();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
