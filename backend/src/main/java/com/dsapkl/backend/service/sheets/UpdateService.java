package com.dsapkl.backend.service.sheets;

import com.dsapkl.backend.dto.ItemServiceDTO;
import com.dsapkl.backend.entity.Category;
import com.dsapkl.backend.service.ItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.dsapkl.backend.entity.QItem.item;

@Service
@Slf4j
@RequiredArgsConstructor
public class UpdateService {

    private final GoogleSheetsService googleSheetsService;
    private final ItemService itemService;
    private final ImageService imageService;

    @Value("${google.sheets.data-range}")
    private String dataRange;

    public void compareColumns() throws IOException {
        // 1. 시트 데이터 → DTO 변환
        List<List<Object>> sheetData = googleSheetsService.readSheet(dataRange);
        List<ItemServiceDTO> sheetDTOs = sheetData.stream()
                .map(row -> {
                    ItemServiceDTO dto = new ItemServiceDTO();
                    dto.setItemId(Long.parseLong(row.get(0).toString()));
                    dto.setName((String) row.get(1));      // 시트 순서에 맞게
                    dto.setPrice(Integer.parseInt(row.get(3).toString()));
                    dto.setStockQuantity(Integer.parseInt(row.get(4).toString()) );
                    dto.setDescription((String) row.get(5));
                    dto.setCategory(Category.valueOf((String) row.get(2)));
                    String[] imageNames = row.get(6).toString().split(",\\s*");
                    List<MultipartFile> images = null;
                    try {
                        images = imageService.processImages(imageNames);
                        log.info("이미지 이름 목록: {}", images);
                        // 이미지 리스트의 상세 정보 출력
                        log.info("처리된 이미지들: {}", images.stream().map(img -> img.getOriginalFilename()).collect(Collectors.joining(",")));

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    dto.setItemImages(images);
                    dto.setShowYn((String) row.get(7));
                    return dto;
                })
                .collect(Collectors.toList());


        // 2. DB 데이터 → DTO 변환 (이미 ItemServiceDTO로 반환됨)
        List<ItemServiceDTO> dbDTOs = itemService.getAllItems();


        // 3. DTO끼리 비교 (같은 구조이므로 안전)
        for (int i = 0; i < sheetDTOs.size(); i++) {
            ItemServiceDTO sheetDTO = sheetDTOs.get(i);
            ItemServiceDTO dbDTO = dbDTOs.get(i);

            if (!Objects.equals(sheetDTO, dbDTO)) {
                // 2. 실제 업데이트 수행
                itemService.updateItemBySheets(dbDTO.getItemId(), sheetDTO);
            }
        }


    }

}
