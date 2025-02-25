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
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
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

}
