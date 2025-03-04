package com.dsapkl.backend.restcontroller.admin;


import com.dsapkl.backend.dto.CompareResult;
import com.dsapkl.backend.entity.CompareStatus;
import com.dsapkl.backend.service.sheets.AddService;
import com.dsapkl.backend.service.sheets.CompareService;
import com.dsapkl.backend.service.sheets.GoogleSheetsService;
import com.dsapkl.backend.service.sheets.UpdateService;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/admin/sheets")
@RequiredArgsConstructor
public class GoogleSheetsController {

    private final AddService addService;
    private final CompareService compareService;
    private final UpdateService updateService;

    @PostMapping("/sync")
    public ResponseEntity<?> synchronizeSheets() {
        try {
            // 1. Compare rows
            CompareResult result = compareService.compareRowCounts();


            // 2. Process based on comparison result
            switch (result.getStatus()) {
                case SHEET_MORE:
                    addService.importNewProducts();
                    return ResponseEntity.ok("New products imported successfully");
                case EQUAL:
                    updateService.compareColumns();
                    return ResponseEntity.ok("Product updated successfully");
                case DB_MORE:
                    return ResponseEntity.badRequest().body("Database contains more records than the sheet. Manual review required.");
                default:
                    return ResponseEntity.internalServerError().body("Unknown comparison status" );
            }


        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Synchronization failed: " + e.getMessage());
        }
    }

}
