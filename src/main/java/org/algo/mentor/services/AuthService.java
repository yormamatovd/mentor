package org.algo.mentor.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.algo.mentor.config.DatabaseManager;
import org.algo.mentor.models.User;

public class AuthService {
    public User authenticate(String username, String password) {
        try {
            Connection conn = DatabaseManager.getConnection();
            String query = "SELECT username, full_name, avatar FROM users WHERE username = ? AND password = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, username);
            pstmt.setString(2, password);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String fetchedUsername = rs.getString("username");
                String fullName = rs.getString("full_name");
                String avatar = rs.getString("avatar");
                System.out.println("To'g'ri login");
                return new User(fetchedUsername, fullName, avatar);
            } else {
                System.out.println("Login yoki parol xato");
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
