package com.dsapkl.backend.service;

import com.dsapkl.backend.entity.Category;
import com.dsapkl.backend.entity.Item;
import com.dsapkl.backend.entity.ItemImage;
import com.dsapkl.backend.repository.ItemImageRepository;
import com.dsapkl.backend.repository.ItemRepository;
import com.dsapkl.backend.dto.ItemServiceDTO;
import com.dsapkl.backend.dto.ItemForm;
import com.dsapkl.backend.dto.ItemImageDto;
import com.dsapkl.backend.service.sheets.ImageService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ItemService {

    private final ItemRepository itemRepository;
    private final ItemImageRepository itemImageRepository;
    private final FileHandler filehandler;
    private final ItemImageService itemImageService;
    private final ImageService imageService;

    //상품 정보 저장
    public Long saveItem(ItemServiceDTO itemServiceDTO, List<MultipartFile> multipartFileList) throws IOException {
        Item item = Item.createItem(
                itemServiceDTO.getName(),
                itemServiceDTO.getPrice(),
                itemServiceDTO.getStockQuantity(),
                itemServiceDTO.getDescription(),
                itemServiceDTO.getCategory(),
                itemServiceDTO.getShowYn());

        List<ItemImage> itemImages = filehandler.storeImages(multipartFileList);

        //대표 상품 이미지 설정

        if (!itemImages.isEmpty()) {
            itemImages.get(0).isFirstImage("F");  // 첫 번째 이미지를 대표 이미지로 설정
        }

        for (ItemImage itemImage : itemImages) {
            item.addItemImage(itemImage);  // 연관 관계 설정
        }

        itemRepository.save(item);

        return item.getId();
    }

    //상품 정보 업데이트 (Dirty Checking, 변경감지)
    @Transactional
    public void updateItem(ItemServiceDTO itemServiceDTO, List<MultipartFile> multipartFileList) throws IOException {
        Item findItem = itemRepository.findById(itemServiceDTO.getItemId())
                .orElseThrow(() -> new IllegalArgumentException("Product does not exist."));

        findItem.updateItem(
                itemServiceDTO.getName(),
                itemServiceDTO.getPrice(),
                itemServiceDTO.getStockQuantity(),
                itemServiceDTO.getDescription(),
                itemServiceDTO.getCategory()
        );

        //상품 이미지를 수정(삭제, 추가) 하지 않으면 실행 x
        if(!multipartFileList.get(0).isEmpty()) {
            itemImageService.addItemImage(multipartFileList, findItem);
        }

        //대표 이미지 재설정
        List<ItemImage> itemImageList = itemImageRepository.findByItemIdAndDeleteYN(itemServiceDTO.getItemId(), "N");
        itemImageList.get(0).isFirstImage("F");
    }

    @Transactional
    public void updateItem(Long itemId, String name, int price, int stockQuantity, String description, Category category) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("The product does not exist."));

        item.updateItem(name, price, stockQuantity, description, category);
    }

    @Transactional
    public void updateItem(Long itemId, ItemServiceDTO itemServiceDTO) {
        // 1. 상품 조회
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("The product does not exist.: " + itemId));

        // 2. 기본 정보 업데이트
        item.updateItem(
                itemServiceDTO.getName(),
                itemServiceDTO.getPrice(),
                itemServiceDTO.getStockQuantity(),
                itemServiceDTO.getDescription(),
                itemServiceDTO.getCategory()
        );

        // 3. 이미지 처리
        if (itemServiceDTO.getItemImages() != null && !itemServiceDTO.getItemImages().isEmpty()) {
            // 3-1. 기존 이미지 soft delete 처리
            List<ItemImage> oldImages = itemImageRepository.findByItemIdAndDeleteYN(itemServiceDTO.getItemId(), "N");
            for (ItemImage oldImage : oldImages) {
                oldImage.deleteSet("Y");
            }

            // 3-2. 새 이미지 저장
            try {
                List<ItemImage> newImages = filehandler.storeImages(itemServiceDTO.getItemImages());

                // 첫 번째 이미지를 대표 이미지로 설정
                if (!newImages.isEmpty()) {
                    newImages.get(0).isFirstImage("F");
                }

                // 이미지와 상품 연결
                for (ItemImage newImage : newImages) {
                    newImage.deleteSet("N");
                    item.addItemImage(newImage);
                }
            } catch (IOException e) {
                throw new RuntimeException("이미지 저장 중 오류 발생", e);
            }
        }

    }

    @Transactional
    public void updateItemBySheets(Long itemId, ItemServiceDTO sheetProduct) throws IOException {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("The product does not exist.: " + itemId));

        if (!item.getName().equals(sheetProduct.getName())) {
            item.updateName(sheetProduct.getName());
        }

        if (item.getPrice() != sheetProduct.getPrice()) {
            item.updatePrice(sheetProduct.getPrice());
        }

        if (!item.getCategory().equals(sheetProduct.getCategory())) {
            item.updateCategory(sheetProduct.getCategory());
        }

        if (item.getStockQuantity() != sheetProduct.getStockQuantity()) {
            item.updateStockQuantity(sheetProduct.getStockQuantity());
        }

        if (!item.getDescription().equals(sheetProduct.getDescription())) {
            item.updateDescription(sheetProduct.getDescription());
        }

        if (!item.getShowYn().equals(sheetProduct.getShowYn())) {
            item.updateShowYn(sheetProduct.getShowYn());
        }

        // 1. 기존 이미지 가져오기
        List<ItemImage> allExistingImages = itemImageRepository.findByItemId(itemId);

        // 2. 시트의 이미지 이름 목록 (확장자 포함)
        List<String> sheetImageNames = sheetProduct.getItemImages().stream()
                .map(MultipartFile::getOriginalFilename)
                .collect(Collectors.toList());

        // 3. 이미지 상태 업데이트
        allExistingImages.forEach(img -> {
            if (sheetImageNames.contains(img.getOriginalName())) {
                // 시트에 있는 이미지는 복구 또는 유지
                img.deleteSet("N");
            } else {
                // 시트에 없는 이미지는 삭제 처리
                img.deleteSet("Y");
            }
        });

        // 4. 기존 이미지 이름 목록 (확장자 포함)
        List<String> allExistingImageNames = allExistingImages.stream()
                .map(ItemImage::getOriginalName)
                .collect(Collectors.toList());


        // 8 모든 이미지의 firstImage를 "N"으로 초기화
        allExistingImages.forEach(img -> img.isFirstImage("N"));

        // 8-1 시트의 첫번째 이미지를 firstImageName에 담는다.
        String firstImageName = sheetImageNames.get(0);

        // 8-2 첫 번째 이미지가 기존 이미지인 경우
        allExistingImages.stream()
                .filter(img -> img.getOriginalName().equals(firstImageName))
                .findFirst()
                .ifPresent(img -> img.isFirstImage("F"));

        // 6 Sheet에만 존재하는 새로운 이미지 추가 (기존에 없는 새 이미지)
        List<MultipartFile> newImages = sheetProduct.getItemImages().stream()
                .filter(file -> !allExistingImageNames.contains(file.getOriginalFilename()))
                .collect(Collectors.toList());

        if (!newImages.isEmpty()) {
            List<ItemImage> addedImages = filehandler.storeImages(newImages);
            for (ItemImage newImage : addedImages) {
                newImage.deleteSet("N");
                // 새로 추가되는 이미지가 시트의 첫 번째 이미지인 경우
                if (newImage.getOriginalName().equals(firstImageName)) {
                    newImage.isFirstImage("F");
                } else {
                    newImage.isFirstImage("N");
                }
                item.addItemImage(newImage);
            }
        }
    }

    // 통합 검색 기능
    @Transactional(readOnly = true)
    public List<Item> searchItems(String name, String categoryStr) {
        Category category = null;
        if (categoryStr != null && !categoryStr.isEmpty()) {
            try {
                category = Category.valueOf(categoryStr);
            } catch (IllegalArgumentException e) {
                // 잘못된 카테고리 문자열이 들어온 경우 전체 상품 반환
                return itemRepository.findAll();
            }
        }

        if (name != null && !name.isEmpty()) {
            if (category != null) {
                return itemRepository.findByCategoryAndNameContainingIgnoreCase(category, name);
            }
            return itemRepository.findByNameContainingIgnoreCase(name);
        }

        if (category != null) {
            return itemRepository.findByCategory(category);
        }

        return itemRepository.findAll();
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

    public Page<Item> findItemsPage(Pageable pageable) {
        return itemRepository.findAll(pageable);
    }

    public Page<Item> searchItems(String query, String categoryStr, String status, Pageable pageable) {
        final Category selectedCategory = (categoryStr != null && !categoryStr.isEmpty()) ? 
            Category.valueOf(categoryStr) : null;

        Specification<Item> spec = Specification.where(null);
        
        if (selectedCategory != null) {
            spec = spec.and((root, criteriaQuery, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("category"), selectedCategory)
            );
        }

        if (query != null && !query.isEmpty()) {
            spec = spec.and((root, criteriaQuery, criteriaBuilder) ->
                criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + query.toLowerCase() + "%"),
                    criteriaBuilder.like(root.get("id").as(String.class), "%" + query + "%")
                )
            );
        }

        if (status != null) {
            switch (status) {
                case "SELLING":
                    spec = spec.and((root, criteriaQuery, criteriaBuilder) ->
                        criteriaBuilder.greaterThan(root.get("stockQuantity"), 10));
                    break;
                case "LOW_STOCK":
                    spec = spec.and((root, criteriaQuery, criteriaBuilder) ->
                        criteriaBuilder.and(
                            criteriaBuilder.greaterThan(root.get("stockQuantity"), 0),
                            criteriaBuilder.lessThanOrEqualTo(root.get("stockQuantity"), 10)
                        ));
                    break;
                case "SOLDOUT":
                    spec = spec.and((root, criteriaQuery, criteriaBuilder) ->
                        criteriaBuilder.equal(root.get("stockQuantity"), 0));
                    break;
            }
        }

        return itemRepository.findAll(spec, pageable);
    }

    @Transactional(readOnly = true)
    public ItemForm getItemDtl(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(EntityNotFoundException::new);
        ItemForm itemForm = ItemForm.from(item);
        
        List<ItemImage> itemImages = itemImageRepository.findByItemIdAndDeleteYN(itemId, "N");
        List<ItemImageDto> itemImageDtos = itemImages.stream()
                .map(ItemImageDto::new)
                .collect(Collectors.toList());
        
        itemForm.setItemImageListDto(itemImageDtos);
        return itemForm;
    }
    
    //Id 가 가장 큰 값이 먼저 => latest 부터 4개
    public List<Item> findLatestItems(int limit) {
        return itemRepository.findAll(Sort.by(Sort.Direction.DESC, "id"))
                .stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Item> findAll() {
        return itemRepository.findAll();
    }

    public long count() {
        return itemRepository.count();  // JPA Repository의 기본 count() 메서드 사용
    }

    @Transactional
    public void saveItem(ItemServiceDTO sheetProduct) {
        try {
            // 1. Item 엔티티 생성
            Item item = Item.createItem(
                sheetProduct.getName(),
                sheetProduct.getPrice(),
                sheetProduct.getStockQuantity(),
                sheetProduct.getDescription(),
                sheetProduct.getCategory(),
                sheetProduct.getShowYn()
            );

            // 2. 이미지 처리
            if (sheetProduct.getItemImages() != null && !sheetProduct.getItemImages().isEmpty()) {
                List<ItemImage> itemImages = filehandler.storeImages(sheetProduct.getItemImages());

                // 첫 번째 이미지를 대표 이미지로 설정
                if (!itemImages.isEmpty()) {
                    itemImages.get(0).isFirstImage("F");
                }

                // 모든 이미지의 deleteYN을 'N'으로 설정하고 상품과 연결
                for (ItemImage itemImage : itemImages) {
                    itemImage.deleteSet("N");
                    item.addItemImage(itemImage);
                }
            }

            // 3. 상품 저장
            itemRepository.save(item);

        } catch (IOException e) {
            throw new RuntimeException("상품 저장 중 오류 발생: " + sheetProduct.getName(), e);
        }
    }

    @Transactional(readOnly = true)
    public List<ItemServiceDTO> getAllItems() {
        List<Item> items = itemRepository.findAll();

        return items.stream()
                .map(item -> {
                    ItemServiceDTO dto = new ItemServiceDTO();
                    dto.setItemId(item.getId());
                    dto.setName(item.getName());
                    dto.setPrice(item.getPrice());
                    dto.setStockQuantity(item.getStockQuantity());
                    dto.setDescription(item.getDescription());
                    dto.setCategory(item.getCategory());

                    List<ItemImage> itemImages = itemImageRepository.findByItemIdAndDeleteYN(item.getId(), "N");
                    List<MultipartFile> multipartFiles = itemImages.stream()
                            .map(img -> {
                                try {
                                    return imageService.convertToMultipartFile(img);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            })
                            .collect(Collectors.toList());
                    dto.setItemImages(multipartFiles);
                    dto.setShowYn(item.getShowYn());
                    return dto;
                })
                .collect(Collectors.toList());
    }
}
