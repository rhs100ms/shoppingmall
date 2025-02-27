package com.dsapkl.backend.dto;

import com.dsapkl.backend.entity.ItemImage;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ItemImageDto {

    private Long id;
    private String originalName;
    private String storeName;
    private String deleteYN;
    private String firstImage; //썸네일 이미지 설정

    public ItemImageDto(ItemImage itemImage) {
        this.id = itemImage.getId();
        this.originalName = itemImage.getOriginalName();
        this.storeName = itemImage.getStoreName();
        this.deleteYN = itemImage.getDeleteYN();
        this.firstImage = itemImage.getFirstImage();
    }
}
