package com.dsapkl.backend.service;

import com.dsapkl.backend.dto.ItemForm;
import com.dsapkl.backend.dto.ItemServiceDTO;
import com.dsapkl.backend.entity.Category;
import com.dsapkl.backend.entity.Item;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import lombok.RequiredArgsConstructor;
import org.codehaus.groovy.tools.shell.IO;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CsvImportService {

    private final ItemService itemService;
    private final FileHandler fileHandler;
    private final ItemImageService itemImageService;

    private final String CSV_PATH = "C:/data/products.csv";
    private final String IMAGE_BASE_PATH = "C:/data/images/"; // 기본 이미지 경로 추가
    private final String PROCESSED_ROWS_FILE = "C:/data/processed_rows.txt";
    private long lastModified = 0;

    public void importProductsFromCsv(String csvPath) {

        Set<String> processedRows = loadProcessedRows();

        try (CSVReader reader = new CSVReader(new FileReader(csvPath))) {
            String[] header = reader.readNext(); // 헤더 스킵
            String[] line;

            while ((line = reader.readNext()) != null) {
                String productName = line[0].trim();

                // 이미 실행된 행이면 건너뜀
                if (processedRows.contains(productName)) {
                    System.out.println("이미 실행된 행 건너뛰기: " + productName);
                    continue;
                }

                // CSV 데이터로 ItemForm 생성
                ItemServiceDTO form = new ItemServiceDTO();
                form.setName(line[0]);
                form.setCategory(Category.valueOf(line[1].toUpperCase()));
                form.setPrice(Integer.parseInt(line[2]));
                form.setStockQuantity(Integer.parseInt(line[3]));
                form.setDescription(line[4]);

                // 이미지 경로들을 파이프(|)로 분리
                String[] imageNames = line[5].split(",\\s*");

                List<MultipartFile> imageFiles = new ArrayList<>();
                List<String> extensions = Arrays.asList("png", "jpg", "jpeg", "webp", "gif", "bmp");

                for (String imageName : imageNames) {
                    imageName = imageName.trim();

                    File foundfile = null;
                    for (String ext : extensions) {
                        File file = new File(IMAGE_BASE_PATH + imageName + "." + ext);
                        if (file.exists()) {
                            foundfile = file;
                            break;
                        }
                    }

                    if (foundfile != null) {
                        MultipartFile multipartFile = new MockMultipartFile(
                                foundfile.getName(),
                                foundfile.getName(),
                                Files.probeContentType(Paths.get(foundfile.getAbsolutePath())),
                                new FileInputStream(foundfile)
                        );
                        imageFiles.add(multipartFile);
                    } else {
                        System.out.println("File not found");
                    }
                }
                // 상품 저장
                itemService.saveItem(form, imageFiles);

                saveProcessedRow(productName);
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (CsvValidationException e) {
            throw new RuntimeException(e);
        }
    }

    private Set<String> loadProcessedRows() {
        Set<String> processedRows = new HashSet<>();
        File file = new File(PROCESSED_ROWS_FILE);

        if (file.exists()) {
            try(BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while((line = br.readLine()) != null) {
                    processedRows.add(line.trim());
                }
            } catch (IOException e) {
                System.out.println("처리된 행을 불러오는 도중 오류 발생");
            }
        }
        return processedRows;
    }

    private void saveProcessedRow(String productName) {
        try(BufferedWriter bw = new BufferedWriter(new FileWriter(PROCESSED_ROWS_FILE, true))) {
            bw.write(productName);
            bw.newLine();
        } catch (IOException e) {
            System.out.println("처리된 행을 처리하는 중 오류 발생");
        }
    }

}
