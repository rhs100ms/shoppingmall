package com.dsapkl.backend.controller.user;

import com.dsapkl.backend.config.AuthenticatedUser;
import com.dsapkl.backend.dto.ItemForm;
import com.dsapkl.backend.dto.ItemImageDto;
import com.dsapkl.backend.entity.*;
import com.dsapkl.backend.repository.OrderDto;
import com.dsapkl.backend.repository.query.CartQueryDto;
import com.dsapkl.backend.service.CartService;
import com.dsapkl.backend.service.ItemImageService;
import com.dsapkl.backend.service.ItemService;
import com.dsapkl.backend.service.OrderService;
import com.dsapkl.backend.util.SessionUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserItemController {

    private final ItemService itemService;
    private final ItemImageService itemImageService;
    private final CartService cartService;
    private final OrderService orderService;


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
