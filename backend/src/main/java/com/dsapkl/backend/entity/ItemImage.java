package com.dsapkl.backend.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;

@Entity
@Getter
@Table(name = "item_image")
public class ItemImage {
    @Id
    @GeneratedValue
    @Column(name = "item_image_id")
    private Long id;
    private String originalName; //원본 파일명
    private String storeName; //서버에 저장될 경로명
    private String deleteYN; //이미지 파일 삭제 여부

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    @Builder
    private ItemImage(String originalName, String storeName, String deleteYN) {
        this.originalName = originalName;
        this.storeName = storeName;
        this.deleteYN = "N";
    }
    public void changeItem(Item item) {
        this.item = item;
    }

    public void deleteSet(String deleteYN) {
        this.deleteYN = deleteYN;
    }
}
