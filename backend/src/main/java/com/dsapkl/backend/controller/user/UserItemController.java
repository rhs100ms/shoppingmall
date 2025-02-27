package com.dsapkl.backend.controller.user;

import com.dsapkl.backend.config.AuthenticatedUser;
import com.dsapkl.backend.dto.ItemForm;
import com.dsapkl.backend.dto.ItemImageDto;
import com.dsapkl.backend.dto.ItemServiceDTO;
import com.dsapkl.backend.entity.*;
import com.dsapkl.backend.repository.OrderDto;
import com.dsapkl.backend.repository.query.CartQueryDto;
import com.dsapkl.backend.service.CartService;
import com.dsapkl.backend.service.ItemImageService;
import com.dsapkl.backend.service.ItemService;
import com.dsapkl.backend.service.OrderService;
import com.dsapkl.backend.service.sheets.GoogleSheetsService;
import com.dsapkl.backend.service.sheets.ImageService;
import com.dsapkl.backend.util.SessionUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserItemController {

    private final ItemService itemService;
    private final ItemImageService itemImageService;
    private final CartService cartService;
    private final OrderService orderService;
    private final GoogleSheetsService googleSheetsService;
    private final ImageService imageService;


    /**
     * 상품 상세 조회
     */
    @GetMapping("items/{itemId}")
    public String itemView(@PathVariable(name = "itemId") Long itemId, Model model, @AuthenticationPrincipal AuthenticatedUser user) {

        Item item = itemService.findItem(itemId);

        if (item == null) {
            return "redirect:/user";
        }

        List<ItemImage> itemImageList = itemImageService.findItemImageDetail(itemId, "N");
        List<ItemImageDto> itemImageDtoList = itemImageList.stream()
                .map(ItemImageDto::new)
                .collect(Collectors.toList());

        ItemForm itemForm = ItemForm.from(item);
        itemForm.setItemImageListDto(itemImageDtoList);
        model.addAttribute("item", itemForm);

        // 구글 시트 이미지 순서로 이미지 띄우기
        // 1. 시트 데이터 → DTO 변환
        List<List<Object>> sheetData = googleSheetsService.readSheet("Sheet1!A2:G");
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
                        images = imageService.processImages(imageNames);
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


        List<CartQueryDto> cartItemListForm = cartService.findCartItems(user.getId());
        model.addAttribute("cartItemListForm", cartItemListForm);
        model.addAttribute("cartItemCount", cartItemListForm.size());
        model.addAttribute("currentMemberId", user.getId());

        List<Order> order = orderService.findOrders(user.getId());
        long orderCount = order.stream().filter(o -> o.getStatus() == OrderStatus.ORDER).count();
        model.addAttribute("orderCount", orderCount);

        return "item/itemViewUser";
    }


}
