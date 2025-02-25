package com.dsapkl.backend.service.sheets;

import com.dsapkl.backend.entity.ItemImage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ImageService {

    @Value("${common.path.image}")
    private String IMAGE_BASE_PATH;

    public List<MultipartFile> processImages(String[] imageNames) throws IOException {
        List<MultipartFile> imagesFiles = new ArrayList<>();
        List<String> extensions = Arrays.asList("png", "jpg", "webp");

        for (String imageName : imageNames) {
            imageName = imageName.trim();
            File foundFile = null;

            for (String ext : extensions) {
                File file = new File(IMAGE_BASE_PATH + imageName + "." + ext);
                if (file.exists()) {
                    foundFile = file;
                    break;
                }
            }

            if (foundFile != null) {
                MultipartFile multipartFile = new MockMultipartFile(
                        foundFile.getName(),
                        foundFile.getName(),
                        Files.probeContentType(Paths.get(foundFile.getAbsolutePath())),
                        new FileInputStream(foundFile)
                );
                imagesFiles.add(multipartFile);
            } else {
                log.warn("이미지를 찾을 수 없음: " + imageName);
            }
        }
        return imagesFiles;
    }

    public MultipartFile convertToMultipartFile(ItemImage itemImage) throws IOException {
        String fullPath = IMAGE_BASE_PATH + itemImage.getOriginalName();
        return new MockMultipartFile(

                itemImage.getOriginalName(),
                itemImage.getOriginalName(),
                Files.probeContentType(Paths.get(fullPath)),
                new FileInputStream(fullPath)
        );
    }
}
