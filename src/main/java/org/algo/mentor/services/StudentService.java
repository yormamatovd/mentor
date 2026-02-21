package org.algo.mentor.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.algo.mentor.config.DatabaseManager;
import org.algo.mentor.models.Student;

public class StudentService {
    private static final Logger logger = LoggerFactory.getLogger(StudentService.class);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static ObservableList<Student> getAllStudents() {
        ObservableList<Student> students = FXCollections.observableArrayList();
        try {
            Connection conn = DatabaseManager.getConnection();
            String query = "SELECT * FROM students ORDER BY last_name, first_name";
            Statement stmt = conn.createStatement();

            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                Student student = createStudentFromResultSet(rs);
                students.add(student);
            }
            logger.info("Loaded {} total students", students.size());
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            logger.error("Error loading all students", e);
        }
        return students;
    }

    public static ObservableList<Student> getStudentsByGroup(int groupId) {
        ObservableList<Student> students = FXCollections.observableArrayList();
        try {
            Connection conn = DatabaseManager.getConnection();
            String query = "SELECT s.* FROM students s " +
                    "INNER JOIN student_groups sg ON s.id = sg.student_id " +
                    "WHERE sg.group_id = ? ORDER BY s.last_name, s.first_name";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, groupId);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Student student = createStudentFromResultSet(rs);
                students.add(student);
            }
            logger.info("Loaded {} students for group {}", students.size(), groupId);
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            logger.error("Error loading students for group {}", groupId, e);
        }
        return students;
    }

    public static ObservableList<Student> getStudentsByName(String searchText) {
        ObservableList<Student> students = FXCollections.observableArrayList();
        try {
            Connection conn = DatabaseManager.getConnection();
            String query = "SELECT * FROM students WHERE first_name LIKE ? OR last_name LIKE ? ORDER BY last_name, first_name";
            PreparedStatement pstmt = conn.prepareStatement(query);
            String searchPattern = "%" + searchText + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Student student = createStudentFromResultSet(rs);
                students.add(student);
            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            logger.error("Error searching students by name: {}", searchText, e);
        }
        return students;
    }

    public static ObservableList<Student> getStudentsByPhone(String phone) {
        ObservableList<Student> students = FXCollections.observableArrayList();
        try {
            Connection conn = DatabaseManager.getConnection();
            String query = "SELECT * FROM students WHERE phone LIKE ? ORDER BY last_name, first_name";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, "%" + phone + "%");

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Student student = createStudentFromResultSet(rs);
                students.add(student);
            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            logger.error("Error searching students by phone: {}", phone, e);
        }
        return students;
    }

    public static ObservableList<Student> getStudentsByActiveStatus(boolean isActive) {
        ObservableList<Student> students = FXCollections.observableArrayList();
        try {
            Connection conn = DatabaseManager.getConnection();
            String query = "SELECT * FROM students WHERE is_active = ? ORDER BY last_name, first_name";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, isActive ? 1 : 0);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Student student = createStudentFromResultSet(rs);
                students.add(student);
            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            logger.error("Error loading students by active status: {}", isActive, e);
        }
        return students;
    }

    public static Student getStudentById(int studentId) {
        try {
            Connection conn = DatabaseManager.getConnection();
            String query = "SELECT * FROM students WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, studentId);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return createStudentFromResultSet(rs);
            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            logger.error("Error getting student by id: {}", studentId, e);
        }
        return null;
    }

    public static int addStudent(String firstName, String lastName, String phone,
                                String telegramUsername, String parentName, String parentPhone,
                                String parentTelegram) {
        try {
            Connection conn = DatabaseManager.getConnection();
            String query = "INSERT INTO students (first_name, last_name, phone, " +
                    "telegram_username, parent_name, parent_phone, parent_telegram, is_active) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, 1)";
            PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, firstName);
            pstmt.setString(2, lastName);
            pstmt.setString(3, phone);
            pstmt.setString(4, telegramUsername);
            pstmt.setString(5, parentName);
            pstmt.setString(6, parentPhone);
            pstmt.setString(7, parentTelegram);

            pstmt.executeUpdate();
            ResultSet rs = pstmt.getGeneratedKeys();
            int studentId = -1;
            if (rs.next()) {
                studentId = rs.getInt(1);
            }
            rs.close();
            pstmt.close();
            logger.info("Added new student: {} {} (ID: {})", firstName, lastName, studentId);
            return studentId;
        } catch (SQLException e) {
            logger.error("Error adding student: {} {}", firstName, lastName, e);
            return -1;
        }
    }

    public static void addStudentToGroup(int studentId, int groupId) {
        try {
            Connection conn = DatabaseManager.getConnection();
            String query = "INSERT OR IGNORE INTO student_groups (student_id, group_id) VALUES (?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, studentId);
            pstmt.setInt(2, groupId);

            pstmt.executeUpdate();
            pstmt.close();
            logger.debug("Added student {} to group {}", studentId, groupId);
        } catch (SQLException e) {
            logger.error("Error adding student {} to group {}", studentId, groupId, e);
        }
    }

    public static void removeStudentFromGroup(int studentId, int groupId) {
        try {
            Connection conn = DatabaseManager.getConnection();
            String query = "DELETE FROM student_groups WHERE student_id = ? AND group_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, studentId);
            pstmt.setInt(2, groupId);

            pstmt.executeUpdate();
            pstmt.close();
            logger.debug("Removed student {} from group {}", studentId, groupId);
        } catch (SQLException e) {
            logger.error("Error removing student {} from group {}", studentId, groupId, e);
        }
    }

    public static ObservableList<Integer> getGroupIdsByStudent(int studentId) {
        ObservableList<Integer> groupIds = FXCollections.observableArrayList();
        try {
            Connection conn = DatabaseManager.getConnection();
            String query = "SELECT group_id FROM student_groups WHERE student_id = ? ORDER BY group_id";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, studentId);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                groupIds.add(rs.getInt("group_id"));
            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            logger.error("Error getting groups for student {}", studentId, e);
        }
        return groupIds;
    }

    public static void updateStudent(int studentId, String firstName, String lastName, String phone,
                                    String telegramUsername, String parentName, String parentPhone,
                                    String parentTelegram) {
        try {
            Connection conn = DatabaseManager.getConnection();
            String query = "UPDATE students SET first_name = ?, last_name = ?, phone = ?, " +
                    "telegram_username = ?, parent_name = ?, parent_phone = ?, parent_telegram = ? " +
                    "WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, firstName);
            pstmt.setString(2, lastName);
            pstmt.setString(3, phone);
            pstmt.setString(4, telegramUsername);
            pstmt.setString(5, parentName);
            pstmt.setString(6, parentPhone);
            pstmt.setString(7, parentTelegram);
            pstmt.setInt(8, studentId);

            pstmt.executeUpdate();
            pstmt.close();
            logger.info("Updated student {} {}", firstName, lastName);
        } catch (SQLException e) {
            logger.error("Error updating student {}", studentId, e);
        }
    }

    public static void setStudentActive(int studentId, boolean isActive) {
        try {
            Connection conn = DatabaseManager.getConnection();
            String query = "UPDATE students SET is_active = ? WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, isActive ? 1 : 0);
            pstmt.setInt(2, studentId);

            pstmt.executeUpdate();
            pstmt.close();
            logger.info("Set student {} active status to {}", studentId, isActive);
        } catch (SQLException e) {
            logger.error("Error setting student {} active status", studentId, e);
        }
    }

    public static void deleteStudent(int studentId) {
        try {
            Connection conn = DatabaseManager.getConnection();
            
            String query1 = "DELETE FROM student_groups WHERE student_id = ?";
            PreparedStatement pstmt1 = conn.prepareStatement(query1);
            pstmt1.setInt(1, studentId);
            pstmt1.executeUpdate();
            pstmt1.close();
            
            String query2 = "DELETE FROM students WHERE id = ?";
            PreparedStatement pstmt2 = conn.prepareStatement(query2);
            pstmt2.setInt(1, studentId);
            pstmt2.executeUpdate();
            pstmt2.close();
            
            logger.info("Deleted student {}", studentId);
        } catch (SQLException e) {
            logger.error("Error deleting student {}", studentId, e);
        }
    }

    public static boolean isStudentPaymentValid(int studentId) {
        try {
            Connection conn = DatabaseManager.getConnection();
            String today = LocalDate.now().format(DATE_FORMAT);
            String query = "SELECT COUNT(*) FROM payments WHERE student_id = ? AND payment_to_date >= ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, studentId);
            pstmt.setString(2, today);
            
            ResultSet rs = pstmt.executeQuery();
            boolean hasValidPayment = false;
            if (rs.next()) {
                hasValidPayment = rs.getInt(1) > 0;
            }
            rs.close();
            pstmt.close();
            return hasValidPayment;
        } catch (SQLException e) {
            logger.error("Error checking student payment validity for {}", studentId, e);
            return false;
        }
    }

    public static boolean updateStudentPaymentStatus() {
        try {
            Connection conn = DatabaseManager.getConnection();
            String today = LocalDate.now().format(DATE_FORMAT);

            String activeQuery = "UPDATE students SET is_active = 1 WHERE id IN " +
                    "(SELECT DISTINCT student_id FROM payments WHERE payment_to_date >= ?)";
            PreparedStatement pstmt1 = conn.prepareStatement(activeQuery);
            pstmt1.setString(1, today);
            pstmt1.executeUpdate();
            pstmt1.close();

            String inactiveQuery = "UPDATE students SET is_active = 0 WHERE id NOT IN " +
                    "(SELECT DISTINCT student_id FROM payments WHERE payment_to_date >= ?)";
            PreparedStatement pstmt2 = conn.prepareStatement(inactiveQuery);
            pstmt2.setString(1, today);
            pstmt2.executeUpdate();
            pstmt2.close();
            
            logger.info("Updated student payment status");
            return true;
        } catch (SQLException e) {
            logger.error("Error updating student payment status", e);
            return false;
        }
    }

    public static ObservableList<Student> searchStudentsGlobal(String queryText) {
        ObservableList<Student> students = FXCollections.observableArrayList();
        try {
            Connection conn = DatabaseManager.getConnection();
            String query = "SELECT * FROM students WHERE " +
                    "first_name LIKE ? OR last_name LIKE ? OR phone LIKE ? " +
                    "ORDER BY last_name, first_name";
            PreparedStatement pstmt = conn.prepareStatement(query);
            String pattern = "%" + queryText + "%";
            pstmt.setString(1, pattern);
            pstmt.setString(2, pattern);
            pstmt.setString(3, pattern);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                students.add(createStudentFromResultSet(rs));
            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            logger.error("Error in global search for: {}", queryText, e);
        }
        return students;
    }

    public static ObservableList<Student> searchStudents(String name, String phone, String status, String groupName) {
        ObservableList<Student> students = FXCollections.observableArrayList();
        try {
            Connection conn = DatabaseManager.getConnection();
            StringBuilder query = new StringBuilder("SELECT DISTINCT s.* FROM students s ");
            if (groupName != null && !groupName.equals("Hammasi")) {
                query.append("INNER JOIN student_groups sg ON s.id = sg.student_id ");
                query.append("INNER JOIN groups g ON sg.group_id = g.id ");
            }
            query.append("WHERE 1=1 ");

            if (name != null && !name.isEmpty()) {
                query.append("AND (s.first_name LIKE ? OR s.last_name LIKE ?) ");
            }
            if (phone != null && !phone.isEmpty()) {
                query.append("AND s.phone LIKE ? ");
            }
            if (status != null && !status.equals("Hammasi")) {
                query.append("AND s.is_active = ? ");
            }
            if (groupName != null && !groupName.equals("Hammasi")) {
                query.append("AND g.name = ? ");
            }

            query.append("ORDER BY s.last_name, s.first_name");

            PreparedStatement pstmt = conn.prepareStatement(query.toString());
            int paramIndex = 1;

            if (name != null && !name.isEmpty()) {
                pstmt.setString(paramIndex++, "%" + name + "%");
                pstmt.setString(paramIndex++, "%" + name + "%");
            }
            if (phone != null && !phone.isEmpty()) {
                pstmt.setString(paramIndex++, "%" + phone + "%");
            }
            if (status != null && !status.equals("Hammasi")) {
                pstmt.setInt(paramIndex++, status.equals("Faol") ? 1 : 0);
            }
            if (groupName != null && !groupName.equals("Hammasi")) {
                pstmt.setString(paramIndex++, groupName);
            }

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                students.add(createStudentFromResultSet(rs));
            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            logger.error("Error searching students with filters - name: {}, phone: {}, status: {}, group: {}", 
                    name, phone, status, groupName, e);
        }
        return students;
    }

    private static Student createStudentFromResultSet(ResultSet rs) throws SQLException {
        return new Student(
                rs.getInt("id"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("phone"),
                rs.getString("telegram_username"),
                rs.getString("parent_name"),
                rs.getString("parent_phone"),
                rs.getString("parent_telegram"),
                rs.getInt("is_active") == 1
        );
    }
}
