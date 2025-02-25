package com.dsapkl.backend.restcontroller.admin;


import com.dsapkl.backend.service.sheets.AddService;
import com.dsapkl.backend.service.sheets.GoogleSheetsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/sheets")
@RequiredArgsConstructor
public class GoogleSheetsController {

    private final AddService addService;

    @PostMapping("/import")
    public ResponseEntity<String> importFromSheets() {
        addService.importNewProducts();
        return ResponseEntity.ok().body("Import successful");
    }

//    @PutMapping("/update")
//    public ResponseEntity<String> updateSheets() {
//        googleSheetsServiceSync
//    }
//
}
