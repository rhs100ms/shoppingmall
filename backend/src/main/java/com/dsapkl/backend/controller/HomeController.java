package com.dsapkl.backend.controller;

import com.dsapkl.backend.controller.dto.ItemSearchCondition;
import com.dsapkl.backend.entity.Item;
import com.dsapkl.backend.entity.Member;
import com.dsapkl.backend.entity.OrderStatus;
import com.dsapkl.backend.repository.OrderDto;
import com.dsapkl.backend.entity.Category;
import com.dsapkl.backend.repository.query.CartQueryDto;
import com.dsapkl.backend.repository.query.MainItemDto;
import com.dsapkl.backend.repository.query.MainItemQueryRepository;
import com.dsapkl.backend.service.CartService;
import com.dsapkl.backend.service.ItemService;
import com.dsapkl.backend.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

import static com.dsapkl.backend.controller.CartController.getMember;

@Controller
@Slf4j
@RequiredArgsConstructor
public class HomeController {
    private final ItemService itemService;
    private final CartService cartService;
    private final OrderService orderService;

    @GetMapping("/")
    public String home(@RequestParam(value = "query", required = false) String query,
                      @RequestParam(value = "orderStatus", required = false) OrderStatus orderStatus,
                      @RequestParam(value = "category", required = false) String category,
                      Model model, HttpServletRequest request) {

        List<Item> items = itemService.searchItems(query, category);

        model.addAttribute("items", items);
        HttpSession session = request.getSession(false);

        //비로그인 사용자
        if (session == null || session.getAttribute(SessionConst.LOGIN_MEMBER) == null) {
            return "home";
        }
        //로그인된 사용자
        Member member = getMember(request);
        List<CartQueryDto> cartItemListForm = cartService.findCartItems(member.getId());
        int cartItemCount = cartItemListForm.size();
        model.addAttribute("cartItemListForm", cartItemListForm);
        model.addAttribute("cartItemCount", cartItemCount);

        List<OrderDto> ordersDetail = orderService.findOrdersDetail(member.getId(), orderStatus);

        long orderCount = ordersDetail.stream()
                                      .filter(order -> order.getOrderStatus() == OrderStatus.ORDER)
                                      .count();

        model.addAttribute("orderCount", orderCount);

        return "index";
    }
}
