package com.dsapkl.backend.restcontroller.admin;


import com.dsapkl.backend.service.GoogleSheetsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/sheets")
@RequiredArgsConstructor
public class GoogleSheetsController {
    private final GoogleSheetsService googleSheetsService;

    @PostMapping("/import")
    public ResponseEntity<String> importFromSheets() {
        googleSheetsService.importProductsFromSheet();
        return ResponseEntity.ok().body("Import successful");
    }
}
