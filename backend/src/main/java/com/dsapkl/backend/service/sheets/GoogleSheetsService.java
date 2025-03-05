package com.dsapkl.backend.service.sheets;

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
import java.util.Collections;
import java.util.List;

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

    @Value("${common.path.image}")
    private String imageBasePath;


//    private static final String APPLICATION_NAME = "Google Sheets API";
//    private static final String SPREADSHEET_ID = "1fwWSUKKbu7t4VhGFqiNXEWkECsy0_fgHyjn4WgfsqDo";
//    private static final String CREDENTIALS_FILE_PATH = "C:/data/pkl-shop-b18126875648.json";
//
//        private final String IMAGE_BASE_PATH = "C:/data/images/";

    private final ItemService itemService;

    private Sheets sheetsService;

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


//    // 시트 데이터 업데이트
//    public void udpateSheet(String range, List<List<Object>> values) {
//        try {
//            log.info("Updating sheet data from range: {}", range);
//            ValueRange body = new ValueRange().setValues(values);
//
//            sheetsService.spreadsheets().values()
//                    .update(spreadsheetId, range, body)
//                    .setValueInputOption("RAW")
//                    .execute();
//
//            log.info("Successfully updated {} rows in sheet", values.size());
//        } catch (IOException e) {
//            log.error("Failed to update Google Sheet", e);
//            throw new RuntimeException("구글 시트 업데이트 실패", e);
//        }
//    }
//
//    // 특정 셀 업데이트
//    public void updateCell(String range, Object value) {
//        try {
//            log.info("Updating cell data from range: {}", range);
//            ValueRange body = new ValueRange()
//                    .setValues(List.of(List.of(value)));
//
//            sheetsService.spreadsheets().values()
//                    .update(spreadsheetId, range, body)
//                    .setValueInputOption("RAW")
//                    .execute();
//            log.info("Successfully updated Cell at {}", range);
//        } catch (IOException e) {
//            log.error("Failed to update Google Sheet", e);
//            throw new RuntimeException("구글 시트 셀 업데이트 실패", e);
//        }
//    }

    // 시트 데이터 업데이트
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
}
