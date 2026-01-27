package org.algo.mentor.services;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.AreaBreakType;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;
import org.algo.mentor.models.Schedule;
import org.algo.mentor.models.Student;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PdfExportService {

    public static void exportStudentReport(
            Student student,
            List<ReportService.LessonScoreRow> attendanceDetails,
            String rank,
            String avgScore,
            String attRate,
            WritableImage attChart,
            WritableImage perfChart,
            File file
    ) throws IOException {
        PdfWriter writer = new PdfWriter(file);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        // Header
        document.add(new Paragraph("Talaba Shaxsiy Statistikasi")
                .setFontSize(20)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER));
        
        document.add(new Paragraph("Sana: " + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                .setTextAlignment(TextAlignment.RIGHT)
                .setFontSize(10));

        // Student Info
        document.add(new Paragraph("\nO'quvchi ma'lumotlari:").setBold().setFontSize(14));
        document.add(new Paragraph("F.I.SH: " + student.getFirstName() + " " + student.getLastName()));
        document.add(new Paragraph("Telefon: " + (student.getPhone() != null ? student.getPhone() : "Ko'rsatilmagan")));

        // Summary Stats Table
        document.add(new Paragraph("\nUmumiy ko'rsatkichlar (Oxirgi 1 yil):").setBold().setFontSize(14));
        Table summaryTable = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1}));
        summaryTable.setWidth(UnitValue.createPercentValue(100));
        
        summaryTable.addCell(createStatCell("Reyting", rank, new DeviceRgb(235, 248, 255)));
        summaryTable.addCell(createStatCell("O'rtacha Ball", avgScore, new DeviceRgb(240, 255, 244)));
        summaryTable.addCell(createStatCell("Davomat", attRate, new DeviceRgb(255, 250, 240)));
        
        document.add(summaryTable);

        // Charts
        document.add(new Paragraph("\nGrafik tahlillar:").setBold().setFontSize(14));
        
        // Attendance Pie
        document.add(new Paragraph("Davomat taqsimoti").setTextAlignment(TextAlignment.CENTER).setMarginBottom(5).setMarginTop(10));
        Image attImg = convertToItextImage(attChart);
        attImg.setWidth(UnitValue.createPercentValue(90));
        attImg.setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.CENTER);
        document.add(attImg);

        // Performance Line
        document.add(new Paragraph("\nO'zlashtirish grafigi").setTextAlignment(TextAlignment.CENTER).setMarginBottom(5).setMarginTop(15));
        Image perfImg = convertToItextImage(perfChart);
        perfImg.setWidth(UnitValue.createPercentValue(90));
        perfImg.setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.CENTER);
        document.add(perfImg);

        // Detailed Table
        document.add(new Paragraph("\nDarslar bo'yicha batafsil:").setBold().setFontSize(14));
        Table detailsTable = new Table(UnitValue.createPercentArray(new float[]{1.5f, 1.2f, 1.2f, 1}));
        detailsTable.setWidth(UnitValue.createPercentValue(100));

        detailsTable.addHeaderCell(createHeaderCell("Sana"));
        detailsTable.addHeaderCell(createHeaderCell("Holati"));
        detailsTable.addHeaderCell(createHeaderCell("Topshiriq"));
        detailsTable.addHeaderCell(createHeaderCell("Ball"));

        String previousDateTime = null;
        boolean isLightRow = false;
        DeviceRgb lightGray = new DeviceRgb(225, 225, 225);
        DeviceRgb white = new DeviceRgb(255, 255, 255);
        
        for (ReportService.LessonScoreRow row : attendanceDetails) {
            String currentDateTime = row.date();
            String formattedDate = row.date();
            
            if (previousDateTime == null || !currentDateTime.equals(previousDateTime)) {
                isLightRow = !isLightRow;
                previousDateTime = currentDateTime;
            }
            
            DeviceRgb rowColor = isLightRow ? lightGray : white;
            
            detailsTable.addCell(new Cell().add(new Paragraph(formattedDate)).setFontSize(9).setBackgroundColor(rowColor));
            
            Cell statusCell = new Cell().add(new Paragraph(row.status())).setFontSize(9).setBackgroundColor(rowColor);
            if (row.status().equals("Kelgan")) statusCell.setFontColor(new DeviceRgb(56, 161, 105));
            else statusCell.setFontColor(new DeviceRgb(229, 62, 62));
            detailsTable.addCell(statusCell);
            
            detailsTable.addCell(new Cell().add(new Paragraph(row.scoreType())).setFontSize(9).setBackgroundColor(rowColor));
            String scoreText = row.score() == null ? "-" : String.format("%.1f", row.score());
            detailsTable.addCell(new Cell().add(new Paragraph(scoreText)).setFontSize(9).setBackgroundColor(rowColor));
        }

        document.add(detailsTable);
        document.close();
    }

    private static Cell createStatCell(String label, String value, DeviceRgb bgColor) {
        Cell cell = new Cell();
        cell.setBackgroundColor(bgColor);
        cell.setPadding(10);
        cell.add(new Paragraph(label).setFontSize(10).setFontColor(ColorConstants.GRAY));
        cell.add(new Paragraph(value).setFontSize(18).setBold());
        cell.setTextAlignment(TextAlignment.CENTER);
        return cell;
    }

    private static Image convertToItextImage(WritableImage writableImage) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(SwingFXUtils.fromFXImage(writableImage, null), "png", baos);
        return new Image(ImageDataFactory.create(baos.toByteArray()));
    }

    public static void exportCalendarSchedule(List<Schedule> schedules, LocalDate startDate, LocalDate endDate, File file) throws IOException {
        PdfWriter writer = new PdfWriter(file);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        Map<String, List<Schedule>> groupedSchedules = schedules.stream()
                .collect(Collectors.groupingBy(Schedule::getGroupName));

        boolean firstGroup = true;
        for (Map.Entry<String, List<Schedule>> entry : groupedSchedules.entrySet()) {
            if (!firstGroup) {
                document.add(new AreaBreak(AreaBreakType.NEXT_PAGE));
            }
            firstGroup = false;

            String groupName = entry.getKey();
            List<Schedule> groupSchedules = entry.getValue();

            // Group Title
            document.add(new Paragraph("Guruh: " + groupName)
                    .setFontSize(18)
                    .setBold()
                    .setMarginBottom(5));

            document.add(new Paragraph("Dars jadvali: " + startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + " - " +
                    endDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                    .setFontSize(12)
                    .setMarginBottom(15));

            // Iterate through months in the range
            LocalDate current = startDate.withDayOfMonth(1);
            while (!current.isAfter(endDate)) {
                addMonthCalendar(document, current, startDate, endDate, groupSchedules);
                current = current.plusMonths(1);
                if (!current.isAfter(endDate)) {
                    document.add(new Paragraph("\n"));
                }
            }
        }

        document.close();
    }

    private static void addMonthCalendar(Document document, LocalDate monthDate, LocalDate rangeStart, LocalDate rangeEnd, List<Schedule> groupSchedules) {
        String monthName = getMonthName(monthDate.getMonthValue());
        document.add(new Paragraph(monthName + " " + monthDate.getYear())
                .setBold()
                .setFontSize(14)
                .setMarginBottom(5));

        float[] columnWidths = {1, 1, 1, 1, 1, 1, 1};
        Table table = new Table(UnitValue.createPercentArray(columnWidths));
        table.setWidth(UnitValue.createPercentValue(100));

        // Weekday headers
        String[] weekdays = {"Du", "Se", "Ch", "Pa", "Ju", "Sh", "Ya"};
        for (String day : weekdays) {
            table.addHeaderCell(new Cell().add(new Paragraph(day))
                    .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBold());
        }

        LocalDate firstDayOfMonth = monthDate.withDayOfMonth(1);
        int dayOfWeekOfFirst = firstDayOfMonth.getDayOfWeek().getValue(); // 1 (Mon) to 7 (Sun)

        // Add empty cells for days before the first day of the month
        for (int i = 1; i < dayOfWeekOfFirst; i++) {
            table.addCell(new Cell());
        }

        int daysInMonth = monthDate.lengthOfMonth();
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = monthDate.withDayOfMonth(day);
            Cell cell = new Cell().setHeight(40);
            
            Paragraph p = new Paragraph(String.valueOf(day)).setFontSize(10);
            cell.add(p);

            // Check if this date is within the requested range and has a lesson
            if (!date.isBefore(rangeStart) && !date.isAfter(rangeEnd)) {
                int dow = date.getDayOfWeek().getValue();
                Schedule daySchedule = groupSchedules.stream()
                        .filter(s -> s.getDayOfWeek() == dow)
                        .findFirst()
                        .orElse(null);

                if (daySchedule != null) {
                    cell.setBackgroundColor(new DeviceRgb(255, 222, 173)); // NavajoWhite (Light Orange)
                    cell.add(new Paragraph(daySchedule.getLessonTime())
                            .setFontSize(8)
                            .setBold()
                            .setTextAlignment(TextAlignment.CENTER));
                }
            } else {
                // Day outside of range but in the same month
                cell.setBackgroundColor(ColorConstants.WHITE);
                cell.setOpacity(0.5f);
            }

            table.addCell(cell);
        }

        // Add empty cells to complete the last row
        int lastDayOfWeek = monthDate.withDayOfMonth(daysInMonth).getDayOfWeek().getValue();
        for (int i = lastDayOfWeek; i < 7; i++) {
            table.addCell(new Cell());
        }

        document.add(table);
    }

    private static String getMonthName(int month) {
        return switch (month) {
            case 1 -> "Yanvar";
            case 2 -> "Fevral";
            case 3 -> "Mart";
            case 4 -> "Aprel";
            case 5 -> "May";
            case 6 -> "Iyun";
            case 7 -> "Iyul";
            case 8 -> "Avgust";
            case 9 -> "Sentyabr";
            case 10 -> "Oktyabr";
            case 11 -> "Noyabr";
            case 12 -> "Dekabr";
            default -> "";
        };
    }

    public static void exportSchedule(List<Schedule> schedules, File file) throws IOException {
        PdfWriter writer = new PdfWriter(file);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        // Title
        Paragraph title = new Paragraph("Haftalik Dars Jadvali")
                .setFontSize(20)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
        document.add(title);

        // Table
        float[] columnWidths = {3, 2, 2};
        Table table = new Table(UnitValue.createPercentArray(columnWidths));
        table.setWidth(UnitValue.createPercentValue(100));

        // Headers
        table.addHeaderCell(createHeaderCell("Guruh nomi"));
        table.addHeaderCell(createHeaderCell("Hafta kuni"));
        table.addHeaderCell(createHeaderCell("Vaqt"));

        for (Schedule s : schedules) {
            table.addCell(new Cell().add(new Paragraph(s.getGroupName())));
            table.addCell(new Cell().add(new Paragraph(getDayName(s.getDayOfWeek()))));
            table.addCell(new Cell().add(new Paragraph(s.getLessonTime())));
        }

        document.add(table);
        document.close();
    }

    private static Cell createHeaderCell(String text) {
        return new Cell().add(new Paragraph(text))
                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER);
    }

    private static String getDayName(int day) {
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
}
