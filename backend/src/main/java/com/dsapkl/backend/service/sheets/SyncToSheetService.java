package com.dsapkl.backend.service.sheets;

import com.dsapkl.backend.dto.ItemServiceDTO;
import com.dsapkl.backend.service.ItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class SyncToSheetService {

    private final GoogleSheetsService sheetsService;
    private final ItemService itemService;

    @Value("${google.sheets.data-range}")
    private String dataRange;

    /**
     * DB의 모든 정보를 구글 시트에 동기화
     */
    public void syncDbToSheet() {
        log.info("DB에서 구글 시트로 데이터 동기화 시작");

        try {
            // 1. DB에서 모든 상품 정보 가져오기
            List<ItemServiceDTO> dbItems = itemService.getAllItems();

            // 2. 구글 시트에서 데이터 가져오기 (헤더 포함)
            List<List<Object>> sheetData = sheetsService.readSheet(dataRange);

            // 3. DB 데이터를 시트 형식으로 변환
            List<List<Object>> newSheetData = new ArrayList<>();

            for (ItemServiceDTO item : dbItems) {
                List<Object> row = new ArrayList<>();
                row.add(item.getItemId());
                row.add(item.getName());
                row.add(item.getCategory().toString());
                row.add(item.getPrice());
                row.add(item.getStockQuantity());
                row.add(item.getDescription());
                // 이미지 이름 처리
                String imageNames = item.getItemImages().stream()
                        .map(img -> img.getOriginalFilename().replaceAll("\\.(png|jpg|webp)$", ""))
                        .reduce((a, b) -> a + ", " + b)
                        .orElse("");
                row.add(imageNames);
                row.add(item.getShowYn());
                newSheetData.add(row);
            }
            // 4. 시트 전체 업데이트
            sheetsService.updateSheet(dataRange, newSheetData);
            log.info("DB 데이터가 구글 시트에 성공적으로 동기화됨 (총 {}개 항목)", dbItems.size());
        } catch (Exception e) {
            log.error("데이터 동기화 중 오류 발생", e);
            throw new RuntimeException("구글 시트로 데이터 동기화 실패",e);
        }
    }

}
