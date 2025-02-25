package com.dsapkl.backend.restcontroller.admin;


import com.dsapkl.backend.dto.CompareResult;
import com.dsapkl.backend.service.sheets.AddService;
import com.dsapkl.backend.service.sheets.CompareService;
import com.dsapkl.backend.service.sheets.GoogleSheetsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/sheets")
@RequiredArgsConstructor
public class GoogleSheetsController {

    private final AddService addService;
    private final CompareService compareService;

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

//    @PutMapping("/update")
//    public ResponseEntity<String> updateSheets() {
//        googleSheetsServiceSync
//    }
//
}
