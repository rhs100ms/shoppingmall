package com.dsapkl.backend.restcontroller.admin;


import com.dsapkl.backend.dto.CompareResult;
import com.dsapkl.backend.entity.CompareStatus;
import com.dsapkl.backend.service.sheets.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/admin/sheets")
@RequiredArgsConstructor
@Slf4j
public class GoogleSheetsController {

    private final AddService addService;
    private final CompareService compareService;
    private final UpdateService updateService;
    private final SyncToSheetService syncToSheetService;

    @PostMapping("/sync")
    public ResponseEntity<?> synchronizeSheets() {
        try {
            // 1. Compare rows
            CompareResult result = compareService.compareRowCounts();
            // 2. Process based on comparison result
            switch (result.getStatus()) {
                case SHEET_MORE:
                    addService.importNewProducts();
                    return ResponseEntity.ok("Updated & New products imported successfully");
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

    @PostMapping("/sync-to-sheet")
    public ResponseEntity<?> syncDbToSheet() {
        try {
            // 1. Compare rows
            CompareResult result = compareService.compareRowCounts();
            // 2. Process based on comparison result
            switch (result.getStatus()) {
                case DB_MORE:
                    addService.importNewProductsFromDb();
                    return ResponseEntity.ok("Updated & New products imported successfully");
                case EQUAL:
                    updateService.compareColumnsFromDb();
                    return ResponseEntity.ok("Product updated successfully");
                case SHEET_MORE:
                    return ResponseEntity.badRequest().body("Sheet contains more records than the database. Manual review required.");
                default:
                    return ResponseEntity.internalServerError().body("Unknown comparison status" );
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Synchronization failed: " + e.getMessage());
        }
    }


//    @PostMapping("/sync-to-sheet")
//    public ResponseEntity<?> syncDbToSheet() {
//        try {
//            log.info("DB에서 시트로 데이터 동기화 요청 받음");
//            syncToSheetService.syncDbToSheet();
//            return ResponseEntity.ok("DB 데이터가 구글 시트에 성공적으로 동기화되었습니다.");
//        } catch (Exception e) {
//            log.error("데이터 동기화 실패", e);
//            return ResponseEntity.internalServerError().body("Sync failed: " + e.getMessage());
//        }
//    }



}
