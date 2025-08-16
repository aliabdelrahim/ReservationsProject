package com.reservations.reservations.service;

import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVParserBuilder;
import com.reservations.reservations.model.Location;
import com.reservations.reservations.model.Representation;
import com.reservations.reservations.model.Show;
import com.reservations.reservations.repository.LocationRepository;
import com.reservations.reservations.repository.RepresentationRepository;
import com.reservations.reservations.repository.ShowRepository;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.*;

@Service
public class ImportService {

    public static class ImportResult {
        public boolean dryRun;
        public int processed;
        public int createdShows;
        public int createdRepresentations;
        public int linkedExistingLocations;
        public List<String> errors = new ArrayList<>();
    }

    private final ShowRepository showRepo;
    private final LocationRepository locationRepo;
    private final RepresentationRepository repRepo;

    public ImportService(ShowRepository showRepo, LocationRepository locationRepo, RepresentationRepository repRepo) {
        this.showRepo = showRepo;
        this.locationRepo = locationRepo;
        this.repRepo = repRepo;
    }

    // Formats acceptés (avec et sans secondes, FR/US, ISO)
    private static final List<DateTimeFormatter> FMT = List.of(
            // ISO "2025-10-15T20:00" etc.
            DateTimeFormatter.ISO_LOCAL_DATE_TIME,

            // yyyy-MM-dd HH:mm[:ss]
            new DateTimeFormatterBuilder().parseCaseInsensitive()
                    .appendPattern("yyyy-MM-dd HH:mm")
                    .optionalStart().appendPattern(":ss").optionalEnd()
                    .toFormatter(Locale.ROOT),

            // dd/MM/yyyy HH:mm[:ss]
            new DateTimeFormatterBuilder().parseCaseInsensitive()
                    .appendPattern("dd/MM/uuuu HH:mm")
                    .optionalStart().appendPattern(":ss").optionalEnd()
                    .toFormatter(Locale.FRANCE),

            // dd-MM-yyyy HH:mm[:ss]
            new DateTimeFormatterBuilder().parseCaseInsensitive()
                    .appendPattern("dd-MM-uuuu HH:mm")
                    .optionalStart().appendPattern(":ss").optionalEnd()
                    .toFormatter(Locale.FRANCE),

            // yyyy/MM/dd HH:mm[:ss]
            new DateTimeFormatterBuilder().parseCaseInsensitive()
                    .appendPattern("uuuu/MM/dd HH:mm")
                    .optionalStart().appendPattern(":ss").optionalEnd()
                    .toFormatter(Locale.ROOT),

            // US Excel "M/d/yyyy h:mm a" (ex: 10/15/2025 8:00 PM)
            new DateTimeFormatterBuilder().parseCaseInsensitive()
                    .appendPattern("M/d/uuuu h:mm a")
                    .toFormatter(Locale.US)
    );

    @Transactional
    public ImportResult importRepresentations(MultipartFile file, boolean dryRun) {
        ImportResult r = new ImportResult();
        r.dryRun = dryRun;

        String name = Optional.ofNullable(file.getOriginalFilename()).orElse("").toLowerCase();
        List<String[]> rows;

        try {
            if (name.endsWith(".xlsx")) {
                rows = readXlsx(file.getInputStream());
            } else if (name.endsWith(".csv")) {
                rows = readCsv(file.getInputStream());
            } else {
                r.errors.add("Extension non supportée (utilise .csv ou .xlsx)");
                return r;
            }
        } catch (Exception e) {
            r.errors.add("Lecture du fichier impossible: " + e.getMessage());
            return r;
        }

        // Attend un header: title;when;location;capacity
        int lineNo = 0;
        for (String[] row : rows) {
            lineNo++;
            if (lineNo == 1 && isHeader(row)) continue;
            if (row.length < 4) { r.errors.add("Ligne "+lineNo+": 4 colonnes attendues"); continue; }

            String title = trim(row[0]);
            String whenStr = trim(row[1]);
            String locName = trim(row[2]);
            String capStr = trim(row[3]);

            if (title.isEmpty()) { r.errors.add("Ligne "+lineNo+": title manquant"); continue; }
            if (whenStr.isEmpty()) { r.errors.add("Ligne "+lineNo+": when manquant"); continue; }

            LocalDateTime when = parseDateTime(whenStr);
            if (when == null) { r.errors.add("Ligne "+lineNo+": when invalide (formats: yyyy-MM-dd HH:mm ou dd/MM/yyyy HH:mm)"); continue; }

            Integer capacity = null;
            try { capacity = Integer.valueOf(capStr); } catch (Exception ignore) {}
            if (capacity == null || capacity < 1) { r.errors.add("Ligne "+lineNo+": capacity invalide"); continue; }

            r.processed++;

            // Show
            Show show = showRepo.findByTitleIgnoreCase(title).orElse(null);
            if (show == null) {
                show = new Show();
                show.setTitle(title);
                // slug @PrePersist si tu as
                if (!dryRun) show = showRepo.save(show);
                r.createdShows++;
            }

            // Location (facultative)
            Location location = null;
            if (!locName.isBlank()) {
                location = locationRepo.findByDesignationIgnoreCase(locName).orElse(null);
                if (location != null) r.linkedExistingLocations++;
            }

            if (!dryRun) {
                Representation rep = new Representation(show, when, location);
                // si tu as un setter pour capacity (ou champ direct)
                try {
                    var f = Representation.class.getDeclaredField("capacity");
                    f.setAccessible(true);
                    f.set(rep, capacity);
                } catch (Exception ignored) {}
                repRepo.save(rep);
            }
            r.createdRepresentations++;
        }

        return r;
    }

    private List<String[]> readCsv(java.io.InputStream in) throws Exception {
        var parser = new CSVParserBuilder().withSeparator(';').build();
        try (var reader = new CSVReaderBuilder(new InputStreamReader(in, StandardCharsets.UTF_8))
                .withCSVParser(parser).build()) {
            List<String[]> rows = new ArrayList<>();
            for (String[] row; (row = reader.readNext()) != null; ) rows.add(row);
            return rows;
        }
    }

    private List<String[]> readXlsx(java.io.InputStream in) throws Exception {
        List<String[]> rows = new ArrayList<>();
        try (Workbook wb = WorkbookFactory.create(in)) {
            Sheet sheet = wb.getSheetAt(0);
            for (Row row : sheet) {
                List<String> line = new ArrayList<>();
                for (int c=0; c<4; c++) {
                    Cell cell = row.getCell(c);
                    String v;
                    if (cell == null) v = "";
                    else if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                        v = cell.getLocalDateTimeCellValue().toString().replace('T',' ');
                    } else {
                        cell.setCellType(CellType.STRING);
                        v = cell.getStringCellValue();
                    }
                    line.add(v != null ? v.trim() : "");
                }
                rows.add(line.toArray(String[]::new));
            }
        }
        return rows;
    }

    private String trim(String s){ return s==null? "": s.trim(); }
    private boolean isHeader(String[] row){
        if (row.length < 4) return false;
        String a=row[0].toLowerCase(), b=row[1].toLowerCase();
        return a.contains("title") && b.contains("when");
    }
    private LocalDateTime parseDateTime(String s){
        for (var f : FMT) {
            try { return LocalDateTime.parse(s, f); } catch (Exception ignore) {}
        }
        // Essai ISO '2025-10-15T20:00'
        try { return LocalDateTime.parse(s.replace('T',' '), FMT.get(0)); } catch (Exception ignore) {}
        return null;
    }
}

