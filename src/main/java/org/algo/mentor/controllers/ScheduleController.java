package org.algo.mentor.controllers;

import javafx.animation.TranslateTransition;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.algo.mentor.core.NavigableController;
import org.algo.mentor.core.NavigationController;
import org.algo.mentor.models.Group;
import org.algo.mentor.models.Schedule;
import org.algo.mentor.services.GroupService;
import org.algo.mentor.services.ScheduleService;

import javafx.stage.FileChooser;
import org.algo.mentor.services.PdfExportService;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScheduleController implements NavigableController {

    @FXML private ComboBox<Group> groupFilterCombo;
    @FXML private TableView<Schedule> scheduleTable;
    @FXML private TableColumn<Schedule, String> groupNameCol;
    @FXML private TableColumn<Schedule, Integer> dayOfWeekCol;
    @FXML private TableColumn<Schedule, String> timeCol;
    @FXML private TableColumn<Schedule, String> nextOccurrenceCol;

    @FXML private Pane overlayPane;
    @FXML private VBox scheduleSidebar;
    @FXML private ComboBox<Group> groupSelectCombo;
    @FXML private ToggleButton day1Btn, day2Btn, day3Btn, day4Btn, day5Btn, day6Btn, day7Btn;
    @FXML private ComboBox<String> lessonTimeCombo;
    
    @FXML private ToggleGroup timeModeGroup;
    @FXML private RadioButton sameTimeRadio;
    @FXML private RadioButton individualTimeRadio;
    @FXML private VBox sameTimeSection;
    @FXML private VBox individualTimeSection;
    @FXML private VBox individualTimesContainer;
    @FXML private VBox scheduleContentVBox;

    private NavigationController navigationController;
    private final ObservableList<Schedule> allSchedules = FXCollections.observableArrayList();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private final Map<Integer, ComboBox<String>> individualTimeCombos = new HashMap<>();

    @FXML
    public void initialize() {
        setupTable();
        loadData();
        setupFilters();
        setupSidebarCombos();
        setupModeListeners();
        setupDayListeners();
    }

    private void setupModeListeners() {
        timeModeGroup.selectedToggleProperty().addListener((obs, old, val) -> {
            boolean isSame = sameTimeRadio.isSelected();
            sameTimeSection.setVisible(isSame);
            sameTimeSection.setManaged(isSame);
            individualTimeSection.setVisible(!isSame);
            individualTimeSection.setManaged(!isSame);
        });
    }

    private void setupDayListeners() {
        ToggleButton[] dayBtns = {day1Btn, day2Btn, day3Btn, day4Btn, day5Btn, day6Btn, day7Btn};
        for (ToggleButton btn : dayBtns) {
            btn.selectedProperty().addListener((obs, old, val) -> updateIndividualTimeRows());
        }
    }

    private void updateIndividualTimeRows() {
        individualTimesContainer.getChildren().clear();
        individualTimeCombos.clear();
        
        ToggleButton[] dayBtns = {day1Btn, day2Btn, day3Btn, day4Btn, day5Btn, day6Btn, day7Btn};
        List<String> times = generateTimes();

        for (int i = 0; i < dayBtns.length; i++) {
            if (dayBtns[i].isSelected()) {
                int dayNum = i + 1;
                HBox row = new HBox(10);
                row.setAlignment(Pos.CENTER_LEFT);
                
                Label dayLabel = new Label(getDayName(dayNum));
                dayLabel.setPrefWidth(100);
                dayLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #4a5568;");
                
                ComboBox<String> timeCombo = new ComboBox<>(FXCollections.observableArrayList(times));
                timeCombo.setPromptText("Vaqt...");
                timeCombo.getStyleClass().add("form-control");
                timeCombo.setMaxWidth(Double.MAX_VALUE);
                HBox.setHgrow(timeCombo, Priority.ALWAYS);
                
                // If we have a global time selected, default to it
                if (lessonTimeCombo.getValue() != null) {
                    timeCombo.setValue(lessonTimeCombo.getValue());
                }

                individualTimeCombos.put(dayNum, timeCombo);
                row.getChildren().addAll(dayLabel, timeCombo);
                individualTimesContainer.getChildren().add(row);
            }
        }
    }

    private List<String> generateTimes() {
        List<String> times = new ArrayList<>();
        for (int h = 8; h <= 21; h++) {
            times.add(String.format("%02d:00", h));
            times.add(String.format("%02d:30", h));
        }
        return times;
    }

    private void setupTable() {
        groupNameCol.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getGroupName()));
        dayOfWeekCol.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getDayOfWeek()));
        timeCol.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getLessonTime()));
        
        nextOccurrenceCol.setCellValueFactory(cd -> {
            var next = ScheduleService.getNextOccurrence(cd.getValue());
            return new ReadOnlyObjectWrapper<>(next.format(dateFormatter));
        });

        dayOfWeekCol.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else setText(getDayName(item));
            }
        });
    }

    private String getDayName(int day) {
        return switch (day) {
            case 1 -> "Dushanba";
            case 2 -> "Seshanba";
            case 3 -> "Chorshanba";
            case 4 -> "Payshanba";
            case 5 -> "Juma";
            case 6 -> "Shanba";
            case 7 -> "Yakshanba";
            default -> "";
        };
    }

    private void loadData() {
        allSchedules.setAll(ScheduleService.getAllSchedules());
        scheduleTable.setItems(allSchedules);
    }

    private void setupFilters() {
        ObservableList<Group> groups = GroupService.getAllGroups();
        groupFilterCombo.setItems(groups);
        groupFilterCombo.getSelectionModel().selectedItemProperty().addListener((obs, old, val) -> {
            if (val != null) {
                scheduleTable.setItems(ScheduleService.searchSchedules(val.getId()));
            } else {
                scheduleTable.setItems(allSchedules);
            }
        });
    }

    private void setupSidebarCombos() {
        groupSelectCombo.setItems(GroupService.getAllGroups());
        groupSelectCombo.getSelectionModel().selectedItemProperty().addListener((obs, old, val) -> onGroupSelected(val));
        
        List<String> times = new ArrayList<>();
        for (int h = 8; h <= 21; h++) {
            times.add(String.format("%02d:00", h));
            times.add(String.format("%02d:30", h));
        }
        lessonTimeCombo.setItems(FXCollections.observableArrayList(times));
    }

    @FXML
    private void onAddScheduleClick() {
        openSidebar();
    }

    @FXML
    private void onExportPdfClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("PDF ni saqlash");
        fileChooser.setInitialFileName("dars_jadvali.pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        
        File file = fileChooser.showSaveDialog(scheduleTable.getScene().getWindow());
        if (file != null) {
            try {
                PdfExportService.exportSchedule(scheduleTable.getItems(), file);
                
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Muvaffaqiyatli");
                alert.setHeaderText(null);
                alert.setContentText("Dars jadvali PDF formatida saqlandi.");
                alert.showAndWait();
            } catch (IOException e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Xatolik");
                alert.setHeaderText("PDF eksport qilishda xatolik");
                alert.setContentText(e.getMessage());
                alert.showAndWait();
            }
        }
    }

    @FXML
    private void onSaveScheduleClick() {
        Group group = groupSelectCombo.getValue();
        if (group == null) return;

        boolean isSameMode = sameTimeRadio.isSelected();
        Map<Integer, String> scheduleData = new HashMap<>();

        if (isSameMode) {
            String time = lessonTimeCombo.getValue();
            if (time == null) return;
            
            if (day1Btn.isSelected()) scheduleData.put(1, time);
            if (day2Btn.isSelected()) scheduleData.put(2, time);
            if (day3Btn.isSelected()) scheduleData.put(3, time);
            if (day4Btn.isSelected()) scheduleData.put(4, time);
            if (day5Btn.isSelected()) scheduleData.put(5, time);
            if (day6Btn.isSelected()) scheduleData.put(6, time);
            if (day7Btn.isSelected()) scheduleData.put(7, time);
        } else {
            for (Map.Entry<Integer, ComboBox<String>> entry : individualTimeCombos.entrySet()) {
                String time = entry.getValue().getValue();
                if (time != null) {
                    scheduleData.put(entry.getKey(), time);
                }
            }
        }

        // To support "editing" (replacing old schedule with new selection),
        // we can either update changed ones or just clear and re-add.
        // Given we might have deselected some days, clearing old ones for this group is safer.
        ScheduleService.deleteSchedulesByGroup(group.getId());

        if (scheduleData.isEmpty()) {
            loadData();
            closeSidebar();
            clearSidebarFields();
            return;
        }

        boolean allSaved = true;
        for (Map.Entry<Integer, String> entry : scheduleData.entrySet()) {
            if (!ScheduleService.addSchedule(group.getId(), entry.getKey(), entry.getValue())) {
                allSaved = false;
            }
        }

        if (allSaved) {
            loadData();
            closeSidebar();
            clearSidebarFields();
        }
    }

    private void onGroupSelected(Group group) {
        if (group == null) {
            scheduleContentVBox.setDisable(true);
            clearSidebarFields();
            return;
        }

        scheduleContentVBox.setDisable(false);

        // Reset sidebar state before loading
        ToggleButton[] dayBtns = {day1Btn, day2Btn, day3Btn, day4Btn, day5Btn, day6Btn, day7Btn};
        for (ToggleButton btn : dayBtns) btn.setSelected(false);
        lessonTimeCombo.setValue(null);
        individualTimesContainer.getChildren().clear();
        individualTimeCombos.clear();

        List<Schedule> groupSchedules = ScheduleService.searchSchedules(group.getId());
        if (groupSchedules.isEmpty()) return;

        // Check if all schedules have the same time
        String firstTime = groupSchedules.get(0).getLessonTime();
        boolean allSameTime = groupSchedules.stream().allMatch(s -> s.getLessonTime().equals(firstTime));

        if (allSameTime) {
            sameTimeRadio.setSelected(true);
            lessonTimeCombo.setValue(firstTime);
            for (Schedule s : groupSchedules) {
                if (s.getDayOfWeek() >= 1 && s.getDayOfWeek() <= 7) {
                    dayBtns[s.getDayOfWeek() - 1].setSelected(true);
                }
            }
        } else {
            individualTimeRadio.setSelected(true);
            // First select the days
            for (Schedule s : groupSchedules) {
                if (s.getDayOfWeek() >= 1 && s.getDayOfWeek() <= 7) {
                    dayBtns[s.getDayOfWeek() - 1].setSelected(true);
                }
            }
            // updateIndividualTimeRows is called via listeners, but we need to set values
            updateIndividualTimeRows();
            for (Schedule s : groupSchedules) {
                ComboBox<String> combo = individualTimeCombos.get(s.getDayOfWeek());
                if (combo != null) {
                    combo.setValue(s.getLessonTime());
                }
            }
        }
    }

    private void clearSidebarFields() {
        groupSelectCombo.getSelectionModel().clearSelection();
        lessonTimeCombo.setValue(null);
        day1Btn.setSelected(false);
        day2Btn.setSelected(false);
        day3Btn.setSelected(false);
        day4Btn.setSelected(false);
        day5Btn.setSelected(false);
        day6Btn.setSelected(false);
        day7Btn.setSelected(false);
        sameTimeRadio.setSelected(true);
        individualTimesContainer.getChildren().clear();
        individualTimeCombos.clear();
    }

    @FXML
    private void onClearFilterClick() {
        groupFilterCombo.getSelectionModel().clearSelection();
        loadData();
    }

    private void openSidebar() {
        overlayPane.setVisible(true);
        TranslateTransition tt = new TranslateTransition(Duration.millis(300), scheduleSidebar);
        tt.setToX(0);
        tt.play();
    }

    @FXML
    private void closeSidebar() {
        TranslateTransition tt = new TranslateTransition(Duration.millis(300), scheduleSidebar);
        tt.setToX(400);
        tt.setOnFinished(e -> overlayPane.setVisible(false));
        tt.play();
    }

    @Override
    public void initialize(NavigationController navigationController) {
        this.navigationController = navigationController;
    }
}
