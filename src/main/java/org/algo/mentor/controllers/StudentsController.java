package org.algo.mentor.controllers;

import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import org.algo.mentor.core.NavigableController;
import org.algo.mentor.core.NavigationController;
import org.algo.mentor.models.Group;
import org.algo.mentor.models.Payment;
import org.algo.mentor.models.Student;
import org.algo.mentor.services.GroupService;
import org.algo.mentor.services.PaymentService;
import org.algo.mentor.services.StudentService;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.function.UnaryOperator;

public class StudentsController implements NavigableController {

    @FXML private TextField nameFilterField;
    @FXML private TextField phoneFilterField;
    @FXML private ComboBox<String> statusFilterCombo;
    @FXML private ComboBox<String> groupFilterCombo;
    @FXML private FlowPane studentsFlowPane;
    
    // Student Sidebar
    @FXML private VBox studentSidebar;
    @FXML private Pane overlayPane;
    @FXML private TextField fullNameField;
    @FXML private TextField phoneField;
    @FXML private TextField telegramField;
    @FXML private TextField parentNameField;
    @FXML private TextField parentPhoneField;
    @FXML private TextField parentTelegramField;
    @FXML private CheckBox isActiveCheck;
    @FXML private Label statusLabel;
    @FXML private VBox deleteConfirmBox;
    @FXML private HBox actionButtonsBox;
    @FXML private Button deleteBtn;
    
    // Payment Sidebar
    @FXML private VBox paymentSidebar;
    @FXML private Label paymentStudentNameLabel;
    @FXML private DatePicker paymentFromDatePicker;
    @FXML private DatePicker paymentToDatePicker;
    @FXML private TextField paymentAmountField;
    @FXML private VBox paymentsHistoryVBox;

    private NavigationController navigationController;
    private Student selectedStudent;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @FXML
    public void initialize() {
        setupFilters();
        loadStudents();
        setupPhoneMasks();
        setupAmountMask();
        setupEscKeyHandler();
        setupDateSync();
        setupValidationListeners();
        
        // Initial default values - previous month
        LocalDate firstOfPrevMonth = LocalDate.now().minusMonths(1).withDayOfMonth(1);
        paymentFromDatePicker.setValue(firstOfPrevMonth);
    }

    private void setupValidationListeners() {
        fullNameField.textProperty().addListener((obs, old, val) -> fullNameField.setStyle(""));
        phoneField.textProperty().addListener((obs, old, val) -> phoneField.setStyle(""));
        paymentAmountField.textProperty().addListener((obs, old, val) -> paymentAmountField.setStyle(""));
    }

    private void setupDateSync() {
        // Sync 'To' date when 'From' date changes
        paymentFromDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                paymentToDatePicker.setValue(newVal.withDayOfMonth(newVal.lengthOfMonth()));
            }
        });

        // Force yyyy-MM-dd format for display
        String pattern = "yyyy-MM-dd";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        
        UnaryOperator<DatePicker> setConverter = dp -> {
            dp.setConverter(new javafx.util.StringConverter<LocalDate>() {
                @Override
                public String toString(LocalDate date) {
                    if (date != null) return formatter.format(date);
                    return "";
                }
                @Override
                public LocalDate fromString(String string) {
                    if (string != null && !string.isEmpty()) return LocalDate.parse(string, formatter);
                    return null;
                }
            });
            return dp;
        };
        
        setConverter.apply(paymentFromDatePicker);
        setConverter.apply(paymentToDatePicker);

        // Prevent selecting 'To' date earlier than 'From' date
        paymentToDatePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate fromDate = paymentFromDatePicker.getValue();
                if (fromDate != null && date != null) {
                    setDisable(empty || date.isBefore(fromDate));
                }
            }
        });
    }

    private void setupAmountMask() {
        paymentAmountField.setTextFormatter(new TextFormatter<>(change -> {
            if (!change.isContentChange()) return change;
            
            String newText = change.getControlNewText().replaceAll("[^\\d]", "");
            if (newText.isEmpty()) {
                change.setText("");
                change.setRange(0, change.getControlText().length());
                return change;
            }

            try {
                long amount = Long.parseLong(newText);
                java.text.DecimalFormatSymbols symbols = new java.text.DecimalFormatSymbols();
                symbols.setGroupingSeparator(',');
                java.text.DecimalFormat df = new java.text.DecimalFormat("#,###", symbols);
                String formatted = df.format(amount);
                
                change.setRange(0, change.getControlText().length());
                change.setText(formatted);
                change.setCaretPosition(formatted.length());
                change.setAnchor(formatted.length());
            } catch (NumberFormatException e) {
                return null;
            }
            return change;
        }));
    }

    private void setupPhoneMasks() {
        applyPhoneMask(phoneField);
        applyPhoneMask(parentPhoneField);
        applyPhoneMask(phoneFilterField);
    }

    private void applyPhoneMask(TextField textField) {
        textField.setText("+998 ");
        
        textField.setTextFormatter(new TextFormatter<>(change -> {
            if (!change.isContentChange()) return change;

            String oldText = change.getControlText();
            String newText = change.getControlNewText();

            // Backspace handling for formatting characters
            if (change.isDeleted() && change.getText().isEmpty()) {
                String deletedText = oldText.substring(change.getRangeStart(), change.getRangeEnd());
                if (deletedText.chars().noneMatch(Character::isDigit)) {
                    // Find the last digit before the deletion point
                    String textBeforeDeletion = oldText.substring(0, change.getRangeStart());
                    int digitToDeleteIdx = -1;
                    for (int i = textBeforeDeletion.length() - 1; i >= 0; i--) {
                        if (Character.isDigit(textBeforeDeletion.charAt(i))) {
                            if (i > 3) { // Avoid deleting +998
                                digitToDeleteIdx = i;
                            }
                            break;
                        }
                    }
                    if (digitToDeleteIdx != -1) {
                        newText = oldText.substring(0, digitToDeleteIdx) + oldText.substring(digitToDeleteIdx + 1);
                    }
                }
            }

            // Extract only the 9 digits after 998
            String allDigits = newText.replaceAll("\\D", "");
            String digitsOnly = "";
            if (allDigits.startsWith("998")) {
                digitsOnly = allDigits.substring(3);
            } else {
                digitsOnly = allDigits;
            }
            
            if (digitsOnly.length() > 9) {
                digitsOnly = digitsOnly.substring(0, 9);
            }
            
            StringBuilder sb = new StringBuilder("+998 ");
            if (digitsOnly.length() > 0) {
                sb.append("(").append(digitsOnly.substring(0, Math.min(digitsOnly.length(), 2)));
                if (digitsOnly.length() >= 2) sb.append(") ");
            }
            if (digitsOnly.length() > 2) {
                sb.append(digitsOnly.substring(2, Math.min(digitsOnly.length(), 5)));
            }
            if (digitsOnly.length() > 5) {
                sb.append(" ").append(digitsOnly.substring(5, Math.min(digitsOnly.length(), 9)));
            }
            
            String result = sb.toString();
            change.setRange(0, oldText.length());
            change.setText(result);
            change.setCaretPosition(result.length());
            change.setAnchor(result.length());
            
            return change;
        }));
    }

    private String sanitizePhone(String phone) {
        if (phone == null) return "";
        String digits = phone.replaceAll("\\D", "");
        return "+" + digits;
    }

    private boolean isValidPhone(String phone) {
        if (phone == null) return false;
        return phone.replaceAll("\\D", "").length() == 12;
    }

    private void setupEscKeyHandler() {
        Platform.runLater(() -> {
            if (studentsFlowPane.getScene() != null) {
                studentsFlowPane.getScene().addEventHandler(KeyEvent.KEY_PRESSED, event -> {
                    if (event.getCode() == KeyCode.ESCAPE) {
                        if (overlayPane.isVisible()) {
                            closeSidebars();
                        }
                    }
                });
            }
        });
    }

    private void setupFilters() {
        statusFilterCombo.setItems(FXCollections.observableArrayList("Hammasi", "Faol", "Faol emas"));
        statusFilterCombo.setValue("Hammasi");
        
        ObservableList<String> groupNames = FXCollections.observableArrayList("Hammasi");
        for (Group g : GroupService.getAllGroups()) {
            groupNames.add(g.getName());
        }
        groupFilterCombo.setItems(groupNames);
        groupFilterCombo.setValue("Hammasi");

        // Listeners for real-time filtering
        nameFilterField.textProperty().addListener((obs, old, val) -> loadStudents());
        phoneFilterField.textProperty().addListener((obs, old, val) -> loadStudents());
        statusFilterCombo.valueProperty().addListener((obs, old, val) -> loadStudents());
        groupFilterCombo.valueProperty().addListener((obs, old, val) -> loadStudents());
    }

    private void loadStudents() {
        StudentService.updateStudentPaymentStatus();
        
        String name = nameFilterField.getText();
        String phone = sanitizePhone(phoneFilterField.getText());
        if (phone.equals("+998")) phone = "";
        
        String status = statusFilterCombo.getValue();
        String group = groupFilterCombo.getValue();

        ObservableList<Student> students = StudentService.searchStudents(name, phone, status, group);
        
        Platform.runLater(() -> {
            studentsFlowPane.getChildren().clear();
            for (Student student : students) {
                studentsFlowPane.getChildren().add(createStudentCard(student));
            }
        });
    }

    private VBox createStudentCard(Student student) {
        VBox card = new VBox(10);
        card.setPrefWidth(260);
        card.getStyleClass().add("student-card");
        
        card.setOnMouseClicked(e -> openStudentSidebar(student));

        Label nameLabel = new Label(student.getFirstName() + " " + student.getLastName());
        nameLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        String formattedPhone = student.getPhone();
        if (formattedPhone != null && formattedPhone.length() == 13 && formattedPhone.startsWith("+998")) {
            // Format: +998 (90) 123 4567
            formattedPhone = String.format("+998 (%s) %s %s", 
                formattedPhone.substring(4, 6), 
                formattedPhone.substring(6, 9), 
                formattedPhone.substring(9, 13));
        }
        
        Label phoneLabel = new Label(formattedPhone);
        phoneLabel.setStyle("-fx-text-fill: #7f8c8d;");
        
        boolean hasValidPayment = StudentService.isStudentPaymentValid(student.getId());
        HBox statusBox = new HBox(5);
        statusBox.setAlignment(Pos.CENTER_LEFT);
        Circle statusCircle = new Circle(4, hasValidPayment ? Color.GREEN : Color.RED);
        Label statusLabel = new Label(hasValidPayment ? "Faol" : "Faol emas");
        statusLabel.setStyle("-fx-font-size: 12; -fx-text-fill: " + (hasValidPayment ? "#27ae60" : "#e74c3c") + ";");
        statusBox.getChildren().addAll(statusCircle, statusLabel);

        Button paymentsBtn = new Button("To'lovlar");
        paymentsBtn.getStyleClass().addAll("btn", "btn-info", "btn-sm");
        paymentsBtn.setPrefWidth(Double.MAX_VALUE);
        paymentsBtn.setOnAction(e -> {
            e.consume();
            openPaymentSidebar(student);
        });

        card.getChildren().addAll(nameLabel, phoneLabel, statusBox, paymentsBtn);
        return card;
    }

    // --- Sidebar Logic ---

    private void openStudentSidebar(Student student) {
        this.selectedStudent = student;
        resetDeleteConfirmation();
        if (student != null) {
            fullNameField.setText(student.getFirstName() + (student.getLastName().isEmpty() ? "" : " " + student.getLastName()));
            phoneField.setText(student.getPhone());
            telegramField.setText(student.getTelegramUsername());
            parentNameField.setText(student.getParentName());
            parentPhoneField.setText(student.getParentPhone());
            parentTelegramField.setText(student.getParentTelegram());
            boolean hasValidPayment = StudentService.isStudentPaymentValid(student.getId());
            isActiveCheck.setSelected(hasValidPayment);
            updateStatusLabel(hasValidPayment);
            deleteBtn.setVisible(true);
            deleteBtn.setManaged(true);
        } else {
            clearStudentForm();
            updateStatusLabel(false);
            deleteBtn.setVisible(false);
            deleteBtn.setManaged(false);
        }
        
        animateSidebar(studentSidebar, 0);
        overlayPane.setVisible(true);
    }

    private void updateStatusLabel(boolean isActive) {
        if (isActive) {
            statusLabel.setText("Faol");
            statusLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
        } else {
            statusLabel.setText("Faol emas");
            statusLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
        }
    }

    @FXML
    private void onStatusChange() {
        updateStatusLabel(isActiveCheck.isSelected());
    }

    private void openPaymentSidebar(Student student) {
        this.selectedStudent = student;
        paymentStudentNameLabel.setText(student.getFirstName() + " " + student.getLastName());
        
        // Reset fields for new payment
        paymentAmountField.clear();
        
        // Set default dates: previous month
        paymentFromDatePicker.setValue(LocalDate.now().minusMonths(1).withDayOfMonth(1));
        
        loadPaymentHistory();
        
        animateSidebar(paymentSidebar, 0);
        overlayPane.setVisible(true);
    }

    @FXML
    private void closeSidebars() {
        animateSidebar(studentSidebar, 400);
        animateSidebar(paymentSidebar, 450);
        overlayPane.setVisible(false);
        resetDeleteConfirmation();
    }

    private void resetDeleteConfirmation() {
        deleteConfirmBox.setVisible(false);
        deleteConfirmBox.setManaged(false);
        actionButtonsBox.setVisible(true);
    }

    private void animateSidebar(VBox sidebar, double toX) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(300), sidebar);
        tt.setToX(toX);
        tt.play();
    }

    private void clearStudentForm() {
        fullNameField.clear();
        phoneField.setText("+998 ");
        telegramField.clear();
        parentNameField.clear();
        parentPhoneField.setText("+998 ");
        parentTelegramField.clear();
        isActiveCheck.setSelected(true);
    }

    // --- Actions ---

    @FXML
    private void onAddStudentClick() {
        openStudentSidebar(null);
    }

    @FXML
    private void onSaveStudentClick() {
        boolean hasError = false;
        
        if (fullNameField.getText().trim().isEmpty()) {
            fullNameField.setStyle("-fx-border-color: #e53e3e; -fx-border-width: 1px; -fx-border-radius: 5px;");
            fullNameField.requestFocus();
            hasError = true;
        }

        if (!isValidPhone(phoneField.getText())) {
            phoneField.setStyle("-fx-border-color: #e53e3e; -fx-border-width: 1px; -fx-border-radius: 5px;");
            if (!hasError) {
                phoneField.requestFocus();
                hasError = true;
            }
        }

        if (hasError) return;

        String phone = sanitizePhone(phoneField.getText());
        String parentPhone = sanitizePhone(parentPhoneField.getText());
        if (parentPhone.equals("+998")) parentPhone = "";
        
        // Split full name into first and last name
        String fullName = fullNameField.getText().trim();
        String firstName = fullName;
        String lastName = "";
        int lastSpace = fullName.lastIndexOf(' ');
        if (lastSpace != -1) {
            firstName = fullName.substring(0, lastSpace).trim();
            lastName = fullName.substring(lastSpace + 1).trim();
        }

        // If parent phone is partially entered, block it
        if (!parentPhone.isEmpty() && !isValidPhone(parentPhoneField.getText())) {
            parentPhoneField.setStyle("-fx-border-color: #e53e3e; -fx-border-width: 1px; -fx-border-radius: 5px;");
            parentPhoneField.requestFocus();
            return;
        }

        if (selectedStudent == null) {
            // New Student
            int id = StudentService.addStudent(
                firstName, lastName, phone,
                telegramField.getText(), parentNameField.getText(), parentPhone,
                parentTelegramField.getText()
            );
        } else {
            // Update Existing
            StudentService.updateStudent(
                selectedStudent.getId(), firstName, lastName,
                phone, telegramField.getText(), parentNameField.getText(),
                parentPhone, parentTelegramField.getText()
            );
        }
        
        StudentService.updateStudentPaymentStatus();
        loadStudents();
        closeSidebars();
    }

    @FXML
    private void onDeleteStudentClick() {
        deleteConfirmBox.setVisible(true);
        deleteConfirmBox.setManaged(true);
        actionButtonsBox.setVisible(false);
    }

    @FXML
    private void onConfirmDeleteClick() {
        if (selectedStudent != null) {
            StudentService.deleteStudent(selectedStudent.getId());
            loadStudents();
            closeSidebars();
        }
    }

    @FXML
    private void onCancelDeleteClick() {
        resetDeleteConfirmation();
    }

    @FXML
    private void onSavePaymentClick() {
        if (selectedStudent == null) return;
        
        if (paymentAmountField.getText().isEmpty()) {
            paymentAmountField.setStyle("-fx-border-color: #e53e3e; -fx-border-width: 1px; -fx-border-radius: 5px;");
            paymentAmountField.requestFocus();
            return;
        }

        if (paymentFromDatePicker.getValue() == null || paymentToDatePicker.getValue() == null) {
            // These should usually not be null due to initialization, but just in case
            return;
        }

        try {
            // Commit any manually typed text in date pickers
            LocalDate fromDateVal = paymentFromDatePicker.getConverter().fromString(paymentFromDatePicker.getEditor().getText());
            LocalDate toDateVal = paymentToDatePicker.getConverter().fromString(paymentToDatePicker.getEditor().getText());
            if (fromDateVal == null) fromDateVal = paymentFromDatePicker.getValue();
            if (toDateVal == null) toDateVal = paymentToDatePicker.getValue();
            if (fromDateVal == null || toDateVal == null) return;

            // Remove separators before parsing
            String amountText = paymentAmountField.getText().replaceAll("[^\\d]", "");
            double amount = Double.parseDouble(amountText);
            String fromDate = fromDateVal.format(DATE_FORMAT);
            String toDate = toDateVal.format(DATE_FORMAT);

            PaymentService.addPayment(selectedStudent.getId(), amount, fromDate, toDate);
            
            paymentAmountField.clear();
            paymentAmountField.setStyle("");
            LocalDate firstOfPrevMonth = LocalDate.now().minusMonths(1).withDayOfMonth(1);
            paymentFromDatePicker.setValue(firstOfPrevMonth);
            
            loadPaymentHistory();
            loadStudents(); // Status might change
        } catch (NumberFormatException e) {
            paymentAmountField.setStyle("-fx-border-color: #e53e3e; -fx-border-width: 1px; -fx-border-radius: 5px;");
            paymentAmountField.requestFocus();
        }
    }

    private void loadPaymentHistory() {
        paymentsHistoryVBox.getChildren().clear();
        ObservableList<Payment> payments = PaymentService.getPaymentsByStudentId(selectedStudent.getId());
        
        for (Payment p : payments) {
            VBox item = new VBox(4);
            item.setStyle("-fx-background-color: #ffffff; -fx-padding: 10; -fx-border-color: #dee2e6; -fx-border-radius: 5;");

            Label amountLabel = new Label(String.format("%,.0f so'm", p.getAmount()));
            amountLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #27ae60;");

            Label periodLabel = new Label(p.getPaymentFromDate() + " — " + p.getPaymentToDate());
            periodLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #7f8c8d;");

            Label createdLabel = new Label("To'langan sana: " + p.getCreatedDate());
            createdLabel.setStyle("-fx-font-size: 10; -fx-text-fill: #bdc3c7;");

            HBox confirmRow = new HBox(6);
            confirmRow.setAlignment(Pos.CENTER_LEFT);
            confirmRow.setVisible(false);
            confirmRow.setManaged(false);

            Label confirmLabel = new Label("O'chirilsinmi?");
            confirmLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #e53e3e;");

            Button yesBtn = new Button("Ha");
            yesBtn.setStyle("-fx-background-color: #e53e3e; -fx-text-fill: white; -fx-font-size: 10; -fx-padding: 2 8; -fx-background-radius: 4; -fx-cursor: hand;");

            Button noBtn = new Button("Yo'q");
            noBtn.setStyle("-fx-background-color: #e2e8f0; -fx-text-fill: #4a5568; -fx-font-size: 10; -fx-padding: 2 8; -fx-background-radius: 4; -fx-cursor: hand;");

            confirmRow.getChildren().addAll(confirmLabel, yesBtn, noBtn);

            Button deleteBtn = new Button("O'chirish");
            deleteBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #a0aec0; -fx-font-size: 10; -fx-cursor: hand; -fx-padding: 0;");
            deleteBtn.setOnAction(e -> {
                confirmRow.setVisible(true);
                confirmRow.setManaged(true);
                deleteBtn.setVisible(false);
                deleteBtn.setManaged(false);
            });

            yesBtn.setOnAction(e -> {
                PaymentService.deletePayment(p.getId());
                loadPaymentHistory();
                loadStudents();
            });

            noBtn.setOnAction(e -> {
                confirmRow.setVisible(false);
                confirmRow.setManaged(false);
                deleteBtn.setVisible(true);
                deleteBtn.setManaged(true);
            });

            item.getChildren().addAll(amountLabel, periodLabel, createdLabel, deleteBtn, confirmRow);
            paymentsHistoryVBox.getChildren().add(item);
        }
    }

    @Override
    public void initialize(NavigationController navigationController) {
        this.navigationController = navigationController;
    }
}
