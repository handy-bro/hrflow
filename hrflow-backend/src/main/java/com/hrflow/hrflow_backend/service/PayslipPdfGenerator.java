package com.hrflow.hrflow_backend.service;

import com.hrflow.hrflow_backend.entity.Payslip;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;

@Component
public class PayslipPdfGenerator {

    public byte[] generate(Payslip payslip) {
        try {
            Document document = new Document(PageSize.A4, 40, 40, 50, 50);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
            Font headerFont = new Font(Font.HELVETICA, 11, Font.BOLD);
            Font normalFont = new Font(Font.HELVETICA, 10, Font.NORMAL);

            Paragraph title = new Paragraph("HRFlow - Payslip", titleFont);
            title.setSpacingAfter(4);
            document.add(title);

            String periodLabel = Month.of(payslip.getMonth()).getDisplayName(TextStyle.FULL, Locale.ENGLISH)
                    + " " + payslip.getYear();
            Paragraph period = new Paragraph("Period: " + periodLabel, normalFont);
            period.setSpacingAfter(20);
            document.add(period);

            document.add(new Paragraph("Employee: " + payslip.getEmployee().getFirstName()
                    + " " + payslip.getEmployee().getLastName(), normalFont));
            document.add(new Paragraph("Position: " + payslip.getEmployee().getPosition(), normalFont));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10);

            addRow(table, "Base Salary", formatAmount(payslip.getBaseSalary()), headerFont, normalFont);
            addRow(table, "Daily Rate", formatAmount(payslip.getDailyRate()), headerFont, normalFont);
            addRow(table, "Unjustified Absence Days", String.valueOf(payslip.getUnjustifiedAbsenceDays()),
                    headerFont, normalFont);
            addRow(table, "Deduction", "- " + formatAmount(payslip.getDeductionAmount()), headerFont, normalFont);

            document.add(table);
            document.add(new Paragraph(" "));

            Paragraph net = new Paragraph(
                    "Net Salary: " + formatAmount(payslip.getNetSalary()),
                    new Font(Font.HELVETICA, 13, Font.BOLD));
            document.add(net);

            document.add(new Paragraph(" "));
            document.add(new Paragraph(
                    "Generated on " + payslip.getGeneratedAt().toLocalDate(),
                    new Font(Font.HELVETICA, 8, Font.ITALIC, java.awt.Color.GRAY)));

            document.close();
            return out.toByteArray();

        } catch (DocumentException e) {
            throw new IllegalStateException("Failed to generate payslip PDF", e);
        }
    }

    private void addRow(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBorder(Rectangle.BOTTOM);
        labelCell.setPadding(6);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setBorder(Rectangle.BOTTOM);
        valueCell.setPadding(6);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);

        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    private String formatAmount(java.math.BigDecimal amount) {
        return amount.setScale(2, java.math.RoundingMode.HALF_UP) + " XAF";
    }
}