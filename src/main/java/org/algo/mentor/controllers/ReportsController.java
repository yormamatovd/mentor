package org.algo.mentor.controllers;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.WritableImage;
import javafx.scene.SnapshotParameters;
import javafx.stage.FileChooser;
import org.algo.mentor.core.NavigableController;
import org.algo.mentor.core.NavigationController;
import org.algo.mentor.models.Group;
import org.algo.mentor.models.Student;
import org.algo.mentor.services.GroupService;
import org.algo.mentor.services.PdfExportService;
import org.algo.mentor.services.ReportService;
import org.algo.mentor.services.StudentService;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
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
    @FXML private TableView<ReportService.LessonScoreRow> individualAttTable;
    @FXML private TableColumn<ReportService.LessonScoreRow, String> attDateCol;
    @FXML private TableColumn<ReportService.LessonScoreRow, String> attStatusCol;
    @FXML private TableColumn<ReportService.LessonScoreRow, String> scoreTypeCol;
    @FXML private TableColumn<ReportService.LessonScoreRow, Double> attScoreCol;
    @FXML private PieChart individualAttendancePie;
    @FXML private LineChart<String, Number> performanceChart;
    @FXML private Button exportPersonalPdfBtn;
    @FXML private Label personalExportStatusLabel;

    private NavigationController navigationController;

    @FXML
    public void initialize() {
        setupGroupStatsTable();
        setupStudentStatsTable();
        setupIndividualStatsTable();
        
        loadSummary();
        loadGroupStats();
        loadFilterCombos();
        
        // Disable chart animations and rotate X-axis labels
        performanceChart.setAnimated(false);
        groupComparisonChart.setAnimated(false);
        groupDistributionChart.setAnimated(false);
        individualAttendancePie.setAnimated(false);

        if (performanceChart.getXAxis() instanceof CategoryAxis xAxis) {
            xAxis.setTickLabelRotation(-45);
        }
        if (groupComparisonChart.getXAxis() instanceof CategoryAxis xAxis) {
            xAxis.setTickLabelRotation(-45);
        }

        groupFilterCombo.getSelectionModel().selectedItemProperty().addListener((obs, old, val) -> {
            if (val != null) loadStudentStats(val.getId());
        });
        
        studentFilterCombo.getSelectionModel().selectedItemProperty().addListener((obs, old, val) -> {
            if (val != null) {
                loadIndividualStats(val.getId());
                exportPersonalPdfBtn.setDisable(false);
            } else {
                exportPersonalPdfBtn.setDisable(true);
            }
            clearStatus();
        });
    }

    private void clearStatus() {
        personalExportStatusLabel.setVisible(false);
        personalExportStatusLabel.setManaged(false);
        personalExportStatusLabel.setText("");
    }

    private void showStatus(String message, boolean isError) {
        personalExportStatusLabel.setText(message);
        personalExportStatusLabel.setVisible(true);
        personalExportStatusLabel.setManaged(true);
        if (isError) {
            personalExportStatusLabel.setStyle("-fx-background-color: #fff5f5; -fx-text-fill: #c53030; -fx-border-color: #feb2b2; -fx-border-radius: 5; -fx-background-radius: 5; -fx-padding: 8;");
        } else {
            personalExportStatusLabel.setStyle("-fx-background-color: #f0fff4; -fx-text-fill: #2f855a; -fx-border-color: #9ae6b4; -fx-border-radius: 5; -fx-background-radius: 5; -fx-padding: 8;");
        }
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
        attDateCol.setCellValueFactory(cd -> {
            String date = cd.getValue().date();
            if (date != null && date.length() >= 16) {
                String year = date.substring(0, 4);
                String month = date.substring(5, 7);
                String day = date.substring(8, 10);
                String time = date.substring(11, 16);
                return new ReadOnlyObjectWrapper<>(year + "." + month + "." + day + " " + time);
            } else if (date != null && date.length() >= 10) {
                return new ReadOnlyObjectWrapper<>(date.substring(0, 4) + "." + date.substring(5, 7) + "." + date.substring(8, 10));
            }
            return new ReadOnlyObjectWrapper<>(date);
        });
        
        attStatusCol.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().status()));
        scoreTypeCol.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().scoreType()));
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
        
        attScoreCol.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "-" : String.format("%.1f", item));
            }
        });
        
        individualAttTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(ReportService.LessonScoreRow item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("");
                } else {
                    setStyle(shouldHaveGrayBackground(getIndex()) ? "-fx-background-color: #e1e1e1;" : "");
                }
            }
            
            private boolean shouldHaveGrayBackground(int currentIndex) {
                if (currentIndex < 0 || getTableView() == null || getTableView().getItems() == null 
                    || currentIndex >= getTableView().getItems().size()) {
                    return false;
                }
                
                boolean isGray = false;
                for (int i = 0; i <= currentIndex; i++) {
                    if (i == 0 || !getTableView().getItems().get(i).date().equals(getTableView().getItems().get(i - 1).date())) {
                        isGray = !isGray;
                    }
                }
                return isGray;
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
            List<ReportService.DetailedLessonScore> allDetails = ReportService.getDetailedLessonScores(studentId, groupId);
            
            // Filter last 1 year
            LocalDate oneYearAgo = LocalDate.now().minusYears(1);
            List<ReportService.DetailedLessonScore> details = allDetails.stream()
                    .filter(d -> {
                        try {
                            return LocalDate.parse(d.date()).isAfter(oneYearAgo);
                        } catch (Exception e) {
                            return true;
                        }
                    })
                    .collect(Collectors.toList());

            // Get rows for table display
            List<ReportService.LessonScoreRow> allRows = ReportService.getLessonScoreRows(studentId, groupId);
            List<ReportService.LessonScoreRow> rows = allRows.stream()
                    .filter(r -> {
                        try {
                            return LocalDate.parse(r.date()).isAfter(oneYearAgo);
                        } catch (Exception e) {
                            return true;
                        }
                    })
                    .collect(Collectors.toList());
            
            individualAttTable.setItems(FXCollections.observableArrayList(rows));
            
            // Calculate summaries
            double avgScore = details.stream().mapToDouble(ReportService.DetailedLessonScore::totalScore).average().orElse(0.0);
            long presentCount = details.stream().filter(ReportService.DetailedLessonScore::present).count();
            double attRate = details.isEmpty() ? 0 : (double) presentCount / details.size() * 100;
            
            individualAvgScoreLabel.setText(String.format("%.1f", avgScore));
            individualAttRateLabel.setText(String.format("%.0f%%", attRate));
            
            // Attendance Pie Chart
            individualAttendancePie.getData().clear();
            long absentCount = details.size() - presentCount;
            individualAttendancePie.getData().add(new PieChart.Data("Kelgan (" + presentCount + ")", presentCount));
            individualAttendancePie.getData().add(new PieChart.Data("Kelmagan (" + absentCount + ")", absentCount));
            
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
                ReportService.DetailedLessonScore d = details.get(i);
                // Format date as dd.MM (from yyyy-MM-dd)
                String dateLabel = d.date().substring(8, 10) + "." + d.date().substring(5, 7);
                studentSeries.getData().add(new XYChart.Data<>(dateLabel, d.totalScore()));
                
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

    @FXML
    private void onExportPersonalPdfClick() {
        Student student = studentFilterCombo.getValue();
        if (student == null) return;

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Talaba hisobotini saqlash");
        fileChooser.setInitialFileName(student.getFirstName() + "_" + student.getLastName() + "_hisobot.pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

        File file = fileChooser.showSaveDialog(exportPersonalPdfBtn.getScene().getWindow());
        if (file != null) {
            try {
                // High-quality snapshots of charts
                SnapshotParameters params = new SnapshotParameters();
                params.setTransform(javafx.scene.transform.Transform.scale(2.0, 2.0));
                
                WritableImage attImage = individualAttendancePie.snapshot(params, null);
                WritableImage perfImage = performanceChart.snapshot(params, null);

                PdfExportService.exportStudentReport(
                        student,
                        individualAttTable.getItems(),
                        individualRankLabel.getText(),
                        individualAvgScoreLabel.getText(),
                        individualAttRateLabel.getText(),
                        attImage,
                        perfImage,
                        file
                );

                showStatus("Talaba hisoboti PDF formatida saqlandi.", false);
            } catch (IOException e) {
                e.printStackTrace();
                showStatus("PDF eksport qilishda xatolik: " + e.getMessage(), true);
            }
        }
    }

    @Override
    public void initialize(NavigationController navigationController) {
        this.navigationController = navigationController;
    }
}
