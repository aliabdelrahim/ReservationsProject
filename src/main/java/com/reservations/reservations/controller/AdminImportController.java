package com.reservations.reservations.controller;

import com.reservations.reservations.service.ImportService;
import com.reservations.reservations.service.ImportService.ImportResult;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;

@Controller
@RequestMapping("/admin/import")
@PreAuthorize("hasRole('ADMIN')")
public class AdminImportController {

    private final ImportService importService;
    public AdminImportController(ImportService importService) { this.importService = importService; }

    @GetMapping
    public String page() {
        return "admin/import"; // templates/admin/import.html
    }

    @GetMapping("/representations/template.csv")
    public void template(HttpServletResponse resp) throws Exception {
        resp.setContentType("text/csv; charset=UTF-8");
        resp.setHeader("Content-Disposition","attachment; filename=\"template-representations.csv\"");
        String header = "title;when;location;capacity\n";
        String ex1 = "Hamlet;2025-10-15 20:00;Théâtre Royal;200\n";
        String ex2 = "Le Malade imaginaire;15/11/2025 19:30;Salle des Fêtes;150\n";
        resp.getOutputStream().write(new byte[]{(byte)0xEF,(byte)0xBB,(byte)0xBF}); // BOM
        resp.getOutputStream().write((header+ex1+ex2).getBytes(StandardCharsets.UTF_8));
    }

    @PostMapping("/representations")
    public String importReps(@RequestParam("file") MultipartFile file,
                             @RequestParam(value="dryRun", required=false, defaultValue="false") boolean dryRun,
                             Model model) {
        ImportResult r = importService.importRepresentations(file, dryRun);
        model.addAttribute("result", r);
        return "admin/import";
    }
}

