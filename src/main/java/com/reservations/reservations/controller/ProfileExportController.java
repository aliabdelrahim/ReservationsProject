package com.reservations.reservations.controller;

import com.reservations.reservations.model.Reservation;
import com.reservations.reservations.service.ReservationService;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpServletResponse;

import java.awt.*;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.time.format.DateTimeFormatter;
import java.util.List;

// --- PDF (OpenPDF) ---
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;

// --- Excel (Apache POI) ---
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;


@Controller
@PreAuthorize("isAuthenticated()")
public class ProfileExportController {

    private final ReservationService reservationService;
    public ProfileExportController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    private List<Reservation> my(Principal p) {
        return reservationService.myReservations(p.getName());
    }

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @GetMapping("/profile/exports/reservations.csv")
    public void exportCsv(HttpServletResponse resp, Principal p) throws Exception {
        resp.setContentType("text/csv; charset=UTF-8");
        resp.setHeader("Content-Disposition","attachment; filename=\"mes-reservations.csv\"");
        try (OutputStream os = resp.getOutputStream()) {
            // BOM pour Excel
            os.write(new byte[]{(byte)0xEF,(byte)0xBB,(byte)0xBF});
            os.write("ID;Spectacle;Date;Lieu;Places;Statut\n".getBytes(StandardCharsets.UTF_8));
            for (Reservation r : my(p)) {
                String show = r.getRepresentation().getShow().getTitle();
                String date = r.getRepresentation().getWhen().format(FMT);
                String lieu = r.getRepresentation().getLocation()!=null
                        ? r.getRepresentation().getLocation().getDesignation() : "";
                String line = String.format("%d;%s;%s;%s;%d;%s\n",
                        r.getId(), escape(show), date, escape(lieu), r.getPlaces(), r.getStatus().name());
                os.write(line.getBytes(StandardCharsets.UTF_8));
            }
        }
    }
    private String escape(String s){ return s==null? "" : s.replace(";", ","); }

    @GetMapping("/profile/exports/reservations.xlsx")
    public void exportXlsx(HttpServletResponse resp, Principal p) throws Exception {
        resp.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        resp.setHeader("Content-Disposition","attachment; filename=\"mes-reservations.xlsx\"");
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Réservations");
            // styles
            CellStyle head = wb.createCellStyle();
            Font bold = wb.createFont(); bold.setBold(true); head.setFont(bold);
            int r = 0;
            Row H = sheet.createRow(r++);
            String[] cols = {"ID","Spectacle","Date","Lieu","Places","Statut"};
            for (int c=0;c<cols.length;c++){ Cell cell = H.createCell(c); cell.setCellValue(cols[c]); cell.setCellStyle(head); }
            for (Reservation it : my(p)) {
                Row row = sheet.createRow(r++);
                row.createCell(0).setCellValue(it.getId());
                row.createCell(1).setCellValue(it.getRepresentation().getShow().getTitle());
                row.createCell(2).setCellValue(it.getRepresentation().getWhen().format(FMT));
                row.createCell(3).setCellValue(it.getRepresentation().getLocation()!=null
                        ? it.getRepresentation().getLocation().getDesignation() : "");
                row.createCell(4).setCellValue(it.getPlaces());
                row.createCell(5).setCellValue(it.getStatus().name());
            }
            for (int c=0;c<cols.length;c++) sheet.autoSizeColumn(c);
            wb.write(resp.getOutputStream());
        }
    }

    @GetMapping("/profile/exports/reservations.pdf")
    public void exportPdf(HttpServletResponse resp, Principal p) throws Exception {
        resp.setContentType("application/pdf");
        resp.setHeader("Content-Disposition","attachment; filename=\"mes-reservations.pdf\"");
        Document doc = new Document(PageSize.A4.rotate(), 20, 20, 20, 20);
        PdfWriter.getInstance(doc, resp.getOutputStream());
        doc.open();

        Paragraph title = new Paragraph("Mes réservations");
        title.getFont().setStyle(java.awt.Font.BOLD);
        title.setAlignment(Element.ALIGN_LEFT);
        doc.add(title);
        doc.add(new Paragraph(" "));

        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{7, 30, 18, 25, 8, 12});

        addHeader(table,"ID","Spectacle","Date","Lieu","Places","Statut");

        for (Reservation it : my(p)) {
            table.addCell(cell(String.valueOf(it.getId())));
            table.addCell(cell(it.getRepresentation().getShow().getTitle()));
            table.addCell(cell(it.getRepresentation().getWhen().format(FMT)));
            table.addCell(cell(it.getRepresentation().getLocation()!=null
                    ? it.getRepresentation().getLocation().getDesignation() : ""));
            table.addCell(cell(String.valueOf(it.getPlaces())));
            table.addCell(cell(it.getStatus().name()));
        }
        doc.add(table);
        doc.close();
    }

    private void addHeader(PdfPTable t, String... labels){
        for (String l: labels){
            PdfPCell c = new PdfPCell(new Phrase(l));
            c.setGrayFill(0.90f);
            t.addCell(c);
        }
    }
    private PdfPCell cell(String s){ return new PdfPCell(new Phrase(s==null? "":s)); }
}

