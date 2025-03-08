package com.dsapkl.backend.service.sheets;

import com.dsapkl.backend.dto.CellUpdate;
import com.dsapkl.backend.dto.ItemServiceDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class SheetUpdateService {

    private GoogleSheetsService googleSheetsService;

    public void compareDTO(ItemServiceDTO sheetDTO, ItemServiceDTO dbDTO, int rowIndex) {

        List<CellUpdate> updates = new ArrayList<>();

        if (!Objects.equals(sheetDTO.getName(), dbDTO.getName())) {
            updates.add(new CellUpdate(rowIndex, "B", dbDTO.getName()));
        }

        if (!Objects.equals(sheetDTO.getCategory(), dbDTO.getCategory())) {
            updates.add(new CellUpdate(rowIndex, "C", dbDTO.getCategory()));
        }

        if (sheetDTO.getPrice() != dbDTO.getPrice()) {
            updates.add(new CellUpdate(rowIndex, "D", dbDTO.getPrice()));
        }

        if (sheetDTO.getStockQuantity() != dbDTO.getStockQuantity()) {
            updates.add(new CellUpdate(rowIndex, "E", dbDTO.getStockQuantity()));
        }

        if (!Objects.equals(sheetDTO.getDescription(), dbDTO.getDescription())) {
            updates.add(new CellUpdate(rowIndex, "F", dbDTO.getDescription()));
        }

        List<String> sheetImages = sheetDTO.getItemImages().stream()
                .map(MultipartFile::getOriginalFilename)
                .collect(Collectors.toList());

        List<String> dbImages = dbDTO.getItemImages().stream()
                .map(MultipartFile::getOriginalFilename)
                .collect(Collectors.toList());

        if (!Objects.equals(sheetImages, dbImages)) {
            updates.add(new CellUpdate(rowIndex, "G", dbDTO.getItemImages()));
        }

        if (!Objects.equals(sheetDTO.getShowYn(), dbDTO.getShowYn())) {
            updates.add(new CellUpdate(rowIndex, "H", dbDTO.getShowYn()));
        }

        if (!updates.isEmpty()) {
            for (CellUpdate update : updates) {
                //셀 위치 계산
                String cellRange = update.getColumn() + update.getRow();
                googleSheetsService.updateCell(cellRange, update.getValue());
            }
        }

    }


}
