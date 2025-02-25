//package com.dsapkl.backend.service.sheets;
//
//import com.dsapkl.backend.dto.ItemServiceDTO;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//@Service
//@Slf4j
//@RequiredArgsConstructor
//public class SyncService {
//
//    private GoogleSheetsService googleSheetsService;
//
//    public void syncProductsFromSheet() {
//        List<List<Object>> sheetValues = googleSheetsService.readSheet("Sheet1!A2:G");
//
//        Map<Long, ItemServiceDTO> sheetProducts = convertToProductMap(sheetValues);
//
//
//
//    }
//
//    private Map<Long, ItemServiceDTO> convertToProductMap(List<List<Object>> values) {
//        Map<Long, ItemServiceDTO> products = new HashMap<>();
//
//        for (List<Object> row : values) {
//            Long productId = (Long) row.get(0);
//            String productName = (String) row.get(1);
//
//        }
//
//
//    }
//
//}
