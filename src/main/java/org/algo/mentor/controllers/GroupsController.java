package org.algo.mentor.controllers;

import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import org.algo.mentor.core.NavigableController;
import org.algo.mentor.core.NavigationController;
import org.algo.mentor.models.Group;
import org.algo.mentor.models.Student;
import org.algo.mentor.services.GroupService;
import org.algo.mentor.services.StudentService;

import java.util.Optional;

public class GroupsController implements NavigableController {
    @FXML private TextField nameFilterField;
    @FXML private FlowPane groupsFlowPane;
    
    // Sidebar
    @FXML private VBox groupSidebar;
    @FXML private Pane overlayPane;
    @FXML private TextField groupNameField;
    @FXML private TextField studentSearchField;
    @FXML private ListView<Student> studentSearchResultsList;
    @FXML private VBox groupStudentsVBox;
    @FXML private Button deleteGroupBtn;
    @FXML private Button saveGroupBtn;

    private NavigationController navigationController;
    private Group selectedGroup;
    private final ObservableList<Student> pendingStudents = FXCollections.observableArrayList();
    private final ObservableList<Student> studentsToRemove = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        loadGroups();
        nameFilterField.textProperty().addListener((obs, old, val) -> loadGroups());
        
        studentSearchField.textProperty().addListener((obs, old, val) -> {
            if (val != null && val.trim().length() >= 5) {
                searchStudents(val.trim());
            } else if (val == null || val.trim().isEmpty()) {
                studentSearchResultsList.setVisible(false);
                studentSearchResultsList.setManaged(false);
            }
        });

        // Gurux nomini kiritish uchun inputga enterni bossa burux yaratilmasin
        // groupNameField.setOnAction(e -> onSaveGroupClick()); 
        
        studentSearchResultsList.setCellFactory(lv -> new ListCell<Student>() {
            @Override
            protected void updateItem(Student student, boolean empty) {
                super.updateItem(student, empty);
                if (empty || student == null) {
                    setText(null);
                } else {
                    setText(student.getFirstName() + " " + student.getLastName() + " (" + student.getPhone() + ")");
                }
            }
        });

        studentSearchResultsList.setOnMouseClicked(event -> {
            Student selected = studentSearchResultsList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                addStudentToGroup(selected);
            }
        });
    }

    private void loadGroups() {
        String filter = nameFilterField.getText();
        ObservableList<Group> groups = GroupService.searchGroups(filter);
        
        Platform.runLater(() -> {
            groupsFlowPane.getChildren().clear();
            for (Group group : groups) {
                groupsFlowPane.getChildren().add(createGroupCard(group));
            }
        });
    }

    private VBox createGroupCard(Group group) {
        VBox card = new VBox(15);
        card.setPrefWidth(280);
        card.getStyleClass().add("student-card"); // Reuse same style
        card.setStyle(card.getStyle() + "; -fx-padding: 25;");
        
        card.setOnMouseClicked(e -> openGroupSidebar(group));

        Label nameLabel = new Label(group.getName());
        nameLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #2d3748;");
        
        HBox countBox = new HBox(8);
        countBox.setAlignment(Pos.CENTER_LEFT);
        Circle dot = new Circle(4, Color.web("#3182ce"));
        Label countLabel = new Label(group.getStudentCount() + " ta o'quvchi");
        countLabel.setStyle("-fx-text-fill: #718096; -fx-font-size: 14;");
        countBox.getChildren().addAll(dot, countLabel);

        card.getChildren().addAll(nameLabel, countBox);
        return card;
    }

    private void openGroupSidebar(Group group) {
        this.selectedGroup = group;
        this.pendingStudents.clear();
        this.studentsToRemove.clear();
        groupNameField.setStyle(""); // Reset validation style
        if (group != null) {
            groupNameField.setText(group.getName());
            deleteGroupBtn.setVisible(true);
            deleteGroupBtn.setManaged(true);
            saveGroupBtn.setText("Saqlash");
            loadGroupStudents();
        } else {
            groupNameField.setText("");
            groupNameField.requestFocus();
            deleteGroupBtn.setVisible(false);
            deleteGroupBtn.setManaged(false);
            saveGroupBtn.setText("Saqlash");
            groupStudentsVBox.getChildren().clear();
        }
        
        studentSearchField.clear();
        studentSearchResultsList.setVisible(false);
        studentSearchResultsList.setManaged(false);
        
        animateSidebar(groupSidebar, 0);
        overlayPane.setVisible(true);
    }

    private void loadGroupStudents() {
        groupStudentsVBox.getChildren().clear();
        
        ObservableList<Student> students;
        if (selectedGroup != null) {
            students = StudentService.getStudentsByGroup(selectedGroup.getId());
        } else {
            students = pendingStudents;
        }
        
        for (Student s : students) {
            boolean markedForRemoval = studentsToRemove.stream().anyMatch(st -> st.getId() == s.getId());
            groupStudentsVBox.getChildren().add(createStudentRow(s, markedForRemoval));
        }
    }

    private HBox createStudentRow(Student s, boolean markedForRemoval) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        
        String bgColor = markedForRemoval ? "#fee2e2" : "#ffffff";
        String borderColor = markedForRemoval ? "#fca5a5" : "#f1f5f9";
        row.setStyle("-fx-background-color: " + bgColor + "; -fx-padding: 12; -fx-border-color: " + borderColor + "; -fx-border-radius: 8; -fx-background-radius: 8;");
        
        VBox info = new VBox(2);
        Label name = new Label(s.getFirstName() + " " + s.getLastName());
        String nameColor = markedForRemoval ? "#991b1b" : "#334155";
        name.setStyle("-fx-font-weight: bold; -fx-text-fill: " + nameColor + ";");
        Label phone = new Label(s.getPhone());
        String phoneColor = markedForRemoval ? "#dc2626" : "#64748b";
        phone.setStyle("-fx-font-size: 11; -fx-text-fill: " + phoneColor + ";");
        info.getChildren().addAll(name, phone);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button removeBtn = new Button(markedForRemoval ? "↺" : "✕");
        String btnColor = markedForRemoval ? "#dc2626" : "#94a3b8";
        removeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + btnColor + "; -fx-cursor: hand; -fx-font-weight: bold; -fx-font-size: 14;");
        removeBtn.setOnAction(e -> {
            if (markedForRemoval) {
                studentsToRemove.removeIf(st -> st.getId() == s.getId());
                loadGroupStudents();
            } else {
                removeStudentFromGroup(s);
            }
        });
        
        row.getChildren().addAll(info, spacer, removeBtn);
        return row;
    }

    @FXML
    private void onSearchStudentClick() {
        String query = studentSearchField.getText().trim();
        if (query.isEmpty()) return;
        searchStudents(query);
    }

    private void searchStudents(String query) {
        ObservableList<Student> results = StudentService.searchStudentsGlobal(query);
        studentSearchResultsList.setItems(results);
        studentSearchResultsList.setVisible(!results.isEmpty());
        studentSearchResultsList.setManaged(!results.isEmpty());
    }

    private void addStudentToGroup(Student student) {
        if (selectedGroup != null) {
            StudentService.addStudentToGroup(student.getId(), selectedGroup.getId());
        } else {
            if (pendingStudents.stream().noneMatch(s -> s.getId() == student.getId())) {
                pendingStudents.add(student);
            }
        }
        
        studentSearchField.clear();
        studentSearchResultsList.setVisible(false);
        studentSearchResultsList.setManaged(false);
        
        loadGroupStudents();
        loadGroups();
    }

    private void removeStudentFromGroup(Student student) {
        if (selectedGroup != null) {
            if (studentsToRemove.stream().noneMatch(st -> st.getId() == student.getId())) {
                studentsToRemove.add(student);
            }
        } else {
            pendingStudents.removeIf(s -> s.getId() == student.getId());
        }
        
        loadGroupStudents();
    }

    @FXML
    private void onAddGroupClick() {
        openGroupSidebar(null);
    }

    @FXML
    private void onSaveGroupClick() {
        String name = groupNameField.getText().trim();
        if (name.isEmpty()) {
            groupNameField.setStyle("-fx-border-color: #e53e3e; -fx-border-width: 1px; -fx-border-radius: 5px;");
            groupNameField.requestFocus();
            return;
        }
        groupNameField.setStyle("");

        if (selectedGroup == null) {
            // Yangi guruh yaratish
            int id = GroupService.addGroup(name);
            if (id != -1) {
                this.selectedGroup = new Group(id, name, 0);
                
                // Tanlangan o'quvchilarni biriktirish
                for (Student s : pendingStudents) {
                    StudentService.addStudentToGroup(s.getId(), id);
                }
                pendingStudents.clear();
                
                saveGroupBtn.setText("Saqlash");
                deleteGroupBtn.setVisible(true);
                deleteGroupBtn.setManaged(true);
                loadGroupStudents();
                loadGroups();
                closeSidebars();
            }
        } else {
            // Mavjud guruhni tahrirlash
            GroupService.renameGroup(selectedGroup.getId(), name);
            selectedGroup.setName(name);
            
            // O'chirilishi kerak bo'lgan o'quvchilarni o'chirish
            for (Student s : studentsToRemove) {
                StudentService.removeStudentFromGroup(s.getId(), selectedGroup.getId());
            }
            studentsToRemove.clear();
            
            loadGroups();
            closeSidebars();
        }
    }

    @FXML
    private void onDeleteGroupClick() {
        if (selectedGroup != null) {
            GroupService.deleteGroup(selectedGroup.getId());
            closeSidebars();
            loadGroups();
        }
    }

    @FXML
    private void closeSidebars() {
        animateSidebar(groupSidebar, 450);
        overlayPane.setVisible(false);
    }

    private void animateSidebar(VBox sidebar, double toX) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(300), sidebar);
        tt.setToX(toX);
        tt.play();
    }

    // Navigation
    @FXML private void onDashboardClick() { navigationController.navigateTo("dashboard-view.fxml", "DashboardController"); }
    @FXML private void onStudentsClick() { navigationController.navigateTo("students-view.fxml", "StudentsController"); }

    @Override
    public void initialize(NavigationController navigationController) {
        this.navigationController = navigationController;
    }
}
