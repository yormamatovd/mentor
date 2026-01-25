package org.algo.mentor.services;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import org.algo.mentor.config.DatabaseManager;
import org.algo.mentor.models.Payment;

public class PaymentService {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static ObservableList<Payment> getPaymentsByStudentId(int studentId) {
        ObservableList<Payment> payments = FXCollections.observableArrayList();
        try {
            Connection conn = DatabaseManager.getConnection();
            String query = "SELECT * FROM payments WHERE student_id = ? ORDER BY created_date DESC";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, studentId);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Payment payment = createPaymentFromResultSet(rs);
                payments.add(payment);
            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return payments;
    }

    public static ObservableList<Payment> getPaymentsByStudentIdAndDateRange(int studentId, String fromDate, String toDate) {
        ObservableList<Payment> payments = FXCollections.observableArrayList();
        try {
            Connection conn = DatabaseManager.getConnection();
            String query = "SELECT * FROM payments WHERE student_id = ? " +
                    "AND created_date >= ? AND created_date <= ? " +
                    "ORDER BY created_date DESC";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, studentId);
            pstmt.setString(2, fromDate);
            pstmt.setString(3, toDate);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Payment payment = createPaymentFromResultSet(rs);
                payments.add(payment);
            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return payments;
    }

    public static ObservableList<Payment> getPaymentsByStudentIdAndMonth(int studentId, YearMonth yearMonth) {
        String fromDate = yearMonth.atDay(1).format(DATE_FORMAT);
        String toDate = yearMonth.atEndOfMonth().format(DATE_FORMAT);
        return getPaymentsByStudentIdAndDateRange(studentId, fromDate, toDate);
    }

    public static void addPayment(int studentId, double amount, String paymentFromDate, String paymentToDate) {
        try {
            Connection conn = DatabaseManager.getConnection();
            String createdDate = LocalDate.now().format(DATE_FORMAT);
            String query = "INSERT INTO payments (student_id, amount, payment_from_date, payment_to_date, created_date) " +
                    "VALUES (?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, studentId);
            pstmt.setDouble(2, amount);
            pstmt.setString(3, paymentFromDate);
            pstmt.setString(4, paymentToDate);
            pstmt.setString(5, createdDate);

            pstmt.executeUpdate();
            pstmt.close();

            StudentService.updateStudentPaymentStatus();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updatePayment(int paymentId, double amount, String paymentFromDate, String paymentToDate) {
        try {
            Connection conn = DatabaseManager.getConnection();
            String query = "UPDATE payments SET amount = ?, payment_from_date = ?, payment_to_date = ? WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setDouble(1, amount);
            pstmt.setString(2, paymentFromDate);
            pstmt.setString(3, paymentToDate);
            pstmt.setInt(4, paymentId);

            pstmt.executeUpdate();
            pstmt.close();

            Payment payment = getPaymentById(paymentId);
            if (payment != null) {
                StudentService.updateStudentPaymentStatus();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void deletePayment(int paymentId) {
        try {
            Connection conn = DatabaseManager.getConnection();
            String query = "DELETE FROM payments WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, paymentId);

            pstmt.executeUpdate();
            pstmt.close();

            StudentService.updateStudentPaymentStatus();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Payment getPaymentById(int paymentId) {
        try {
            Connection conn = DatabaseManager.getConnection();
            String query = "SELECT * FROM payments WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, paymentId);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return createPaymentFromResultSet(rs);
            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Payment createPaymentFromResultSet(ResultSet rs) throws SQLException {
        return new Payment(
                rs.getInt("id"),
                rs.getInt("student_id"),
                rs.getDouble("amount"),
                rs.getString("payment_from_date"),
                rs.getString("payment_to_date"),
                rs.getString("created_date")
        );
    }
}
