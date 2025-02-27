package com.dsapkl.backend.service.sheets;

import com.dsapkl.backend.dto.CompareResult;
import com.dsapkl.backend.dto.ItemServiceDTO;
import com.dsapkl.backend.entity.CompareStatus;
import com.dsapkl.backend.service.ItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CompareService {

    private final GoogleSheetsService googleSheetsService;
    private final ItemService itemService;

    public CompareResult compareRowCounts() {

        // 1. 구글 시트 데이터 가져오기
        List<List<Object>> sheetData = googleSheetsService.readSheet("Sheet1!A2:H");
        int sheetRowCount = sheetData.size();

        // 2. DB 데이터 가져오기
        List<ItemServiceDTO> dbData = itemService.getAllItems();
        int dbRowCount = dbData.size();

        // 3. 행 수 비교 및 결과 반환
        CompareResult result = new CompareResult();

        if (sheetRowCount > dbRowCount) {
            result.setStatus(CompareStatus.SHEET_MORE);
            result.setDifference(sheetRowCount - dbRowCount);
        }
        else if (sheetRowCount < dbRowCount) {
            result.setStatus(CompareStatus.DB_MORE);
            result.setDifference(dbRowCount - sheetRowCount);
        } else {
            result.setStatus(CompareStatus.EQUAL);
            result.setDifference(0);
        }

        return result;
    }

}
