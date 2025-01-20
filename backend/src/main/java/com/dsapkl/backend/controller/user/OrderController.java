package com.dsapkl.backend.controller.user;

import com.dsapkl.backend.config.AuthenticatedUser;
import com.dsapkl.backend.dto.CartForm;
import com.dsapkl.backend.dto.CheckoutRequest;
import com.dsapkl.backend.repository.OrderDto;
import com.dsapkl.backend.entity.Member;
import com.dsapkl.backend.entity.OrderStatus;
import com.dsapkl.backend.service.CheckoutService;
import com.dsapkl.backend.service.OrderService;
import com.dsapkl.backend.util.SessionUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class OrderController {

    private final CheckoutService checkoutService;
    private final OrderService orderService;

    @GetMapping("/orders")
    public String findOrder(OrderStatus status, Model model, @AuthenticationPrincipal AuthenticatedUser user) {

        List<OrderDto> findOrders = checkoutService.getOrderDetails(null, status, model, user);
        model.addAttribute("orderDetails", findOrders);

        List<OrderDto> orderDetail = orderService.findOrdersDetail(user.getId(), status);
        long orderCount = orderDetail.stream()
                .filter(order -> order.getOrderStatus() == OrderStatus.ORDER)
                .count();
        model.addAttribute("orderCount", orderCount);

        return "order/orderList";
    }

    @GetMapping("/orders/success")
    public String findOrderSuccess(@RequestParam(required = false) String sessionId, OrderStatus status, Model model, @AuthenticationPrincipal AuthenticatedUser user) throws JsonProcessingException {
        List<OrderDto> findOrders = checkoutService.getOrderDetails(sessionId, status, model, user);
        model.addAttribute("orderDetails", findOrders);
        return "redirect:../orders";
    }


}
