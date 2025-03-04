package com.dsapkl.backend.service;

import com.dsapkl.backend.config.AuthenticatedUser;
import com.dsapkl.backend.dto.CartForm;
import com.dsapkl.backend.dto.CartOrderDto;
import com.dsapkl.backend.dto.CheckoutRequest;
import com.dsapkl.backend.entity.OrderStatus;
import com.dsapkl.backend.repository.OrderDto;
import com.dsapkl.backend.repository.query.CartQueryDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CheckoutService {
    @Value("${stripe.secret.key}")
    private String secretKey;

    private final OrderService orderService;
    private final CartService cartService;

    // 단일 상품 주문 결제 로직
    public List<OrderDto> getOrderDetails(String sessionId, OrderStatus status, Model model, @AuthenticationPrincipal AuthenticatedUser user) {

        Stripe.apiKey = secretKey;
        List<OrderDto> findOrders = Collections.emptyList();

        if (sessionId != null) {
            try {
                // 1. Stripe 세션에서 정보 가져오기
                Session session = Session.retrieve(sessionId);
                String orderInfoJson = session.getMetadata().get("orderInfo");
                String paymentIntentId = session.getPaymentIntent();

                // 2. 주문 정보 파싱
                ObjectMapper objectMapper = new ObjectMapper();
                CheckoutRequest checkoutRequest = objectMapper.readValue(orderInfoJson, CheckoutRequest.class);
                model.addAttribute("checkoutRequest", checkoutRequest);

                // 3. RestTemplate 대신 직접 OrderService 호출
                try {
                    Long orderId = orderService.order(user.getId(), checkoutRequest.getItemId(), checkoutRequest.getCount(), paymentIntentId);
                    if (orderId != null) {
                        log.info("주문 성공:" + orderId );
                        findOrders = orderService.findOrdersDetail(user.getId(), status);
                    } else {
                        log.warn("주문 실패");
                    }
                } catch (Exception e) {
                    log.error("주문 처리 중 오류 발생", e);
                    model.addAttribute("error", "주문 처리 중 오류가 발생했습니다.");
                }

            } catch (StripeException | JsonProcessingException e) {
                log.error("결제 정보 처리 중 오류 발생", e);
                model.addAttribute("error", "결제 정보를 불러오는 데 실패했습니다.");
            }
        }   else {
            findOrders = orderService.findOrdersDetail(user.getId(), status);
        }
        return findOrders;
    }

    // 장바구니 결제 로직
    public List<CartQueryDto> cartViewDetails(@RequestParam(required = false) String sessionId, Model model, AuthenticatedUser user)
            throws JsonProcessingException {

        Stripe.apiKey = secretKey;

        if (user == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }

        List<CartQueryDto> cartItemListForm = Collections.emptyList();

        if (sessionId != null) {
            try{
                Session session = Session.retrieve(sessionId);
                String orderInfoJson = session.getMetadata().get("orderInfo");
                ObjectMapper objectMapper = new ObjectMapper();
                String paymentIntentId = session.getPaymentIntent();


                List<CartQueryDto> cartOrderList = objectMapper.readValue(orderInfoJson, new TypeReference<List<CartQueryDto>>() {});
                model.addAttribute("cartOrderList", cartOrderList);

                List<CartForm> cartFormList = cartOrderList.stream()
                        .map(cartQueryDto -> new CartForm(cartQueryDto.getCartItemId(), cartQueryDto.getItemId(), cartQueryDto.getCount(), paymentIntentId))
                        .collect(Collectors.toList());

                CartOrderDto cartOrderDto = new CartOrderDto();
                cartOrderDto.setCartOrderDtoList(cartFormList);

                try {
                    orderService.orders(user.getId(), cartOrderDto);
                    log.info("장바구니 주문 성공");
                    cartItemListForm = cartService.findCartItems(user.getId());
                } catch (Exception e) {
                    log.error("주문 처리 중 오류 발생", e);
                }

            } catch (StripeException e) {
                log.error("결제 정보 처리 중 오류 발생", e);
                model.addAttribute("error", "결제 정보를 불러오는 데 실패했습니다.");
            }
        } else {
            cartItemListForm = cartService.findCartItems(user.getId());
        }

        return cartItemListForm;
    }

}
