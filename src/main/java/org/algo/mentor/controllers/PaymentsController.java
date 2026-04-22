package org.algo.mentor.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import org.algo.mentor.core.NavigableController;
import org.algo.mentor.core.NavigationController;
import org.algo.mentor.models.Group;
import org.algo.mentor.models.Student;
import org.algo.mentor.services.GroupService;
import org.algo.mentor.services.PaymentService;
import org.algo.mentor.services.PdfExportService;
import org.algo.mentor.services.StudentService;

import java.io.File;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PaymentsController implements NavigableController {

    @FXML private ComboBox<Group> groupComboBox;
    @FXML private ComboBox<String> yearComboBox;
    @FXML private VBox tableContainer;
    @FXML private ScrollPane outerScrollPane;
    @FXML private Button exportPdfBtn;

    private NavigationController navigationController;
    private List<YearMonth> months;
    private int selectedStartYear;

    private static final int COL_NR_W = 45;
    private static final int COL_NAME_W = 200;
    private static final int COL_DAY_W = 40;
    private static final int HEADER_H = 56;
    private static final int ROW_H = 34;

    private static final String[] MONTH_NAMES = {
        "Yanvar", "Fevral", "Mart", "Aprel", "May", "Iyun",
        "Iyul", "Avgust", "Sentabr", "Oktyabr", "Noyabr", "Dekabr"
    };

    private static final String HDR = "-fx-background-color: #3182ce; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12;";
    private static final String BORDER = "-fx-border-color: #e2e8f0; -fx-border-width: 0 1 1 0;";
    private static final String COL_BG_ODD  = "#f0f4f8";
    private static final String COL_BG_EVEN = "#ffffff";
    private static final String COL_SUB_ODD  = "#dce6f0";
    private static final String COL_SUB_EVEN = "#edf2f7";

    @FXML
    public void initialize() {
        LocalDate today = LocalDate.now();
        selectedStartYear = today.getYear();

        List<String> years = new ArrayList<>();
        for (int y = 2026; y <= 2035; y++) {
            years.add(String.valueOf(y));
        }
        yearComboBox.setItems(FXCollections.observableArrayList(years));
        yearComboBox.setValue(String.valueOf(selectedStartYear < 2026 ? 2026 : Math.min(selectedStartYear, 2035)));

        months = buildMonths(selectedStartYear);

        outerScrollPane.setOnScroll(event -> {
            if (event.getDeltaY() == 0) return;
            double contentH = tableContainer.getBoundsInLocal().getHeight();
            double viewportH = outerScrollPane.getViewportBounds().getHeight();
            if (contentH <= viewportH) return;
            double shift = -event.getDeltaY() * 3.0 / (contentH - viewportH);
            outerScrollPane.setVvalue(Math.max(0, Math.min(1, outerScrollPane.getVvalue() + shift)));
            event.consume();
        });

        ObservableList<Group> groups = GroupService.getAllGroups();
        groupComboBox.setItems(groups);
        groupComboBox.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Group g, boolean empty) {
                super.updateItem(g, empty);
                setText(empty || g == null ? null : g.getName());
            }
        });
        groupComboBox.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Group g, boolean empty) {
                super.updateItem(g, empty);
                setText(empty || g == null ? null : g.getName());
            }
        });
    }

    private List<YearMonth> buildMonths(int startYear) {
        List<YearMonth> list = new ArrayList<>();
        for (int m = 1; m <= 12; m++) list.add(YearMonth.of(startYear, m));
        return list;
    }

    @FXML
    private void onYearSelected() {
        String val = yearComboBox.getValue();
        if (val == null) return;
        selectedStartYear = Integer.parseInt(val);
        months = buildMonths(selectedStartYear);
        Group g = groupComboBox.getValue();
        if (g != null) buildPaymentTable(g);
    }

    @FXML
    private void onGroupSelected() {
        Group selected = groupComboBox.getValue();
        if (selected == null) return;
        buildPaymentTable(selected);
    }

    private void buildPaymentTable(Group group) {
        tableContainer.getChildren().clear();

        ObservableList<Student> students = StudentService.getStudentsByGroup(group.getId());
        if (students.isEmpty()) {
            Label empty = new Label("Bu guruhda o'quvchi yo'q");
            empty.setStyle("-fx-text-fill: #718096; -fx-font-size: 14; -fx-padding: 20;");
            tableContainer.getChildren().add(empty);
            return;
        }

        List<Integer> studentIds = new ArrayList<>();
        for (Student s : students) studentIds.add(s.getId());
        Set<String> paidKeys = PaymentService.getMonthlyPaymentKeysForStudents(studentIds);

        VBox table = new VBox(0);
        table.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-radius: 10; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.04), 8, 0, 0, 2);");

        table.getChildren().add(buildHeaderRow());

        for (int si = 0; si < students.size(); si++) {
            Student s = students.get(si);
            boolean even = si % 2 == 1;
            table.getChildren().add(buildDataRow(si + 1, s, even, paidKeys));
        }

        ScrollPane hScroll = new ScrollPane(table);
        hScroll.setFitToHeight(true);
        hScroll.setFitToWidth(false);
        hScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        hScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        hScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");
        hScroll.setOnScroll(event -> {
            double deltaX = event.getDeltaX();
            if (deltaX == 0) return;
            double contentWidth = table.getBoundsInLocal().getWidth();
            double viewportWidth = hScroll.getViewportBounds().getWidth();
            if (contentWidth <= viewportWidth) return;
            double scrollable = contentWidth - viewportWidth;
            double shift = -deltaX * 3.0 / scrollable;
            hScroll.setHvalue(Math.max(0, Math.min(1, hScroll.getHvalue() + shift)));
            event.consume();
        });

        VBox.setVgrow(hScroll, Priority.ALWAYS);
        tableContainer.getChildren().add(hScroll);
    }

    private HBox buildHeaderRow() {
        HBox row = new HBox(0);
        row.setPrefHeight(HEADER_H);
        row.setMinHeight(HEADER_H);
        row.setMaxHeight(HEADER_H);

        row.getChildren().add(makeHdrCell("Tr", COL_NR_W, HEADER_H));
        row.getChildren().add(makeHdrCell("Ism familiya", COL_NAME_W, HEADER_H));

        for (int mi = 0; mi < months.size(); mi++) {
            YearMonth ym = months.get(mi);
            String name = MONTH_NAMES[ym.getMonthValue() - 1];
            boolean isOddCol = mi % 2 == 0;
            String subBg = isOddCol ? COL_SUB_ODD : COL_SUB_EVEN;

            VBox monthBox = new VBox(0);
            monthBox.setPrefWidth(COL_DAY_W * 2);
            monthBox.setMinWidth(COL_DAY_W * 2);
            monthBox.setMaxWidth(COL_DAY_W * 2);
            monthBox.setMinHeight(HEADER_H);
            monthBox.setMaxHeight(HEADER_H);
            monthBox.setStyle(HDR + BORDER);

            Label monthLbl = new Label(name);
            monthLbl.setPrefHeight(HEADER_H / 2.0);
            monthLbl.setMinHeight(HEADER_H / 2.0);
            monthLbl.setMaxWidth(Double.MAX_VALUE);
            monthLbl.setAlignment(Pos.CENTER);
            monthLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: white; -fx-font-size: 11; -fx-border-color: #e2e8f0; -fx-border-width: 0 0 1 0;");

            HBox daysBox = new HBox(0);
            daysBox.setPrefHeight(HEADER_H / 2.0);
            daysBox.setMinHeight(HEADER_H / 2.0);

            Label d1 = new Label("1");
            d1.setPrefWidth(COL_DAY_W);
            d1.setMinWidth(COL_DAY_W);
            d1.setMaxWidth(COL_DAY_W);
            d1.setMaxHeight(Double.MAX_VALUE);
            d1.setAlignment(Pos.CENTER);
            d1.setStyle("-fx-background-color: " + subBg + "; -fx-text-fill: #2b6cb0; -fx-font-weight: bold; -fx-font-size: 11; -fx-border-color: #e2e8f0; -fx-border-width: 0 1 0 0;");

            Label d15 = new Label("15");
            d15.setPrefWidth(COL_DAY_W);
            d15.setMinWidth(COL_DAY_W);
            d15.setMaxWidth(COL_DAY_W);
            d15.setMaxHeight(Double.MAX_VALUE);
            d15.setAlignment(Pos.CENTER);
            d15.setStyle("-fx-background-color: " + subBg + "; -fx-text-fill: #2b6cb0; -fx-font-weight: bold; -fx-font-size: 11;");

            HBox.setHgrow(d1, Priority.NEVER);
            HBox.setHgrow(d15, Priority.NEVER);
            daysBox.getChildren().addAll(d1, d15);

            VBox.setVgrow(monthLbl, Priority.ALWAYS);
            VBox.setVgrow(daysBox, Priority.NEVER);
            monthBox.getChildren().addAll(monthLbl, daysBox);
            row.getChildren().add(monthBox);
        }

        return row;
    }

    private StackPane makeHdrCell(String text, double w, double h) {
        StackPane cell = new StackPane();
        cell.setPrefWidth(w);
        cell.setMinWidth(w);
        cell.setMaxWidth(w);
        cell.setPrefHeight(h);
        cell.setMinHeight(h);
        cell.setMaxHeight(h);
        cell.setStyle(HDR + BORDER);

        Label lbl = new Label(text);
        lbl.setStyle("-fx-font-weight: bold; -fx-text-fill: white; -fx-font-size: 12;");
        lbl.setAlignment(Pos.CENTER);
        cell.getChildren().add(lbl);
        StackPane.setAlignment(lbl, Pos.CENTER);
        return cell;
    }

    private HBox buildDataRow(int nr, Student s, boolean even, Set<String> paidKeys) {
        String rowBg = even ? "#f7fafc" : "#ffffff";
        HBox row = new HBox(0);
        row.setPrefHeight(ROW_H);
        row.setMinHeight(ROW_H);
        row.setMaxHeight(ROW_H);
        row.setStyle("-fx-background-color: " + rowBg + ";");

        row.getChildren().add(makeDataCell(String.valueOf(nr), COL_NR_W, ROW_H, rowBg, true));
        row.getChildren().add(makeDataCell(s.getFirstName() + " " + s.getLastName(), COL_NAME_W, ROW_H, rowBg, false));

        for (int mi = 0; mi < months.size(); mi++) {
            YearMonth ym = months.get(mi);
            boolean isOddCol = mi % 2 == 0;
            String colBg = isOddCol ? COL_BG_ODD : COL_BG_EVEN;

            String key1 = s.getId() + "_" + ym.getYear() + "_" + ym.getMonthValue() + "_1";
            String key15 = s.getId() + "_" + ym.getYear() + "_" + ym.getMonthValue() + "_15";

            Button btn1 = createToggleBtn(s.getId(), ym, 1, paidKeys.contains(key1), colBg);
            Button btn15 = createToggleBtn(s.getId(), ym, 15, paidKeys.contains(key15), colBg);
            row.getChildren().addAll(btn1, btn15);
        }

        return row;
    }

    private StackPane makeDataCell(String text, double w, double h, String bg, boolean center) {
        StackPane cell = new StackPane();
        cell.setPrefWidth(w);
        cell.setMinWidth(w);
        cell.setMaxWidth(w);
        cell.setPrefHeight(h);
        cell.setMinHeight(h);
        cell.setMaxHeight(h);
        cell.setStyle("-fx-background-color: " + bg + "; " + BORDER);

        Label lbl = new Label(text);
        lbl.setStyle("-fx-text-fill: #2d3748; -fx-font-size: 12;");
        if (center) {
            lbl.setAlignment(Pos.CENTER);
            StackPane.setAlignment(lbl, Pos.CENTER);
        } else {
            lbl.setAlignment(Pos.CENTER_LEFT);
            StackPane.setAlignment(lbl, Pos.CENTER_LEFT);
            lbl.setPadding(new javafx.geometry.Insets(0, 8, 0, 10));
        }
        cell.getChildren().add(lbl);
        return cell;
    }

    private Button createToggleBtn(int studentId, YearMonth ym, int day, boolean paid, String bg) {
        Button btn = new Button(paid ? "+" : "");
        btn.setPrefWidth(COL_DAY_W);
        btn.setMinWidth(COL_DAY_W);
        btn.setMaxWidth(COL_DAY_W);
        btn.setPrefHeight(ROW_H);
        btn.setMinHeight(ROW_H);
        btn.setMaxHeight(ROW_H);
        applyBtnStyle(btn, paid, bg);

        btn.setOnAction(e -> {
            boolean nowPaid = "+".equals(btn.getText());
            PaymentService.toggleMonthlyPayment(studentId, ym.getYear(), ym.getMonthValue(), day);
            boolean newPaid = !nowPaid;
            btn.setText(newPaid ? "+" : "");
            applyBtnStyle(btn, newPaid, bg);
        });
        return btn;
    }

    private void applyBtnStyle(Button btn, boolean paid, String bg) {
        if (paid) {
            btn.setStyle("-fx-background-color: #c6f6d5; -fx-text-fill: #276749; -fx-font-weight: bold; -fx-font-size: 15; -fx-cursor: hand; " + BORDER);
        } else {
            btn.setStyle("-fx-background-color: " + bg + "; -fx-text-fill: #cbd5e0; -fx-font-size: 13; -fx-cursor: hand; " + BORDER);
        }
    }

    @FXML
    private void onExportPdfClick() {
        Group selected = groupComboBox.getValue();
        if (selected == null) {
            new Alert(Alert.AlertType.WARNING, "Iltimos, avval guruhni tanlang!", ButtonType.OK).showAndWait();
            return;
        }

        FileChooser chooser = new FileChooser();
        chooser.setTitle("PDF faylni saqlash");
        chooser.setInitialFileName(selected.getName() + "_royxat.pdf");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF fayllar", "*.pdf"));

        File file = chooser.showSaveDialog(tableContainer.getScene().getWindow());
        if (file == null) return;

        ObservableList<Student> students = StudentService.getStudentsByGroup(selected.getId());
        try {
            PdfExportService.exportGroupStudentList(selected.getName(), students, file);
            new Alert(Alert.AlertType.INFORMATION, "PDF muvaffaqiyatli saqlandi!", ButtonType.OK).showAndWait();
        } catch (Exception ex) {
            ex.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Xatolik: " + ex.getMessage(), ButtonType.OK).showAndWait();
        }
    }

    @Override
    public void initialize(NavigationController navigationController) {
        this.navigationController = navigationController;
    }
}
