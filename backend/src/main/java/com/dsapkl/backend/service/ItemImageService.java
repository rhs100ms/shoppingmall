package com.dsapkl.backend.service;

import com.dsapkl.backend.entity.Item;
import com.dsapkl.backend.entity.ItemImage;
import com.dsapkl.backend.repository.ItemImageRepository;
import com.dsapkl.backend.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ItemImageService {

    private final ItemImageRepository itemImageRepository;
    private final FileHandler fileHandler;
    private final ItemRepository itemRepository;

    //삭제 여부를 판단하여 상품 이미지 정보를 조회한다
    @Transactional(readOnly = true)
    public List<ItemImage> findItemImageDetail(Long itemId, String YN) {
        return itemImageRepository.findByItemIdAndDeleteYN(itemId, YN);
    }

    @Transactional(readOnly = true)
    public List<ItemImage> findAllByDeleteYN(String YN) {
        return itemImageRepository.findAllByDeleteYN(YN);
    }

    //데이터베이스를 삭제하지 않고 flag를 이용하여 처리
    public void delete(Long itemImageId) {
        ItemImage itemImage = itemImageRepository.findById(itemImageId).get();
        itemImage.deleteSet("Y");

        //대표 상품 이미지 삭제
        itemImage.isFirstImage("N");
    }

    //상품 이미지 추가
    public void addItemImage(List<MultipartFile> multipartFiles, Item item) throws IOException {
        List<ItemImage> itemImages = fileHandler.storeImages(multipartFiles);

        for (ItemImage itemImage : itemImages) {
            item.addItemImage(itemImageRepository.save(itemImage));
        }
    }

    @Transactional
    public void updateItemImages(Long itemId, List<MultipartFile> itemImageFiles) throws IOException {
        // 기존 이미지 가져오기
        List<ItemImage> existingImages = itemImageRepository.findByItemId(itemId);
        // 기존 이미지 삭제 처리 (임시)
        for (ItemImage image : existingImages) {
            image.deleteSet("Y");
            image.setImageOrder(0);
            image.isFirstImage("N");
        }

        // 기존 이미지 랑 중복 확인
        Map<String, ItemImage> existingImageMap = existingImages.stream()
                .collect(Collectors.toMap(ItemImage::getOriginalName, image -> image));

        Item item =itemRepository.findById(itemId).orElseThrow();
        List<ItemImage> imagesToSave = new ArrayList<>();

        // 새로 받은 이미지 처리 (순서대로)
        for (int i = 0; i < itemImageFiles.size(); i++) {
            MultipartFile file = itemImageFiles.get(i);
            String originalFilename = file.getOriginalFilename();

            ItemImage image;

            // 이미 존재하는 이미지인지 확인
            if (existingImageMap.containsKey(originalFilename)) {
                // 기존 이미지 활용
                image = existingImageMap.get(originalFilename);
                image.deleteSet("N");

            } else {
                // 진짜 새로운 이미지 저장
                List<ItemImage> newImages = fileHandler.storeImages(List.of(file));
                image = newImages.get(0);
                image.changeItem(item);
                image.deleteSet("N");

            }
            // 이미지 순서 설정
            image.setImageOrder(i + 1);
            image.isFirstImage(i == 0 ? "F" : "N");
            imagesToSave.add(image);
        }

    itemImageRepository.saveAll(imagesToSave);

    }

    @Transactional(readOnly = true)
    public List<ItemImage> findByItemIdAndRepImgYn(Long itemId, String repImgYn) {
        return itemImageRepository.findByItemIdAndRepImgYn(itemId, repImgYn);
    }

    public void deleteItemImage(String storeName) throws IOException {
        fileHandler.deleteImage(storeName);
    }

    public List<ItemImage> uploadItemImages(List<MultipartFile> itemImageFiles) throws IOException {
        return fileHandler.storeImages(itemImageFiles);
    }

    public List<ItemImage> findItemImageDetailOrderByImageOrderAsc(Long itemId, String deleteYN) {
        return itemImageRepository.findByItemIdAndDeleteYNOrderByImageOrderAsc(itemId, deleteYN);
    }
}
