package org.algo.mentor.controllers;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class ReportsController implements NavigableController {
    private static final Logger log = LoggerFactory.getLogger(ReportsController.class);

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
    @FXML private DatePicker studentActivityFromPicker;
    @FXML private DatePicker studentActivityToPicker;
    @FXML private TableView<ReportService.StudentStat> studentStatsTable;
    @FXML private TableColumn<ReportService.StudentStat, Integer> studentRankCol;
    @FXML private TableColumn<ReportService.StudentStat, String> studentNameCol;
    @FXML private TableColumn<ReportService.StudentStat, Double> studentAttRateCol;
    @FXML private TableColumn<ReportService.StudentStat, Integer> studentMissedCol;
    @FXML private TableColumn<ReportService.StudentStat, Double> studentAvgScoreCol;

    @FXML private TextField studentSearchField;
    @FXML private ListView<Student> studentSearchResultsList;
    @FXML private DatePicker dateFromPicker;
    @FXML private DatePicker dateToPicker;
    @FXML private Label individualRankLabel;
    @FXML private Label individualAvgScoreLabel;
    @FXML private Label individualAttRateLabel;
    @FXML private TableView<ReportService.LessonScoreRow> individualAttTable;
    @FXML private TableColumn<ReportService.LessonScoreRow, String> attDateCol;
    @FXML private TableColumn<ReportService.LessonScoreRow, String> attStatusCol;
    @FXML private TableColumn<ReportService.LessonScoreRow, String> scoreTypeCol;
    @FXML private TableColumn<ReportService.LessonScoreRow, Double> attTotalCol;
    @FXML private TableColumn<ReportService.LessonScoreRow, Double> attResultCol;
    @FXML private PieChart individualAttendancePie;
    @FXML private LineChart<String, Number> performanceChart;
    @FXML private Button exportPersonalPdfBtn;
    @FXML private Label personalExportStatusLabel;

    private NavigationController navigationController;
    private Student selectedStudent;

    @FXML
    public void initialize() {
        setupGroupStatsTable();
        setupStudentStatsTable();
        setupIndividualStatsTable();
        setupDatePickers();
        setupStudentActivityDatePickers();
        
        loadSummary();
        loadGroupStats();
        loadFilterCombos();
        
        // Disable chart animations and rotate X-axis labels
        performanceChart.setAnimated(false);
        groupComparisonChart.setAnimated(false);
        groupDistributionChart.setAnimated(false);
        individualAttendancePie.setAnimated(false);
        individualAttendancePie.setLabelsVisible(false);

        if (performanceChart.getXAxis() instanceof CategoryAxis xAxis) {
            xAxis.setTickLabelRotation(-45);
        }
        if (groupComparisonChart.getXAxis() instanceof CategoryAxis xAxis) {
            xAxis.setTickLabelRotation(-45);
        }

        groupFilterCombo.getSelectionModel().selectedItemProperty().addListener((obs, old, val) -> {
            if (val != null) loadStudentStats(val.getId());
        });
        
        studentActivityFromPicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            Group selectedGroup = groupFilterCombo.getValue();
            if (selectedGroup != null && newVal != null && !newVal.equals(oldVal)) {
                loadStudentStats(selectedGroup.getId());
            }
        });
        
        studentActivityToPicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            Group selectedGroup = groupFilterCombo.getValue();
            if (selectedGroup != null && newVal != null && !newVal.equals(oldVal)) {
                loadStudentStats(selectedGroup.getId());
            }
        });
        
        studentSearchResultsList.getSelectionModel().selectedItemProperty().addListener((obs, old, val) -> {
            if (val != null) {
                this.selectedStudent = val;
                loadIndividualStats(val.getId());
                exportPersonalPdfBtn.setDisable(false);
                studentSearchResultsList.setVisible(false);
                studentSearchResultsList.setManaged(false);
                studentSearchField.setText(val.getFirstName() + " " + val.getLastName());
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
        avgAttendanceLabel.setText(String.format("%.1f%%", summary.avgAttendance()));
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
                setText(empty ? null : String.format("%.1f%%", item));
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
                setText(empty ? null : String.format("%.1f%%", item));
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
        attTotalCol.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().totalValue()));
        attResultCol.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().score()));
        
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
        
        attTotalCol.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item <= 0) setText("-");
                else setText(String.format("%.1f", item));
            }
        });

        attResultCol.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText("-");
                else setText(String.format("%.1f", item));
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

    private void setupDatePickers() {
        System.out.println("========================================");
        System.out.println("SETUP DATE PICKERS CALLED!");
        System.out.println("========================================");
        
        if (dateFromPicker == null) {
            System.out.println("ERROR: dateFromPicker is NULL!");
            return;
        }
        if (dateToPicker == null) {
            System.out.println("ERROR: dateToPicker is NULL!");
            return;
        }
        
        System.out.println("Date pickers are NOT null, setting values...");
        
        dateToPicker.setValue(LocalDate.now());
        dateFromPicker.setValue(LocalDate.now().minusYears(1));
        
        System.out.println("Date pickers initialized: From=" + dateFromPicker.getValue() + ", To=" + dateToPicker.getValue());
        
        dateFromPicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            System.out.println(">>> Date From CHANGED: old=" + oldVal + ", new=" + newVal + ", student=" + (selectedStudent != null ? selectedStudent.getId() : "null"));
            if (selectedStudent != null && newVal != null && !newVal.equals(oldVal)) {
                System.out.println(">>> RELOADING STATS for date change");
                loadIndividualStats(selectedStudent.getId());
            } else {
                System.out.println(">>> NOT reloading: selectedStudent=" + (selectedStudent != null) + ", newVal=" + (newVal != null) + ", changed=" + (!newVal.equals(oldVal)));
            }
        });
        
        dateToPicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            System.out.println(">>> Date To CHANGED: old=" + oldVal + ", new=" + newVal + ", student=" + (selectedStudent != null ? selectedStudent.getId() : "null"));
            if (selectedStudent != null && newVal != null && !newVal.equals(oldVal)) {
                System.out.println(">>> RELOADING STATS for date change");
                loadIndividualStats(selectedStudent.getId());
            } else {
                System.out.println(">>> NOT reloading: selectedStudent=" + (selectedStudent != null) + ", newVal=" + (newVal != null) + ", changed=" + (!newVal.equals(oldVal)));
            }
        });
        
        System.out.println("Date picker listeners attached successfully!");
        System.out.println("========================================");
    }
    
    private void setupStudentActivityDatePickers() {
        studentActivityToPicker.setValue(LocalDate.now());
        studentActivityFromPicker.setValue(LocalDate.now().minusYears(1));
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
        
        studentSearchResultsList.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Student item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getFirstName() + " " + item.getLastName() + " (" + item.getPhone() + ")");
                }
            }
        });
    }

    private void loadStudentStats(int groupId) {
        LocalDate fromDate = studentActivityFromPicker.getValue();
        LocalDate toDate = studentActivityToPicker.getValue();
        
        if (fromDate == null) fromDate = LocalDate.now().minusYears(1);
        if (toDate == null) toDate = LocalDate.now();
        
        studentStatsTable.setItems(FXCollections.observableArrayList(
            ReportService.getStudentStatistics(groupId, fromDate, toDate)
        ));
    }

    private void loadIndividualStats(int studentId) {
        // Find group for student
        int groupId = -1;
        
        // 1. Try currently selected group in filter
        Group selectedGroup = groupFilterCombo.getValue();
        if (selectedGroup != null) {
            groupId = selectedGroup.getId();
        } 
        
        // 2. If no group selected, find student's actual groups
        if (groupId == -1) {
            ObservableList<Integer> studentGroups = StudentService.getGroupIdsByStudent(studentId);
            if (!studentGroups.isEmpty()) {
                groupId = studentGroups.get(0);
            }
        }
        
        // 3. Fallback to first available group if still not found
        if (groupId == -1) {
            ObservableList<Group> allGroups = GroupService.searchGroups("");
            if (!allGroups.isEmpty()) {
                groupId = allGroups.get(0).getId();
            }
        }

        if (groupId != -1) {
            System.out.println(">>> loadIndividualStats() called for student=" + studentId + ", group=" + groupId);
            
            List<ReportService.DetailedLessonScore> allDetails = ReportService.getDetailedLessonScores(studentId, groupId);
            System.out.println(">>> Total records from service: " + allDetails.size());
            
            LocalDate fromDate = dateFromPicker.getValue();
            LocalDate toDate = dateToPicker.getValue();
            
            System.out.println(">>> DatePicker values: From=" + fromDate + ", To=" + toDate);
            
            if (fromDate == null) fromDate = LocalDate.now().minusYears(1);
            if (toDate == null) toDate = LocalDate.now();
            
            final LocalDate finalFromDate = fromDate;
            final LocalDate finalToDate = toDate;
            
            System.out.println(">>> Filtering with date range: " + finalFromDate + " to " + finalToDate);
            
            List<ReportService.DetailedLessonScore> details = allDetails.stream()
                    .filter(d -> {
                        try {
                            String dateStr = d.date();
                            // Extract date part from datetime string (YYYY-MM-DD)
                            if (dateStr.length() >= 10) {
                                dateStr = dateStr.substring(0, 10);
                            }
                            LocalDate lessonDate = LocalDate.parse(dateStr);
                            boolean inRange = !lessonDate.isBefore(finalFromDate) && !lessonDate.isAfter(finalToDate);
                            if (!inRange) {
                                System.out.println(">>> FILTERED OUT: " + d.date() + " (not in range)");
                            }
                            return inRange;
                        } catch (Exception e) {
                            System.out.println(">>> ERROR parsing date: " + d.date() + " - " + e.getMessage());
                            return false; // Changed from true to false!
                        }
                    })
                    .collect(Collectors.toList());
            
            System.out.println(">>> After filtering: " + details.size() + " records");

            // Get rows for table display
            List<ReportService.LessonScoreRow> allRows = ReportService.getLessonScoreRows(studentId, groupId);
            System.out.println(">>> Total rows for table: " + allRows.size());
            
            List<ReportService.LessonScoreRow> rows = allRows.stream()
                    .filter(r -> {
                        try {
                            String dateStr = r.date();
                            // Extract date part from datetime string (YYYY-MM-DD)
                            if (dateStr.length() >= 10) {
                                dateStr = dateStr.substring(0, 10);
                            }
                            LocalDate rowDate = LocalDate.parse(dateStr);
                            boolean inRange = !rowDate.isBefore(finalFromDate) && !rowDate.isAfter(finalToDate);
                            return inRange;
                        } catch (Exception e) {
                            System.out.println(">>> ERROR parsing row date: " + r.date());
                            return false; // Changed from true to false!
                        }
                    })
                    .collect(Collectors.toList());
            
            System.out.println(">>> Rows after filtering for table: " + rows.size());
            System.out.println(">>> Setting " + rows.size() + " rows to table!");
            
            individualAttTable.setItems(FXCollections.observableArrayList(rows));
            
            // Calculate summaries
            double totalEarned = details.stream().mapToDouble(ReportService.DetailedLessonScore::totalScore).sum();
            double totalPossible = details.stream().mapToDouble(ReportService.DetailedLessonScore::totalValue).sum();
            double avgScore = totalPossible == 0 ? 0 : (totalEarned / totalPossible) * 100;
            
            long presentCount = details.stream().filter(ReportService.DetailedLessonScore::present).count();
            double attRate = details.isEmpty() ? 0 : (double) presentCount / details.size() * 100;
            
            individualAvgScoreLabel.setText(String.format("%.1f%%", avgScore));
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
            
            XYChart.Series<String, Number> totalSeries = new XYChart.Series<>();
            totalSeries.setName("Jami");

            // Take last 10 lessons for the chart
            int limit = Math.min(details.size(), 10);
            
            for (int i = limit - 1; i >= 0; i--) {
                ReportService.DetailedLessonScore d = details.get(i);
                // Format date as dd.MM HH:mm
                String dateLabel;
                if (d.date() != null && d.date().length() >= 16) {
                    dateLabel = d.date().substring(8, 10) + "." + d.date().substring(5, 7) + " " + d.date().substring(11, 16);
                } else if (d.date() != null && d.date().length() >= 10) {
                    dateLabel = d.date().substring(8, 10) + "." + d.date().substring(5, 7);
                } else {
                    dateLabel = d.date();
                }
                studentSeries.getData().add(new XYChart.Data<>(dateLabel, d.totalScore()));
                totalSeries.getData().add(new XYChart.Data<>(dateLabel, d.totalValue()));
            }
            
            performanceChart.getData().addAll(studentSeries, totalSeries);
        }
    }

    @FXML
    private void onSearchStudentClick() {
        String query = studentSearchField.getText().trim();
        if (query.isEmpty()) {
            studentSearchResultsList.setVisible(false);
            studentSearchResultsList.setManaged(false);
            return;
        }
        
        ObservableList<Student> results = StudentService.searchStudentsGlobal(query);
        studentSearchResultsList.setItems(results);
        studentSearchResultsList.setVisible(true);
        studentSearchResultsList.setManaged(true);
        
        if (results.isEmpty()) {
            showStatus("O'quvchi topilmadi", true);
        } else {
            clearStatus();
        }
    }

    @FXML
    private void onExportPersonalPdfClick() {
        if (selectedStudent == null) return;

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Talaba hisobotini saqlash");
        fileChooser.setInitialFileName(selectedStudent.getFirstName() + "_" + selectedStudent.getLastName() + "_hisobot.pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

        File file = fileChooser.showSaveDialog(exportPersonalPdfBtn.getScene().getWindow());
        if (file != null) {
            try {
                // High-quality snapshots of charts
                SnapshotParameters params = new SnapshotParameters();
                params.setTransform(javafx.scene.transform.Transform.scale(2.0, 2.0));
                
                WritableImage attImage = individualAttendancePie.snapshot(params, null);
                WritableImage perfImage = performanceChart.snapshot(params, null);

                LocalDate fromDate = dateFromPicker.getValue();
                LocalDate toDate = dateToPicker.getValue();
                
                if (fromDate == null) fromDate = LocalDate.now().minusYears(1);
                if (toDate == null) toDate = LocalDate.now();
                
                PdfExportService.exportStudentReport(
                        selectedStudent,
                        individualAttTable.getItems(),
                        individualRankLabel.getText(),
                        individualAvgScoreLabel.getText(),
                        individualAttRateLabel.getText(),
                        attImage,
                        perfImage,
                        fromDate,
                        toDate,
                        file
                );

                showStatus("Talaba hisoboti PDF formatida saqlandi.", false);
            } catch (IOException e) {
                log.error(e.getMessage());
                showStatus("PDF eksport qilishda xatolik: " + e.getMessage(), true);
            }
        }
    }

    @Override
    public void initialize(NavigationController navigationController) {
        this.navigationController = navigationController;
    }
}
