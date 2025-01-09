package com.dsapkl.backend.service;

import com.dsapkl.backend.entity.Category;
import com.dsapkl.backend.entity.Item;
import com.dsapkl.backend.entity.ItemImage;
import com.dsapkl.backend.repository.ItemImageRepository;
import com.dsapkl.backend.repository.ItemRepository;
import com.dsapkl.backend.service.dto.ItemServiceDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ItemService {

    private final ItemRepository itemRepository;
    private final ItemImageRepository itemImageRepository;
    private final FileHandler filehandler;
    private final ItemImageService itemImageService;

    //상품 정보 저장
    public Long saveItem(ItemServiceDTO itemServiceDTO, List<MultipartFile> multipartFileList) throws IOException {
        Item item = Item.createItem(
                itemServiceDTO.getName(),
                itemServiceDTO.getPrice(),
                itemServiceDTO.getStockQuantity(),
                itemServiceDTO.getDescription(),
                itemServiceDTO.getCategory());

        List<ItemImage> itemImages = filehandler.storeImages(multipartFileList);

        //대표 상품 이미지 설정

        if (!itemImages.isEmpty()) {
            itemImages.get(0).isFirstImage("Y");  // 첫 번째 이미지를 대표 이미지로 설정
        }

        for (ItemImage itemImage : itemImages) {
            item.addItemImage(itemImage);  // 연관 관계 설정
        }

        itemRepository.save(item);

        return item.getId();
    }

    //상품 정보 업데이트 (Dirty Checking, 변경감지)
    public void updateItem(ItemServiceDTO itemServiceDTO,  List<MultipartFile> multipartFileList) throws IOException {

        Item findItem = itemRepository.findById(itemServiceDTO.getId()).orElse(null);  //DB에서 찾아옴 -> 영속 상태

        findItem.updateItem(itemServiceDTO.getName(), itemServiceDTO.getDescription(), itemServiceDTO.getPrice(), itemServiceDTO.getStockQuantity(), itemServiceDTO.getCategory());

        //상품 이미지를 수정(삭제, 추가) 하지 않으면 실행 x
        if(!multipartFileList.get(0).isEmpty()) {
            itemImageService.addItemImage(multipartFileList, findItem);
        }

        //대표 이미지 재설정
        List<ItemImage> itemImageList = itemImageRepository.findByItemIdAndDeleteYN(itemServiceDTO.getId(), "N");
        itemImageList.get(0).isFirstImage("Y");
    }

    @Transactional(readOnly=true)
    public List<Item> findItems() {
        return itemRepository.findAll();

    }

    // 검색 기능
    @Transactional(readOnly = true)
    public List<Item> searchItemsByNames(String name) {
        return itemRepository.findByNameContainingIgnoreCase(name);
    }
    // 카테고리 분류 기능
    @Transactional(readOnly = true)
    public List<Item> searchItemsByCategory(Category category) {
        return itemRepository.findByCategory(category);
    }
    // 카테고리 + 검색어 기반 상품 조회
    @Transactional(readOnly = true)
    public List<Item> searchItemsByCategoryAndName(Category category, String name) {
        return itemRepository.findByCategoryAndNameContainingIgnoreCase(category, name);
    }

    @Transactional(readOnly=true)
    public List<Item> findItemsPaging() {
        Page<Item> result = itemRepository.findAll(PageRequest.of(0, 3));
        return result.getContent();
    }


    @Transactional(readOnly = true)
    public Item findItem(Long ItemId) {

        return itemRepository.findById(ItemId).orElse(null);
    }

    /**
     * 상품 삭제
     */
    public void deleteItem(Long itemId) {
        // 삭제 전 존재 여부 확인
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new IllegalArgumentException("Item not found with ID: " + itemId));

        // 관련 이미지 삭제 처리
        List<ItemImage> itemImages = itemImageRepository.findByItemIdAndDeleteYN(itemId,"N");

        for (ItemImage image : itemImages) {
            filehandler.deleteImage(image.getStoreFileName()); // 실제 파일 삭제 (FileHandler 활용)
            itemImageRepository.delete(image); // DB에서 이미지 삭제
        }

        // 상품 삭제
        itemRepository.delete(item);
    }
}
