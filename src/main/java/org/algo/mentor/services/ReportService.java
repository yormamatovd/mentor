package org.algo.mentor.services;

import org.algo.mentor.config.DatabaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportService {

    private static final Logger log = LoggerFactory.getLogger(ReportService.class);

    public record GroupStat(int id, String name, int studentCount, double avgAttendance, double avgScore) {}
    public record StudentStat(int id, String fullName, double attendanceRate, double avgScore, int rank, int missedLessons) {}
    public record AttendanceDetail(String date, boolean present, double score, String scoreBreakdown) {}
    public record LessonStat(String date, double avgScore) {}
    public record SummaryStat(int totalStudents, int totalGroups, int lessonsToday, double avgAttendance) {}
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
        double avgAtt = 0;
        
        try (Connection conn = DatabaseManager.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM students");
                if (rs.next()) students = rs.getInt(1);
                
                rs = stmt.executeQuery("SELECT COUNT(*) FROM groups");
                if (rs.next()) groups = rs.getInt(1);
                
                rs = stmt.executeQuery("SELECT COALESCE(AVG(CAST(present AS DOUBLE)) * 100, 0) FROM attendance");
                if (rs.next()) avgAtt = rs.getDouble(1);
            }
            
            try (PreparedStatement pstmt = conn.prepareStatement("SELECT COUNT(*) FROM schedules WHERE day_of_week = ?")) {
                pstmt.setInt(1, java.time.LocalDate.now().getDayOfWeek().getValue());
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) lessons = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new SummaryStat(students, groups, lessons, avgAtt);
    }

    // ------------------------------------------------------------------
    // O'zlashtirish foizi
    //
    // Mijoz talabi: har bir vazifa (test / uy vazifa / savol-javob) o'z foizi
    // bilan hisoblanadi va shu foizlarning ODDIY O'RTACHASI olinadi. Har bir
    // vazifa teng vaznga ega: 25 ballik test ham, 10 ballik uy vazifa ham bir xil.
    // Masalan test 19/25 (76%) + uy vazifa 0/10 (0%) => 38%, 54,3% EMAS.
    // ------------------------------------------------------------------

    /** Bitta vazifa natijasi (test, uy vazifa yoki savol-javob). */
    public record TaskResult(int studentId, String lessonDate, double earned, double total) {}

    /**
     * Guruhdagi barcha o'quvchilarning barcha vazifalari.
     * Faqat hozir guruh a'zosi bo'lgan o'quvchilar kiradi.
     * Ro'yxat davomat yozuvi bor darslardan tuziladi; o'quvchining natijasi
     * kiritilmagan bo'lsa 0 ball deb olinadi (getDetailedLessonScores bilan bir xil mantiq).
     */
    public static List<TaskResult> getGroupTaskResults(int groupId) {
        List<TaskResult> tasks = new ArrayList<>();
        String query =
                "SELECT a.student_id, l.lesson_date, COALESCE(tr.total_score, 0) AS earned, ts.total_questions AS total " +
                "FROM attendance a JOIN lessons l ON a.lesson_id = l.id " +
                "JOIN student_groups sg ON sg.student_id = a.student_id AND sg.group_id = l.group_id " +
                "JOIN test_sessions ts ON ts.lesson_id = l.id " +
                "LEFT JOIN test_results tr ON tr.test_session_id = ts.id AND tr.student_id = a.student_id " +
                "WHERE l.group_id = ? " +
                "UNION ALL " +
                "SELECT a.student_id, l.lesson_date, COALESCE(h.score, 0), l.homework_total_score " +
                "FROM attendance a JOIN lessons l ON a.lesson_id = l.id " +
                "JOIN student_groups sg ON sg.student_id = a.student_id AND sg.group_id = l.group_id " +
                "LEFT JOIN homeworks h ON h.lesson_id = l.id AND h.student_id = a.student_id " +
                "WHERE l.group_id = ? AND l.homework_total_score > 0 " +
                "UNION ALL " +
                "SELECT a.student_id, l.lesson_date, COALESCE(qr.total_score, 0), qs.total_questions " +
                "FROM attendance a JOIN lessons l ON a.lesson_id = l.id " +
                "JOIN student_groups sg ON sg.student_id = a.student_id AND sg.group_id = l.group_id " +
                "JOIN question_sessions qs ON qs.lesson_id = l.id " +
                "LEFT JOIN question_results qr ON qr.question_session_id = qs.id AND qr.student_id = a.student_id " +
                "WHERE l.group_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, groupId);
            pstmt.setInt(2, groupId);
            pstmt.setInt(3, groupId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    tasks.add(new TaskResult(
                            rs.getInt("student_id"),
                            rs.getString("lesson_date"),
                            rs.getDouble("earned"),
                            rs.getDouble("total")
                    ));
                }
            }
        } catch (SQLException e) {
            log.error("Guruh vazifalarini olishda xatolik (group={})", groupId, e);
        }
        return tasks;
    }

    /** Foizlar ro'yxatining oddiy o'rtachasi; bo'sh bo'lsa 0. */
    public static double averageOfPercents(List<Double> percents) {
        if (percents == null || percents.isEmpty()) return 0;
        return percents.stream().mapToDouble(Double::doubleValue).average().orElse(0);
    }

    /** Shaxsiy statistika uchun: darslar bo'yicha batafsil natijalardan o'rtacha foiz. */
    public static double averageTaskPercent(List<DetailedLessonScore> details) {
        List<Double> percents = new ArrayList<>();
        for (DetailedLessonScore d : details) {
            for (TestScore t : d.tests()) {
                if (t.total() > 0) percents.add(t.score() / t.total() * 100);
            }
            for (HomeworkScore h : d.homeworks()) {
                if (h.total() > 0) percents.add(h.score() / h.total() * 100);
            }
            for (QuestionScore q : d.questions()) {
                if (q.total() > 0) percents.add(q.score() / q.total() * 100);
            }
        }
        return averageOfPercents(percents);
    }

    /**
     * Har bir o'quvchi uchun vazifalar foizining o'rtachasi.
     * Faqat kamida bitta vazifasi bo'lgan o'quvchilar kiradi. from/to null bo'lsa sana filtri yo'q.
     */
    public static Map<Integer, Double> averageTaskPercentByStudent(List<TaskResult> tasks, LocalDate from, LocalDate to) {
        Map<Integer, List<Double>> percents = new HashMap<>();
        for (TaskResult t : tasks) {
            if (t.total() <= 0) continue;
            if (!isLessonInRange(t.lessonDate(), from, to)) continue;
            percents.computeIfAbsent(t.studentId(), k -> new ArrayList<>()).add(t.earned() / t.total() * 100);
        }
        Map<Integer, Double> result = new HashMap<>();
        percents.forEach((id, list) -> result.put(id, averageOfPercents(list)));
        return result;
    }

    private static boolean isLessonInRange(String lessonDate, LocalDate from, LocalDate to) {
        if (from == null && to == null) return true;
        if (lessonDate == null || lessonDate.length() < 10) return false;
        try {
            LocalDate d = LocalDate.parse(lessonDate.substring(0, 10));
            return (from == null || !d.isBefore(from)) && (to == null || !d.isAfter(to));
        } catch (Exception e) {
            return false;
        }
    }

    public static List<GroupStat> getGroupStatistics() {
        record GroupBasic(int id, String name, int studentCount, double avgAtt) {}
        List<GroupBasic> basics = new ArrayList<>();
        String query = "SELECT g.id, g.name, " +
                "(SELECT COUNT(*) FROM student_groups WHERE group_id = g.id) as student_count, " +
                "COALESCE((SELECT AVG(CAST(present AS DOUBLE)) * 100 FROM attendance a JOIN lessons l ON a.lesson_id = l.id WHERE l.group_id = g.id), 0) as avg_att " +
                "FROM groups g";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                basics.add(new GroupBasic(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("student_count"),
                        rs.getDouble("avg_att")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Guruh o'zlashtirishi = guruhdagi o'quvchilar o'rtacha foizlarining o'rtachasi
        List<GroupStat> stats = new ArrayList<>();
        for (GroupBasic g : basics) {
            Map<Integer, Double> byStudent = averageTaskPercentByStudent(getGroupTaskResults(g.id()), null, null);
            double avgScore = averageOfPercents(new ArrayList<>(byStudent.values()));
            stats.add(new GroupStat(g.id(), g.name(), g.studentCount(), g.avgAtt(), avgScore));
        }
        return stats;
    }

    public static List<StudentStat> getStudentStatistics(int groupId) {
        return getStudentStatistics(groupId, null, null);
    }

    public static List<StudentStat> getStudentStatistics(int groupId, LocalDate fromDate, LocalDate toDate) {
        record StudentBasic(int id, String fullName, double attRate, int missed) {}
        List<StudentBasic> basics = new ArrayList<>();

        String fromDateStr = fromDate == null ? "0000-01-01" : fromDate.toString();
        String toDateStr = toDate == null ? "9999-12-31" : toDate.toString();

        String query = "SELECT s.id, s.first_name || ' ' || s.last_name as full_name, " +
                "COALESCE((SELECT AVG(CAST(present AS DOUBLE)) * 100 FROM attendance a JOIN lessons l ON a.lesson_id = l.id WHERE a.student_id = s.id AND l.group_id = ? AND DATE(l.lesson_date) >= ? AND DATE(l.lesson_date) <= ?), 0) as att_rate, " +
                "COALESCE((SELECT COUNT(*) FROM attendance a JOIN lessons l ON a.lesson_id = l.id WHERE a.student_id = s.id AND l.group_id = ? AND DATE(l.lesson_date) >= ? AND DATE(l.lesson_date) <= ? AND a.present = 0), 0) as missed_lessons " +
                "FROM students s " +
                "JOIN student_groups sg ON s.id = sg.student_id " +
                "WHERE sg.group_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, groupId);
            pstmt.setString(2, fromDateStr);
            pstmt.setString(3, toDateStr);
            pstmt.setInt(4, groupId);
            pstmt.setString(5, fromDateStr);
            pstmt.setString(6, toDateStr);
            pstmt.setInt(7, groupId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    basics.add(new StudentBasic(
                            rs.getInt("id"),
                            rs.getString("full_name"),
                            rs.getDouble("att_rate"),
                            rs.getInt("missed_lessons")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Map<Integer, Double> avgByStudent = averageTaskPercentByStudent(getGroupTaskResults(groupId), fromDate, toDate);

        List<StudentStat> unranked = new ArrayList<>();
        for (StudentBasic b : basics) {
            double avgScore = avgByStudent.getOrDefault(b.id(), 0.0);
            unranked.add(new StudentStat(b.id(), b.fullName(), b.attRate(), avgScore, 0, b.missed()));
        }
        unranked.sort(Comparator.comparingDouble(StudentStat::avgScore).reversed());

        List<StudentStat> stats = new ArrayList<>();
        int rank = 1;
        for (StudentStat s : unranked) {
            stats.add(new StudentStat(s.id(), s.fullName(), s.attendanceRate(), s.avgScore(), rank++, s.missedLessons()));
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
        record RiskBasic(int id, String fullName, int groupId, String groupName, double attendanceRate) {}
        List<RiskBasic> basics = new ArrayList<>();
        String query = "SELECT s.id, s.first_name || ' ' || s.last_name as full_name, g.id as group_id, g.name as group_name, " +
                "COALESCE((SELECT AVG(CAST(present AS DOUBLE)) * 100 FROM attendance a JOIN lessons l ON a.lesson_id = l.id WHERE a.student_id = s.id), 0) as attendance_rate " +
                "FROM students s " +
                "JOIN student_groups sg ON s.id = sg.student_id " +
                "JOIN groups g ON sg.group_id = g.id " +
                "WHERE (SELECT COUNT(*) FROM lessons WHERE group_id = g.id) > 0 " +
                "GROUP BY s.id, g.id";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                basics.add(new RiskBasic(
                        rs.getInt("id"),
                        rs.getString("full_name"),
                        rs.getInt("group_id"),
                        rs.getString("group_name"),
                        rs.getDouble("attendance_rate")
                ));
            }
        } catch (SQLException e) {
            log.error(e.getSQLState());
            log.error(e.getMessage());
        }

        // O'zlashtirish har bir guruh uchun bir marta hisoblanadi
        Map<Integer, Map<Integer, Double>> perfByGroup = new HashMap<>();
        List<RiskStudent> students = new ArrayList<>();
        for (RiskBasic b : basics) {
            Map<Integer, Double> byStudent = perfByGroup.computeIfAbsent(b.groupId(),
                    gid -> averageTaskPercentByStudent(getGroupTaskResults(gid), null, null));
            double performance = byStudent.getOrDefault(b.id(), 0.0);
            if (b.attendanceRate() < 75 || performance < 50) {
                students.add(new RiskStudent(b.id(), b.fullName(), b.attendanceRate(), performance, b.groupName()));
            }
        }
        students.sort(Comparator.comparingDouble(RiskStudent::attendanceRate)
                .thenComparingDouble(RiskStudent::performanceRate));
        if (students.size() > 10) {
            students = new ArrayList<>(students.subList(0, 10));
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
            log.error(e.getSQLState());
            log.error(e.getMessage());
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
            log.error(e.getSQLState());
            log.error(e.getMessage());
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
            log.error(e.getSQLState());
            log.error(e.getMessage());        }
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
            log.error(e.getSQLState());
            log.error(e.getMessage());        }
        return scores;
    }
}
