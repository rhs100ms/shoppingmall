package com.dsapkl.backend.restcontroller.admin;


import com.dsapkl.backend.dto.CompareResult;
import com.dsapkl.backend.entity.CompareStatus;
import com.dsapkl.backend.service.sheets.AddService;
import com.dsapkl.backend.service.sheets.CompareService;
import com.dsapkl.backend.service.sheets.GoogleSheetsService;
import com.dsapkl.backend.service.sheets.UpdateService;
import lombok.RequiredArgsConstructor;
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

    @PostMapping("/import")
    public ResponseEntity<String> importFromSheets() {
        addService.importNewProducts();
        return ResponseEntity.ok().body("Import successful");
    }

    @GetMapping("/compare")
    public ResponseEntity<CompareResult> compareSheets() {
        CompareResult result = compareService.compareRowCounts();
        return ResponseEntity.ok(result);
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateSheets() throws IOException {
        CompareResult result = compareService.compareRowCounts();
        if (result.getStatus() == CompareStatus.EQUAL) {
            // 행 수가 같을 때만 업데이트 실행
            updateService.compareColumns();
            return ResponseEntity.ok("Update successful");
        } else {
            return ResponseEntity.ok("Update failed");
        }
    }

}
