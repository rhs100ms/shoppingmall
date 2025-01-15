package com.dsapkl.backend.dto;

import com.dsapkl.backend.entity.ReviewImage;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReviewImageDto {
    private Long id;
    private String originalFileName;
    private String storeFileName;

    public static ReviewImageDto from(ReviewImage reviewImage) {
        return ReviewImageDto.builder()
                .id(reviewImage.getId())
                .originalFileName(reviewImage.getOriginalFileName())
                .storeFileName(reviewImage.getStoreFileName())
                .build();
    }
} 