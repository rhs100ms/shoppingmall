package com.dsapkl.backend.controller.user;

import com.dsapkl.backend.dto.CartForm;
import com.dsapkl.backend.dto.CheckoutRequest;
import com.dsapkl.backend.repository.OrderDto;
import com.dsapkl.backend.entity.Member;
import com.dsapkl.backend.entity.OrderStatus;
import com.dsapkl.backend.service.OrderService;
import com.dsapkl.backend.util.SessionUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * 주문 내역 조회
     */
    @GetMapping("/orders")
    public String findOrder(OrderStatus status, Model model, HttpServletRequest request) {
        List<OrderDto> findOrders = getOrderDetails(null, status, model, request);
        model.addAttribute("orderDetails", findOrders);
        return "order/orderList";
    }

    @GetMapping("/orders/success")
    public String findOrderSuccess(@RequestParam(required = false) String sessionId, OrderStatus status, Model model, HttpServletRequest request) throws JsonProcessingException {
        List<OrderDto> findOrders = getOrderDetails(sessionId, status, model, request);
        model.addAttribute("orderDetails", findOrders);
        return "redirect:/orders";
    }

    private List<OrderDto> getOrderDetails(String sessionId, OrderStatus status, Model model, HttpServletRequest request) {
        Stripe.apiKey = "sk_test_51QclmbPPwZvRdRPfWv7wXxklQBavqLzNsxg3hsnaErdkjaZSvWCncfJXaQ9yUbvxCaUPRfEMsp2GXGwvSd2QHcHn00XH6z4sld";
        Member member = SessionUtil.getMember(request);
        List<OrderDto> findOrders = Collections.emptyList();
        if (sessionId != null) {
            try {
                Session session = Session.retrieve(sessionId);
                String jsessionId = request.getSession().getId();
                String orderInfoJson = session.getMetadata().get("orderInfo");
                ObjectMapper objectMapper = new ObjectMapper();

                String paymentIntentId = session.getPaymentIntent();
                CheckoutRequest checkoutRequest = objectMapper.readValue(orderInfoJson, CheckoutRequest.class);
                model.addAttribute("checkoutRequest", checkoutRequest);

                CartForm cartForm = new CartForm(checkoutRequest.getItemId(), checkoutRequest.getCount(), paymentIntentId);
                model.addAttribute("cartForm", cartForm);

                RestTemplate restTemplate = new RestTemplate();
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.add("Cookie", "JSESSIONID=" + jsessionId);

                HttpEntity<CartForm> requestEntity = new HttpEntity<>(cartForm, headers);
                ResponseEntity<Map> response = restTemplate.postForEntity("http://localhost:8888/order", requestEntity, Map.class);

                if (response.getStatusCode().is2xxSuccessful()) {
                    Map<String, Object> responseBody = response.getBody();

                    if ("success".equals(responseBody.get("status"))) {
                        System.out.println("Buy Now Success");  // 콘솔에 메시지 출력
                        findOrders = orderService.findOrdersDetail(member.getId(), status);
                    } else {
                        System.out.println("Buy Now Failed");
                    }
                } else {
                    System.out.println("Server error: " + response.getStatusCode());
                }

            } catch (StripeException | JsonProcessingException e) {
                e.printStackTrace();
                model.addAttribute("error", "결제 정보를 불러오는 데 실패했습니다.");
            }
        }   else {
            findOrders = orderService.findOrdersDetail(member.getId(), status);
        }
        return findOrders;
    }

}
