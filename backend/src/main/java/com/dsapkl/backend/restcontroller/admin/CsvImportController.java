package com.dsapkl.backend.restcontroller.admin;

import com.dsapkl.backend.service.CsvImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/csv")
@RequiredArgsConstructor
public class CsvImportController {

    private final CsvImportService csvImportService;

    @PostMapping("/import")
    public ResponseEntity<String> importCsv() {
        try {
            csvImportService.importProductsFromCsv("C:/data/products.csv");
            return ResponseEntity.ok("CSV import successful");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}
