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

    public static boolean isMonthlyPaymentExists(int studentId, int year, int month, int paymentDay) {
        try {
            Connection conn = DatabaseManager.getConnection();
            String query = "SELECT COUNT(*) FROM monthly_payments WHERE student_id=? AND year=? AND month=? AND payment_day=?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, studentId);
            pstmt.setInt(2, year);
            pstmt.setInt(3, month);
            pstmt.setInt(4, paymentDay);
            ResultSet rs = pstmt.executeQuery();
            boolean exists = rs.next() && rs.getInt(1) > 0;
            rs.close();
            pstmt.close();
            return exists;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void toggleMonthlyPayment(int studentId, int year, int month, int paymentDay) {
        try {
            Connection conn = DatabaseManager.getConnection();
            if (isMonthlyPaymentExists(studentId, year, month, paymentDay)) {
                String query = "DELETE FROM monthly_payments WHERE student_id=? AND year=? AND month=? AND payment_day=?";
                PreparedStatement pstmt = conn.prepareStatement(query);
                pstmt.setInt(1, studentId);
                pstmt.setInt(2, year);
                pstmt.setInt(3, month);
                pstmt.setInt(4, paymentDay);
                pstmt.executeUpdate();
                pstmt.close();
            } else {
                String query = "INSERT INTO monthly_payments (student_id, year, month, payment_day) VALUES (?,?,?,?)";
                PreparedStatement pstmt = conn.prepareStatement(query);
                pstmt.setInt(1, studentId);
                pstmt.setInt(2, year);
                pstmt.setInt(3, month);
                pstmt.setInt(4, paymentDay);
                pstmt.executeUpdate();
                pstmt.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static int countMonthlyPaymentsForStudent(int studentId) {
        try {
            Connection conn = DatabaseManager.getConnection();
            String query = "SELECT COUNT(*) FROM monthly_payments WHERE student_id=?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, studentId);
            ResultSet rs = pstmt.executeQuery();
            int count = rs.next() ? rs.getInt(1) : 0;
            rs.close();
            pstmt.close();
            return count;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static java.util.Set<String> getMonthlyPaymentKeysForStudents(java.util.List<Integer> studentIds) {
        java.util.Set<String> keys = new java.util.HashSet<>();
        if (studentIds == null || studentIds.isEmpty()) return keys;
        try {
            Connection conn = DatabaseManager.getConnection();
            StringBuilder sb = new StringBuilder("SELECT student_id, year, month, payment_day FROM monthly_payments WHERE student_id IN (");
            for (int i = 0; i < studentIds.size(); i++) {
                sb.append(i == 0 ? "?" : ",?");
            }
            sb.append(")");
            PreparedStatement pstmt = conn.prepareStatement(sb.toString());
            for (int i = 0; i < studentIds.size(); i++) {
                pstmt.setInt(i + 1, studentIds.get(i));
            }
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                keys.add(rs.getInt("student_id") + "_" + rs.getInt("year") + "_" + rs.getInt("month") + "_" + rs.getInt("payment_day"));
            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return keys;
    }
}
