package com.dsapkl.backend.service.sheets;

import com.dsapkl.backend.dto.ItemServiceDTO;
import com.dsapkl.backend.entity.Category;
import com.dsapkl.backend.entity.Item;
import com.dsapkl.backend.repository.ItemRepository;
import com.dsapkl.backend.service.ItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class AddService {

    private final ItemService itemService;
    private final GoogleSheetsService googleSheetsService;
    private final ImageService imageService;
    private final ItemRepository itemRepository;
    private final UpdateService updateService;


    public void importNewProducts() throws IOException {
        List<List<Object>> sheetValues = googleSheetsService.readSheet("Sheet1!A2:H");

        // DB에서 현재 모든 아이템 ID 가져오기
        List<Long> existingItemIds = itemRepository.findAll().stream()
                .map(Item::getId)
                .collect(Collectors.toList());

        log.info("기존 DB 아이템 ID: {}", existingItemIds);

        // 시트의 아이템을 ID별로 맵에 저장
        Map<Long, List<Object>> sheetItemsMap = new HashMap<>();
        for (List<Object> row : sheetValues) {
            if (row.size() >= 1 && row.get(0) != null) {
                Long itemId = Long.parseLong(row.get(0).toString());
                sheetItemsMap.put(itemId, row);
            }
        }

        log.info("시트 아이템 ID: {}", sheetItemsMap.keySet());

        // 1. 기존 아이템 업데이트 (DB에 있고 시트에도 있는 아이템)
        for (Long existingId : existingItemIds) {
            if (sheetItemsMap.containsKey(existingId)) {
                try {
                    List<Object> row = sheetItemsMap.get(existingId);
                    ItemServiceDTO sheetDTO = convertToDTO(row);

                    // DB에서 현재 아이템 정보 가져오기
                    ItemServiceDTO dbDTO = itemService.getItemById(existingId);

                    // 변경사항이 있는 경우에만 업데이트
                    if (!Objects.equals(sheetDTO, dbDTO)) {
                        itemService.updateItemBySheets(existingId, sheetDTO);
                        log.info("기존 상품 업데이트 완료: ID={}, 이름={}", existingId, sheetDTO.getName());
                    } else {
                        log.info("상품 ID={} 변경사항 없음, 업데이트 건너뜀", existingId);
                    }

                    // 처리 완료된 아이템은 맵에서 제거
                    sheetItemsMap.remove(existingId);
                } catch (Exception e) {
                    log.error("상품 업데이트 중 오류 발생: ID={}", existingId, e);
                }
            }
        }

        // 2. 새 아이템 추가 (시트에만 있는 아이템)
        for (List<Object> row : sheetItemsMap.values()) {
            try {
                ItemServiceDTO newProduct = convertToDTO(row);
                itemService.saveItem(newProduct);
                log.info("새 상품 저장 완료: ID={}, 이름={}", newProduct.getItemId(), newProduct.getName());
            } catch (Exception e) {
                log.error("새 상품 저장 중 오류 발생: " + row, e);
            }
        }
    }

    private ItemServiceDTO convertToDTO(List<Object> row) throws IOException {
        ItemServiceDTO newProduct = new ItemServiceDTO();
        newProduct.setItemId(Long.parseLong(row.get(0).toString()));
        newProduct.setName(row.get(1).toString());
        String categoryStr = row.get(2).toString();
        newProduct.setCategory(Category.valueOf(categoryStr));
        newProduct.setPrice(Integer.parseInt(row.get(3).toString()));
        newProduct.setStockQuantity(Integer.parseInt(row.get(4).toString()));
        newProduct.setDescription(row.get(5).toString());
        String[] imageNames = row.get(6).toString().split(",\\s*");
        List<MultipartFile> images = imageService.processImages(imageNames);
        newProduct.setItemImages(images);
        newProduct.setShowYn(row.get(7).toString());
        return newProduct;
    }

}
