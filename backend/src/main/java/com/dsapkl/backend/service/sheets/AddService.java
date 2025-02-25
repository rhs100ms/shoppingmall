package com.dsapkl.backend.service.sheets;

import com.dsapkl.backend.dto.ItemServiceDTO;
import com.dsapkl.backend.entity.Category;
import com.dsapkl.backend.service.ItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class AddService {

    private final ItemService itemService;
    private final GoogleSheetsService googleSheetsService;
    private final ImageExtensionService imageExtensionService;

    public void importNewProducts() {
        List<List<Object>> sheetValues = googleSheetsService.readSheet("Sheet1!A2:G");

        for (List<Object> row : sheetValues) {
            try {
                ItemServiceDTO newProduct = convertToDTO(row);
                itemService.saveItem(newProduct);
                log.info("새 상품 저장 완료: {}", newProduct.getName());
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
        List<MultipartFile> images = imageExtensionService.processImages(imageNames);
        newProduct.setItemImages(images);
        return newProduct;
    }

}
