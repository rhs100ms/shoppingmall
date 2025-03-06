package com.dsapkl.backend.controller.admin;

import com.dsapkl.backend.config.AuthenticatedUser;
import com.dsapkl.backend.dto.*;
import com.dsapkl.backend.entity.*;
import com.dsapkl.backend.repository.query.CartQueryDto;
import com.dsapkl.backend.service.CartService;
import com.dsapkl.backend.service.ItemImageService;
import com.dsapkl.backend.service.ItemService;
import com.dsapkl.backend.service.sheets.GoogleSheetsService;
import com.dsapkl.backend.service.sheets.ImageService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/items")
@RequiredArgsConstructor
@Slf4j
public class AdminItemController {

    private final ItemService itemService;
    private final ItemImageService itemImageService;
    private final CartService cartService;
    private final GoogleSheetsService googleSheetsService;
    private final ImageService imageService;

    @Value("${google.sheets.data-range}")
    private String dataRange;

    @GetMapping("/new")
    public String createItemForm(Model model) {
        List<CategoryCode> categoryCode = new ArrayList<>();
        categoryCode.add(new CategoryCode("APPAREL", "Apparel"));
        categoryCode.add(new CategoryCode("ELECTRONICS", "Electronics"));
        categoryCode.add(new CategoryCode("BOOKS", "Books"));
        categoryCode.add(new CategoryCode("HOME_AND_KITCHEN", "Home & Kitchen"));
        categoryCode.add(new CategoryCode("HEALTH_AND_BEAUTY", "Health & Beauty"));
        model.addAttribute("categoryCode", categoryCode);
        model.addAttribute("itemForm", new ItemForm());
        return "item/itemForm";
    }

    @PostMapping("/new")
    public String createItem(@Valid @ModelAttribute ItemForm itemForm, BindingResult bindingResult, Model model,
                             @RequestParam("category") String category,
                             @RequestPart(name = "itemImages") List<MultipartFile> multipartFiles
    ) throws IOException {

        if (bindingResult.hasErrors()) {
            List<CategoryCode> categoryCode = new ArrayList<>();
            categoryCode.add(new CategoryCode("APPAREL", "Apparel"));
            categoryCode.add(new CategoryCode("ELECTRONICS", "Electronics"));
            categoryCode.add(new CategoryCode("BOOKS", "Books"));
            categoryCode.add(new CategoryCode("HOME_AND_KITCHEN", "Home & Kitchen"));
            categoryCode.add(new CategoryCode("HEALTH_AND_BEAUTY", "Health & Beauty"));
            model.addAttribute("categoryCode", categoryCode);
            return "item/itemForm";
        }

        //상품 이미지를 등록안하면
        if (multipartFiles.get(0).isEmpty()) {
            model.addAttribute("errorMessage", "Please upload product images!");
            return "item/itemForm";
        }

        //

        itemService.saveItem(itemForm.toServiceDTO(), multipartFiles);

        return "redirect:/admin";
    }

    /**
     * 상품 상세 조회
     */
    @GetMapping("/{itemId}")
    public String itemView(@PathVariable(name = "itemId") Long itemId, Model model, @AuthenticationPrincipal AuthenticatedUser user) {
        Item item = itemService.findItem(itemId);
        if (item == null) {
            return "redirect:/admin";
        }

        List<ItemImage> itemImageList = itemImageService.findItemImageDetail(itemId, "N");
        List<ItemImageDto> itemImageDtoList = itemImageList.stream()
                .map(ItemImageDto::new)
                .collect(Collectors.toList());

        ItemForm itemForm = ItemForm.from(item);
        itemForm.setItemImageListDto(itemImageDtoList);
        model.addAttribute("item", itemForm);
        log.info("itemForm : {}", itemForm);


        // 구글 시트 이미지 순서로 이미지 띄우기
        // 1. 시트 데이터 → DTO 변환
        List<List<Object>> sheetData = googleSheetsService.readSheet(dataRange);
        List<ItemServiceDTO> sheetDTOs = sheetData.stream()
                .map(row -> {
                    ItemServiceDTO dto = new ItemServiceDTO();
                    dto.setItemId(Long.parseLong(row.get(0).toString()));
                    dto.setName((String) row.get(1));      // 시트 순서에 맞게
                    dto.setPrice(Integer.parseInt(row.get(3).toString()));
                    dto.setStockQuantity(Integer.parseInt(row.get(4).toString()) );
                    dto.setDescription((String) row.get(5));
                    dto.setCategory(Category.valueOf((String) row.get(2)));
                    String[] imageNames = row.get(6).toString().split(",\\s*");
                    List<MultipartFile> images = null;
                    try {
                        images = imageService.processImages(imageNames, Category.valueOf((String) row.get(2)));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    dto.setItemImages(images);

                    return dto;
                })
                .collect(Collectors.toList());

        ItemServiceDTO matchedSheet = sheetDTOs.stream()
                .filter(dto -> dto.getItemId().equals(itemId))
                .findFirst()
                .orElse(null);

        Map<String, String> imageNameMap = itemImageList.stream()
                .collect(Collectors.toMap(
                        ItemImage::getOriginalName,
                        ItemImage::getStoreName
                ));

        List<String> orderedStoreNames = matchedSheet.getItemImages().stream()
                .map(MultipartFile::getOriginalFilename)
                .map(imageNameMap::get)
                .collect(Collectors.toList());

        matchedSheet.setOrderedStoreNames(orderedStoreNames);

        model.addAttribute("sheetImage", matchedSheet);
        log.info("matchedSheet : {}", matchedSheet);
        // 👇 추가 (실제 MultipartFile 내부 확인용)
        for (MultipartFile file : matchedSheet.getItemImages()) {
            log.info("파일명: {}", file.getOriginalFilename());
        }
        // 카트 숫자 // th:text="${cartItemCount}" 쓰기 위함


        if (user != null) {
            List<CartQueryDto> cartItemListForm = cartService.findCartItems(user.getId());
            int cartItemCount = cartItemListForm.size();
            model.addAttribute("cartItemListForm", cartItemListForm);
            model.addAttribute("cartItemCount", cartItemCount);
        }

        model.addAttribute("currentMemberId", user != null ? user.getId() : null);

        return "item/itemViewAdmin";
    }



    @PostMapping("/{itemId}/update-show-status")
    public String updateShowStatus(@PathVariable(name = "itemId") Long itemId,
                                   @RequestBody Map<String, String> body) {
        System.out.println(body);
        String showStatus = body.get("showYn");

        itemService.updateShowStatus(itemId, showStatus);
        return "redirect:/admin";
    }

    /**
     * 상품 삭제
     */
    @PostMapping("/{itemId}/delete")
    public String deleteItem(@PathVariable Long itemId) {
        itemService.deleteItem(itemId);
        return "redirect:/admin"; // 삭제 후 메인 페이지로 이동
    }


    @PostMapping("/{itemId}/edit")
    public String updateItem(@PathVariable("itemId") Long itemId,
                             @RequestParam("name") String name,
                             @RequestParam("price") int price,
                             @RequestParam("stockQuantity") int stockQuantity,
                             @RequestParam("description") String description,
                             @RequestParam("category") Category category,
                             @RequestParam(value = "deleteImages", required = false) List<Long> deleteImageIds,
                             @RequestParam(value = "itemImages", required = false) List<MultipartFile> itemImages,
                             HttpServletRequest request) throws IOException {
        // 상품 수정 로직
        itemService.updateItem(itemId, name, price, stockQuantity, description, category);

        // 선택된 이미지 삭제
        if (deleteImageIds != null && !deleteImageIds.isEmpty()) {
            for (Long imageId : deleteImageIds) {
                itemImageService.delete(imageId);
            }
        }

        // 이미지가 있다면 이미지도 수정
        if (itemImages != null && !itemImages.isEmpty() && !itemImages.get(0).isEmpty()) {
            log.info("itemImages : {}", itemImages);
            itemImageService.updateItemImages(itemId, itemImages);
        }

        // Referer 헤더를 통해 이전 페이지 URL 확인
        String referer = request.getHeader("Referer");
        // itemForm.html에서 온 요청인지 확인 (상품 관리 페이지에서의 수정)
        if (referer != null && referer.contains("/admin/items/" + itemId + "/edit")) {
            return "redirect:/admin/items/manage";
        } else {
            // itemView.html에서의 수정
            return "redirect:/admin/items/" + itemId;
        }
    }

    @GetMapping("/manage")
    public String manageItems(Model model,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "10") int size,
                              @RequestParam(required = false) String query,
                              @RequestParam(required = false) String category,
                              @RequestParam(required = false) String status
                              ) {

        // 전체 아이템 조회 (통계용)
        List<Item> allItems = itemService.findAll();

        // ItemStats 생성
        ItemStats itemStats = ItemStats.builder()
                .totalCount(itemService.count())
                .lowStockCount(allItems.stream()
                        .filter(item -> item.getStockQuantity() <= 10 && item.getStockQuantity() > 0)
                        .count())
                .onSaleCount(allItems.stream()
                        .filter(item -> item.getStockQuantity() > 0)
                        .count())
                .soldOutCount(allItems.stream()
                        .filter(item -> item.getStockQuantity() == 0)
                        .count())
                .build();

        // 검색 파라미터 유지를 위한 모델 속성 추가
        if (category != null && !category.trim().isEmpty()) {
            try {
                Category selectedCategory = Category.valueOf(category.toUpperCase());
                model.addAttribute("selectedCategory", selectedCategory);
            } catch (IllegalArgumentException e) {
                // 잘못된 카테고리 값은 무시
            }
        }

        if (status != null && !status.trim().isEmpty()) {
            ItemStatus selectedStatus = ItemStatus.valueOf(status.toUpperCase());
            model.addAttribute("selectedStatus", selectedStatus);
        }

        Page<Item> itemPage;
        if (query != null || category != null || status != null) {
            itemPage = itemService.searchItems(query, category, status, PageRequest.of(page, size));
        } else {
            itemPage = itemService.findItemsPage(PageRequest.of(page, size));
        }

        // ItemForm으로 변환
        List<ItemForm> itemForms = itemPage.getContent().stream()
                .map(item -> {
                    ItemForm form = ItemForm.from(item);
                    List<ItemImage> images = itemImageService.findItemImageDetail(item.getId(), "N");
                    form.setItemImageListDto(images.stream()
                            .map(ItemImageDto::new)
                            .collect(Collectors.toList()));
                    return form;
                })
                .collect(Collectors.toList());

        int start = Math.max(0, Math.min(itemPage.getNumber() - 2, itemPage.getTotalPages() - 5));
        int end = Math.min(itemPage.getTotalPages() -1 , Math.max(4, itemPage.getNumber() + 2));

        // 선택된 페이지 사이즈 설정
        PageSize selectedPageSize = PageSize.TEN; // 기본값
        for (PageSize pageSize : PageSize.values()) {
            if (pageSize.getValue() == size) {
                selectedPageSize = pageSize;
                break;
            }
        }
        model.addAttribute("selectedPageSize", selectedPageSize);

        // 모델에 데이터 추가
        model.addAttribute("items", itemForms);
        model.addAttribute("itemStats", itemStats);
        model.addAttribute("currentPage", itemPage.getNumber());
        model.addAttribute("totalPages", itemPage.getTotalPages());
        model.addAttribute("start", start);
        model.addAttribute("end", end);



        return "item/itemManage";
    }

    @GetMapping("/{itemId}/edit")
    public String itemEditForm(@PathVariable("itemId") Long itemId, Model model) {
        try {
            ItemForm itemForm = itemService.getItemDtl(itemId);
            List<CategoryCode> categoryCode = new ArrayList<>();
            categoryCode.add(new CategoryCode("APPAREL", "Apparel"));
            categoryCode.add(new CategoryCode("ELECTRONICS", "Electronics"));
            categoryCode.add(new CategoryCode("BOOKS", "Books"));
            categoryCode.add(new CategoryCode("HOME_AND_KITCHEN", "Home & Kitchen"));
            categoryCode.add(new CategoryCode("HEALTH_AND_BEAUTY", "Health & Beauty"));
            model.addAttribute("itemForm", itemForm);
            model.addAttribute("categoryCode", categoryCode);
            model.addAttribute("isEdit", true);  // 수정 모드임을 표시
            return "item/itemForm";
        } catch (EntityNotFoundException e) {
            model.addAttribute("errorMessage", "Product does not exist.");
            return "redirect:/admin/items/manage";
        }
    }

}
