package org.algo.mentor.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.algo.mentor.config.DatabaseManager;
import org.algo.mentor.models.User;

public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    
    public User authenticate(String username, String password) {
        try {
            logger.debug("Attempting authentication for user: {}", username);
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
                logger.info("Authentication successful for user: {}", username);
                return new User(fetchedUsername, fullName, avatar);
            } else {
                logger.warn("Authentication failed for user: {}. Invalid credentials.", username);
                return null;
            }
        } catch (SQLException e) {
            logger.error("Database error during authentication for user: {}", username, e);
            return null;
        }
    }
}
