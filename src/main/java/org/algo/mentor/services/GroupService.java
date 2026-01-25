package org.algo.mentor.services;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.algo.mentor.config.DatabaseManager;
import org.algo.mentor.models.Group;

import java.sql.*;

public class GroupService {
    public static ObservableList<Group> getAllGroups() {
        return searchGroups("");
    }

    public static ObservableList<Group> searchGroups(String name) {
        ObservableList<Group> groups = FXCollections.observableArrayList();
        try {
            Connection conn = DatabaseManager.getConnection();
            String query = "SELECT g.id, g.name, (SELECT COUNT(*) FROM student_groups sg WHERE sg.group_id = g.id) as student_count " +
                           "FROM groups g WHERE g.name LIKE ? ORDER BY g.name";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, "%" + name + "%");
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                groups.add(new Group(rs.getInt("id"), rs.getString("name"), rs.getInt("student_count")));
            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return groups;
    }

    public static int addGroup(String name) {
        try {
            Connection conn = DatabaseManager.getConnection();
            String query = "INSERT INTO groups (name) VALUES (?)";
            PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, name);
            
            pstmt.executeUpdate();
            ResultSet rs = pstmt.getGeneratedKeys();
            int id = -1;
            if (rs.next()) id = rs.getInt(1);
            rs.close();
            pstmt.close();
            return id;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static void deleteGroup(int id) {
        try {
            Connection conn = DatabaseManager.getConnection();
            // Delete student associations first
            String query1 = "DELETE FROM student_groups WHERE group_id = ?";
            PreparedStatement pstmt1 = conn.prepareStatement(query1);
            pstmt1.setInt(1, id);
            pstmt1.executeUpdate();
            pstmt1.close();

            // Delete the group
            String query2 = "DELETE FROM groups WHERE id = ?";
            PreparedStatement pstmt2 = conn.prepareStatement(query2);
            pstmt2.setInt(1, id);
            pstmt2.executeUpdate();
            pstmt2.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void renameGroup(int id, String newName) {
        try {
            Connection conn = DatabaseManager.getConnection();
            String query = "UPDATE groups SET name = ? WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, newName);
            pstmt.setInt(2, id);
            pstmt.executeUpdate();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
