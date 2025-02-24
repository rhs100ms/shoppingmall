package com.dsapkl.backend.service;

import com.dsapkl.backend.dto.ItemServiceDTO;
import com.dsapkl.backend.entity.Category;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GoogleSheetsService {

    private static final String APPLICATION_NAME = "Google Sheets API";
    private static final String SPREADSHEET_ID = "1fwWSUKKbu7t4VhGFqiNXEWkECsy0_fgHyjn4WgfsqDo";
    private static final String CREDENTIALS_FILE_PATH = "C:/data/pkl-shop-b18126875648.json";

        private final String IMAGE_BASE_PATH = "C:/data/images/";

    private final ItemService itemService;

    private Sheets sheetsService;

    @PostConstruct
    public void init() throws IOException, GeneralSecurityException {
        //인증 설정
        GoogleCredentials credentials = GoogleCredentials.fromStream(
                new FileInputStream(CREDENTIALS_FILE_PATH))
                .createScoped(Collections.singleton(SheetsScopes.SPREADSHEETS));

        //sheets 서비스 생성
        sheetsService = new Sheets.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credentials))
                .setApplicationName(APPLICATION_NAME)
                .build();

    }

    // 시트 데이터 읽기
    public List<List<Object>> readSheet(String range) {
        try {
            ValueRange response = sheetsService.spreadsheets().values()
                    .get(SPREADSHEET_ID, range)
                    .execute();
            return response.getValues();
        } catch (IOException e) {
            throw new RuntimeException("구글 시트 읽기 실패", e);
        }
    }

    // 상품 데이터 가져오기
    public void importProductsFromSheet() {
        List<List<Object>> values = readSheet("Sheet1!A2:F");

        for (List<Object> row : values) {
            try {
                ItemServiceDTO form = new ItemServiceDTO();
                form.setName((String) row.get(0));
                form.setCategory(Category.valueOf((String) row.get(1)));
                form.setPrice(Integer.parseInt((String) row.get(2)));
                form.setStockQuantity(Integer.parseInt((String) row.get(3)));
                form.setDescription((String) row.get(4));

                //이미지 처리
                String[] imageNames = ((String) row.get(5)).split(",\\s*");
                List<MultipartFile> imageFiles = new ArrayList<>();
                List<String> extensions = Arrays.asList("png", "jpg", "jpeg", "webp", "gif", "bmp");

                for (String imageName : imageNames) {
                    imageName = imageName.trim();
                    File foundFile = null;

                    //이미지 파일 찾기
                    for (String ext : extensions) {
                        File file = new File( IMAGE_BASE_PATH + imageName + "." + ext);
                        if (file.exists()) {
                            foundFile = file;
                            break;
                        }
                    }

                    //이미지 파일 처리
                    if (foundFile != null) {
                        MultipartFile multipartFile = new MockMultipartFile(
                                foundFile.getName(),
                                foundFile.getName(),
                                Files.probeContentType(Paths.get(foundFile.getAbsolutePath())),
                                new FileInputStream(foundFile)
                        );
                        imageFiles.add(multipartFile);
                    } else {
                        System.out.println("이미지를 찾을 수 없음: " + imageName);
                    }
                }
                //DB에 저장
                itemService.saveItem(form, imageFiles);

            } catch (Exception e) {
                System.out.println("행 처리 중 오류 발생: " + row);
                e.printStackTrace();
            }
        }
    }
}
