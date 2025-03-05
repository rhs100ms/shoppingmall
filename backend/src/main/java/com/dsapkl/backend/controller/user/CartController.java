package com.dsapkl.backend.controller.user;

import com.dsapkl.backend.config.AuthenticatedUser;
import com.dsapkl.backend.dto.CartForm;
import com.dsapkl.backend.dto.CartItemForm;
import com.dsapkl.backend.dto.CartOrderDto;
import com.dsapkl.backend.entity.Member;
import com.dsapkl.backend.entity.Order;
import com.dsapkl.backend.entity.OrderStatus;
import com.dsapkl.backend.repository.OrderDto;
import com.dsapkl.backend.repository.query.CartQueryDto;
import com.dsapkl.backend.service.CartService;
import com.dsapkl.backend.service.CheckoutService;
import com.dsapkl.backend.service.OrderService;
import com.dsapkl.backend.util.SessionUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
@Slf4j
public class CartController {

    private final CheckoutService checkoutService;
    private final CartService cartService;
    private final OrderService orderService;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @GetMapping("/cart")
    public String cartView(Model model, @AuthenticationPrincipal AuthenticatedUser user) throws JsonProcessingException {

        List<Order> order = orderService.findOrders(user.getId());
        long orderCount = order.stream().filter(o -> o.getStatus() == OrderStatus.ORDER).count();
        model.addAttribute("orderCount", orderCount);

        List<CartQueryDto> cartViews = checkoutService.cartViewDetails(null, model, user);
        model.addAttribute("checkoutCartItems", cartViews);

        List<CartQueryDto> cartItemListForm = cartService.findCartItems(user.getId());
        model.addAttribute("cartItemListForm", cartItemListForm);
        model.addAttribute("cartItemCount", cartItemListForm.size());

        model.addAttribute("frontendUrl", frontendUrl);

        return "cart/cartView";
    }

    @GetMapping("/cart/success")
    public String cartSuccessView(@RequestParam(required = false) String sessionId, Model model, @AuthenticationPrincipal AuthenticatedUser user) throws JsonProcessingException {
        List<CartQueryDto> cartViews = checkoutService.cartViewDetails(sessionId, model, user);
        model.addAttribute("cartItemListForm", cartViews);
        return "redirect:../cart";
    }

}
