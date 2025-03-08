package com.dsapkl.backend.service.sheets;

import com.dsapkl.backend.dto.ItemServiceDTO;
import com.dsapkl.backend.service.ItemService;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleSheetsService {

    @Value("${google.sheets.application-name}")
    private String applicationName;

    @Value("${google.sheets.spreadsheet-id}")
    private String spreadsheetId;

    @Value("${common.path.credentials}")
    private String credentialsFilePath;
    private Sheets sheetsService;

    @Value("${google.sheets.data-range}")
    private String dataRange;

    @PostConstruct
    public void init() throws IOException, GeneralSecurityException {
        try {
            log.info("Initializing Google Sheets service");
            log.info("Credentials file path: {}", credentialsFilePath);
            log.info("Spreadsheet ID: {}", spreadsheetId);
            //인증 설정
            GoogleCredentials credentials = GoogleCredentials.fromStream(
                            new FileInputStream(credentialsFilePath))
                    .createScoped(Collections.singleton(SheetsScopes.SPREADSHEETS));

            //sheets 서비스 생성
            sheetsService = new Sheets.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance(),
                    new HttpCredentialsAdapter(credentials))
                    .setApplicationName(applicationName)
                    .build();
            log.info("Google Sheets service initialized successfully");
        } catch (Exception e) {
            log.error("Failed to initialize Google Sheets service", e);
            throw e;
        }


    }

    // 시트 데이터 읽기
    public List<List<Object>> readSheet(String range) {
        try {
            log.info("Reading sheet data from range: {}", range);
            ValueRange response = sheetsService.spreadsheets().values()
                    .get(spreadsheetId, range)
                    .execute();
            log.info("Successfully read {} rows from sheet",
                    response.getValues() != null ? response.getValues().size() : 0);
            return response.getValues();
        } catch (IOException e) {
            log.error("Failed to read Google Sheet", e);
            throw new RuntimeException("구글 시트 읽기 실패", e);
        }
    }


    // DB 내용을 => 시트 데이터 업데이트
    public void updateSheet(String dataRange, List<List<Object>> newSheetData) {
        try {
            log.info("Updating sheet data from range: {}", dataRange);
            ValueRange body = new ValueRange().setValues(newSheetData);
            sheetsService.spreadsheets().values()
                    .update(spreadsheetId, dataRange, body)
                    .setValueInputOption("RAW")
                    .execute();
            log.info("Successfully updated {} rows in sheet", newSheetData.size());
        } catch (IOException e) {
            log.error("Failed to update Google Sheet", e);
            throw new RuntimeException("구글 시트 업데이트 실패" ,e);
        }
    }

    public void appendRow(ItemServiceDTO dbDTO) {
        List<Object> rowData = new ArrayList<>();
        rowData.add(dbDTO.getItemId());
        rowData.add(dbDTO.getName());
        rowData.add(dbDTO.getCategory().toString());
        rowData.add(dbDTO.getPrice());
        rowData.add(dbDTO.getStockQuantity());
        rowData.add(dbDTO.getDescription());

        // 이미지 파일명 추출 및 쉼표로 구분된 문자열로 변환
        String imageNames = dbDTO.getItemImages().stream()
                .map(file -> file.getOriginalFilename())
                .collect(Collectors.joining(", "));
        rowData.add(imageNames);
        rowData.add(dbDTO.getShowYn());

        appendValues(Arrays.asList(rowData));
    }

    private void appendValues(List<List<Object>> list) {
        try {
            ValueRange body = new ValueRange().setValues(list);
            sheetsService.spreadsheets().values()
                    .append(spreadsheetId, dataRange, body)
                    .setValueInputOption("RAW")
                    .execute();
        } catch (IOException e) {
            throw new RuntimeException("Failed to append values", e);
        }

    }


    public void updateCell(String cellRange, Object value) {
        try {
            ValueRange body = new ValueRange()
                    .setValues(Arrays.asList(Arrays.asList(value)));

            sheetsService.spreadsheets().values()
                    .update(spreadsheetId, cellRange, body)
                    .setValueInputOption("RAW")
                    .execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
