package org.algo.mentor.controllers;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.algo.mentor.core.NavigableController;
import org.algo.mentor.core.NavigationController;
import org.algo.mentor.models.Group;
import org.algo.mentor.models.Student;
import org.algo.mentor.services.GroupService;
import org.algo.mentor.services.ReportService;
import org.algo.mentor.services.StudentService;

import java.util.List;
import java.util.stream.Collectors;

public class ReportsController implements NavigableController {

    @FXML private Label totalStudentsLabel;
    @FXML private Label totalGroupsLabel;
    @FXML private Label avgAttendanceLabel;

    @FXML private TableView<ReportService.GroupStat> groupStatsTable;
    @FXML private TableColumn<ReportService.GroupStat, String> groupNameCol;
    @FXML private TableColumn<ReportService.GroupStat, Integer> groupStudentCountCol;
    @FXML private TableColumn<ReportService.GroupStat, Double> groupAvgAttCol;
    @FXML private TableColumn<ReportService.GroupStat, Double> groupAvgScoreCol;
    @FXML private PieChart groupDistributionChart;
    @FXML private BarChart<String, Number> groupComparisonChart;

    @FXML private ComboBox<Group> groupFilterCombo;
    @FXML private TableView<ReportService.StudentStat> studentStatsTable;
    @FXML private TableColumn<ReportService.StudentStat, Integer> studentRankCol;
    @FXML private TableColumn<ReportService.StudentStat, String> studentNameCol;
    @FXML private TableColumn<ReportService.StudentStat, Double> studentAttRateCol;
    @FXML private TableColumn<ReportService.StudentStat, Integer> studentMissedCol;
    @FXML private TableColumn<ReportService.StudentStat, Double> studentAvgScoreCol;

    @FXML private ComboBox<Student> studentFilterCombo;
    @FXML private Label individualRankLabel;
    @FXML private Label individualAvgScoreLabel;
    @FXML private Label individualAttRateLabel;
    @FXML private TableView<ReportService.AttendanceDetail> individualAttTable;
    @FXML private TableColumn<ReportService.AttendanceDetail, String> attDateCol;
    @FXML private TableColumn<ReportService.AttendanceDetail, String> attStatusCol;
    @FXML private TableColumn<ReportService.AttendanceDetail, Double> attScoreCol;
    @FXML private PieChart individualAttendancePie;
    @FXML private LineChart<String, Number> performanceChart;

    private NavigationController navigationController;

    @FXML
    public void initialize() {
        setupGroupStatsTable();
        setupStudentStatsTable();
        setupIndividualStatsTable();
        
        loadSummary();
        loadGroupStats();
        loadFilterCombos();
        
        groupFilterCombo.getSelectionModel().selectedItemProperty().addListener((obs, old, val) -> {
            if (val != null) loadStudentStats(val.getId());
        });
        
        studentFilterCombo.getSelectionModel().selectedItemProperty().addListener((obs, old, val) -> {
            if (val != null) loadIndividualStats(val.getId());
        });
    }

    private void loadSummary() {
        ReportService.SummaryStat summary = ReportService.getSummaryStatistics();
        totalStudentsLabel.setText(String.valueOf(summary.totalStudents()));
        totalGroupsLabel.setText(String.valueOf(summary.totalGroups()));
        
        List<ReportService.GroupStat> groupStats = ReportService.getGroupStatistics();
        double avgAtt = groupStats.stream().mapToDouble(ReportService.GroupStat::avgAttendance).average().orElse(0.0);
        avgAttendanceLabel.setText(String.format("%.1f%%", avgAtt));
    }

    private void setupGroupStatsTable() {
        groupNameCol.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().name()));
        groupStudentCountCol.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().studentCount()));
        groupAvgAttCol.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().avgAttendance()));
        groupAvgScoreCol.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().avgScore()));
        
        // Format doubles
        groupAvgAttCol.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : String.format("%.1f%%", item));
            }
        });
        groupAvgScoreCol.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : String.format("%.1f", item));
            }
        });
    }

    private void setupStudentStatsTable() {
        studentRankCol.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().rank()));
        studentNameCol.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().fullName()));
        studentAttRateCol.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().attendanceRate()));
        studentMissedCol.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().missedLessons()));
        studentAvgScoreCol.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().avgScore()));

        studentAttRateCol.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : String.format("%.1f%%", item));
            }
        });
        studentAvgScoreCol.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : String.format("%.1f", item));
            }
        });
    }

    private void setupIndividualStatsTable() {
        attDateCol.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().date()));
        attStatusCol.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().present() ? "Kelgan" : "Kelmagan"));
        attScoreCol.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().score()));
        
        attStatusCol.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.equals("Kelgan")) setStyle("-fx-text-fill: #38a169; -fx-font-weight: bold;");
                    else setStyle("-fx-text-fill: #e53e3e; -fx-font-weight: bold;");
                }
            }
        });
    }

    private void loadGroupStats() {
        List<ReportService.GroupStat> stats = ReportService.getGroupStatistics();
        groupStatsTable.setItems(FXCollections.observableArrayList(stats));
        
        // Group Distribution Chart
        groupDistributionChart.getData().clear();
        for (ReportService.GroupStat stat : stats) {
            groupDistributionChart.getData().add(new PieChart.Data(stat.name(), stat.studentCount()));
        }
        
        // Group Comparison Chart
        groupComparisonChart.getData().clear();
        XYChart.Series<String, Number> attSeries = new XYChart.Series<>();
        attSeries.setName("Davomat (%)");
        
        XYChart.Series<String, Number> scoreSeries = new XYChart.Series<>();
        scoreSeries.setName("O'zlashtirish");
        
        for (ReportService.GroupStat stat : stats) {
            attSeries.getData().add(new XYChart.Data<>(stat.name(), stat.avgAttendance()));
            scoreSeries.getData().add(new XYChart.Data<>(stat.name(), stat.avgScore()));
        }
        
        groupComparisonChart.getData().addAll(attSeries, scoreSeries);
    }

    private void loadFilterCombos() {
        groupFilterCombo.setItems(GroupService.searchGroups(""));
        studentFilterCombo.setItems(StudentService.searchStudentsGlobal(""));
        
        // Custom string converters for combos
        groupFilterCombo.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Group item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item.getName());
            }
        });
        groupFilterCombo.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Group item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item.getName());
            }
        });
        
        studentFilterCombo.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Student item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item.getFirstName() + " " + item.getLastName());
            }
        });
        studentFilterCombo.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Student item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item.getFirstName() + " " + item.getLastName());
            }
        });
    }

    private void loadStudentStats(int groupId) {
        studentStatsTable.setItems(FXCollections.observableArrayList(ReportService.getStudentStatistics(groupId)));
    }

    private void loadIndividualStats(int studentId) {
        // We need groupId for individual stats in ReportService, but let's assume we want it across all groups or just find the student's group
        // For simplicity, let's find the first group this student belongs to
        ObservableList<Group> groups = GroupService.searchGroups("");
        int groupId = -1;
        // In a real app we'd get the student's current group.
        // Let's modify ReportService to not strictly require groupId if we want overall stats, or just use 0/default.
        // Actually, let's just use the first group for now or modify the UI to select group then student.
        
        // For now, let's just fetch for a specific group if one is selected in Tab 2, or just the first group.
        Group selectedGroup = groupFilterCombo.getValue();
        if (selectedGroup != null) {
            groupId = selectedGroup.getId();
        } else if (!groups.isEmpty()) {
            groupId = groups.get(0).getId();
        }

        if (groupId != -1) {
            List<ReportService.AttendanceDetail> details = ReportService.getIndividualStudentAttendance(studentId, groupId);
            individualAttTable.setItems(FXCollections.observableArrayList(details));
            
            // Calculate summaries
            double avgScore = details.stream().mapToDouble(ReportService.AttendanceDetail::score).average().orElse(0.0);
            long presentCount = details.stream().filter(ReportService.AttendanceDetail::present).count();
            double attRate = details.isEmpty() ? 0 : (double) presentCount / details.size() * 100;
            
            individualAvgScoreLabel.setText(String.format("%.1f", avgScore));
            individualAttRateLabel.setText(String.format("%.0f%%", attRate));
            
            // Attendance Pie Chart
            individualAttendancePie.getData().clear();
            individualAttendancePie.getData().add(new PieChart.Data("Kelgan", presentCount));
            individualAttendancePie.getData().add(new PieChart.Data("Kelmagan", details.size() - presentCount));
            
            // Rank
            List<ReportService.StudentStat> studentStats = ReportService.getStudentStatistics(groupId);
            int rank = studentStats.stream()
                    .filter(s -> s.id() == studentId)
                    .map(ReportService.StudentStat::rank)
                    .findFirst()
                    .orElse(0);
            individualRankLabel.setText(String.valueOf(rank));
            
            // Chart
            performanceChart.getData().clear();
            
            XYChart.Series<String, Number> studentSeries = new XYChart.Series<>();
            studentSeries.setName("O'quvchi");
            
            XYChart.Series<String, Number> groupSeries = new XYChart.Series<>();
            groupSeries.setName("Guruh o'rtachasi");

            // Take last 10 lessons for the chart
            int limit = Math.min(details.size(), 10);
            
            // Get group statistics for the same dates
            List<ReportService.LessonStat> groupLessonStats = ReportService.getGroupLessonStatistics(groupId);

            for (int i = limit - 1; i >= 0; i--) {
                ReportService.AttendanceDetail d = details.get(i);
                String dateLabel = d.date().substring(5, 10);
                studentSeries.getData().add(new XYChart.Data<>(dateLabel, d.score()));
                
                // Find group average for this date
                double groupAvg = groupLessonStats.stream()
                        .filter(gs -> gs.date().equals(d.date()))
                        .findFirst()
                        .map(ReportService.LessonStat::avgScore)
                        .orElse(0.0);
                groupSeries.getData().add(new XYChart.Data<>(dateLabel, groupAvg));
            }
            
            performanceChart.getData().addAll(studentSeries, groupSeries);
        }
    }

    @Override
    public void initialize(NavigationController navigationController) {
        this.navigationController = navigationController;
    }
}
