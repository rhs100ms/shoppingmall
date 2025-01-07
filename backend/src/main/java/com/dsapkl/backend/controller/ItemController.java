package com.dsapkl.backend.controller;

import com.dsapkl.backend.controller.dto.ItemForm;
import com.dsapkl.backend.controller.dto.ItemImageDto;
import com.dsapkl.backend.entity.Item;
import com.dsapkl.backend.entity.ItemImage;
import com.dsapkl.backend.entity.Member;
import com.dsapkl.backend.repository.query.CartQueryDto;
import com.dsapkl.backend.service.CartService;
import com.dsapkl.backend.service.ItemImageService;
import com.dsapkl.backend.service.ItemService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.dsapkl.backend.controller.CartController.getMember;

@Controller
//@Slf4j
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;
    private final ItemImageService itemImageService;
    private final CartService cartService;

//    APPAREL, ELECTRONICS, BOOKS, HOME_AND_KITCHEN, HEALTH_AND_BEAUTY

    @GetMapping("/items/new")
    public String createItemForm(Model model) {
        List<CategoryCode> categoryCode = new ArrayList<>();
        categoryCode.add(new CategoryCode("APPAREL", "의류"));
        categoryCode.add(new CategoryCode("ELECTRONICS", "전자제품"));
        categoryCode.add(new CategoryCode("BOOKS", "서적"));
        categoryCode.add(new CategoryCode("HOME_AND_KITCHEN", "가구&가전"));
        categoryCode.add(new CategoryCode("HEALTH_AND_BEAUTY", "건강&미용"));
        model.addAttribute("categoryCode", categoryCode);
        model.addAttribute("itemForm", new ItemForm());
        return "item/itemForm";
    }

    @Data
    @AllArgsConstructor
    static class CategoryCode {
        private String code;
        private String displayName;
    }

    @PostMapping("/items/new")
    public String createItem(@Valid @ModelAttribute ItemForm itemForm, BindingResult bindingResult, Model model,
                             @RequestParam("category") String category,
                             @RequestPart(name = "itemImages") List<MultipartFile> multipartFiles
    ) throws IOException {

        if (bindingResult.hasErrors()) {
            List<CategoryCode> categoryCode = new ArrayList<>();
            categoryCode.add(new CategoryCode("APPAREL", "의류"));
            categoryCode.add(new CategoryCode("ELECTRONICS", "전자제품"));
            categoryCode.add(new CategoryCode("BOOKS", "서적"));
            categoryCode.add(new CategoryCode("HOME_AND_KITCHEN", "가구&가전"));
            categoryCode.add(new CategoryCode("HEALTH_AND_BEAUTY", "건강&미용"));
            model.addAttribute("categoryCode", categoryCode);
            return "item/itemForm";
        }

        //상품 이미지를 등록안하면
        if (multipartFiles.get(0).isEmpty()) {
            model.addAttribute("errorMessage", "상품 사진을 등록해주세요!");
            return "item/itemForm";
        }

        //

        itemService.saveItem(itemForm.toServiceDTO(), multipartFiles);

        return "redirect:/";
    }

    /**
     * 상품 상세 조회
     */
    @GetMapping("items/{itemId}")
    public String itemView(@PathVariable(name = "itemId") Long itemId, Model model, HttpServletRequest request) {
        Item item = itemService.findItem(itemId);
        List<ItemImage> itemImageList = itemImageService.findItemImageDetail(itemId, "N");

        //엔티티 -> DTO
        List<ItemImageDto> itemImageDtoList = itemImageList.stream()
                .map(ItemImageDto::new)
                .collect(Collectors.toList());

        ItemForm itemform = new ItemForm(
                item.getId(),
                item.getName(),
                item.getCategory(),
                item.getPrice(),
                item.getStockQuantity(),
                item.getDescription(),
                itemImageDtoList
        );

        model.addAttribute("item", itemform);

        // 카트 숫자 // th:text="${cartItemCount}" 쓰기 위함


        Member member = getMember(request);
        if (member != null) {
            List<CartQueryDto> cartItemListForm = cartService.findCartItems(member.getId());
            int cartItemCount = cartItemListForm.size();
            model.addAttribute("cartItemListForm", cartItemListForm);
            model.addAttribute("cartItemCount", cartItemCount);
        }

        return "item/itemView";
    }

    /**
     * 상품 삭제
     */
    @PostMapping("/items/{itemId}/delete")
    public String deleteItem(@PathVariable Long itemId) {
        itemService.deleteItem(itemId);
        return "redirect:/"; // 삭제 후 메인 페이지로 이동


    }

}
