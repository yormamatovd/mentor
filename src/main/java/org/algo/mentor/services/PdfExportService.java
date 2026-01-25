package org.algo.mentor.services;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.algo.mentor.models.Schedule;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class PdfExportService {

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
