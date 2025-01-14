package com.dsapkl.backend.controller;

import com.dsapkl.backend.controller.dto.CartForm;
import com.dsapkl.backend.controller.dto.CartOrderDto;
import com.dsapkl.backend.dto.CheckoutRequest;
import com.dsapkl.backend.repository.OrderDto;
import com.dsapkl.backend.entity.Member;
import com.dsapkl.backend.entity.OrderStatus;
import com.dsapkl.backend.exception.NotEnoughStockException;
import com.dsapkl.backend.service.OrderService;
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

import java.util.List;

@Controller
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * 단일 상품 바로 주문
     */
    @PostMapping("/order")
    @ResponseBody
    public ResponseEntity<String> order(@RequestBody CartForm cartForm, HttpServletRequest request) {

        //CartController 에 작성해둔 세션 정보 조회하는 기능 공용으로 사용
        Member member = CartController.getMember(request);

        if (member == null) {
            return new ResponseEntity<String>("로그인이 필요한 서비스입니다.", HttpStatus.UNAUTHORIZED);
        }

        try {
            orderService.order(member.getId(), cartForm.getItemId(), cartForm.getCount());
        } catch (NotEnoughStockException e) {
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

        return ResponseEntity.ok("order Success");
    }

    /**
     * 주문 내역 조회
     */
    @GetMapping("/orders")
    public String findOrder(@RequestParam(required = false) String sessionId, OrderStatus status, Model model, HttpServletRequest request) throws JsonProcessingException {

        Stripe.apiKey = "sk_test_51QclmbPPwZvRdRPfWv7wXxklQBavqLzNsxg3hsnaErdkjaZSvWCncfJXaQ9yUbvxCaUPRfEMsp2GXGwvSd2QHcHn00XH6z4sld";
        if (sessionId != null) {
            try{
                Session session = Session.retrieve(sessionId);
                String jsessionId = request.getSession().getId();
                String orderInfoJson = session.getMetadata().get("orderInfo");
                ObjectMapper objectMapper = new ObjectMapper();

                CheckoutRequest checkoutRequest = objectMapper.readValue(orderInfoJson, CheckoutRequest.class);
//                List<LineItem> lineItems = orderService.retrieveLineItems(sessionId);
//                lineItems.forEach(lineItem -> System.out.println("LineItem: " + lineItem));
//                cartOrderList.forEach(cartOrder -> System.out.println("CartOrderList: " + cartOrder));
                model.addAttribute("checkoutRequest", checkoutRequest);

                CartForm cartForm = new CartForm(checkoutRequest.getItemId(), checkoutRequest.getCount());
                model.addAttribute("cartForm", cartForm);

                //JSON 확인
                String jsonRequest = objectMapper.writeValueAsString(cartForm);
                System.out.println("Request JSON: " + jsonRequest);


                RestTemplate restTemplate = new RestTemplate();

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                headers.add("Cookie", "JSESSIONID=" + jsessionId);

                HttpEntity<CartForm> requestEntity = new HttpEntity<>(cartForm, headers);

                ResponseEntity<String> response = restTemplate.postForEntity("http://localhost:8888/order", requestEntity, String.class);
//                System.out.println("Response from orders: " + response.getBody());

            } catch (StripeException e) {
                e.printStackTrace();
                model.addAttribute("error", "결제 정보를 불러오는 데 실패했습니다.");
            }
        }

        Member member = CartController.getMember(request);

        List<OrderDto> findOrders = orderService.findOrdersDetail(member.getId(), status);

        model.addAttribute("orderDetails", findOrders);

        return "order/orderList";

    }

//    @GetMapping("/orders")
//    public String findOrder(OrderStatus status, Model model, HttpServletRequest request) {
//
//        Member member = CartController.getMember(request);
//
//        List<OrderDto> findOrders = orderService.findOrdersDetail(member.getId(), status);
//
//        model.addAttribute("orderDetails", findOrders);
//
//        return "order/orderList";
//
//    }


    /**
     * 장바구니 상품 주문
     */
    @PostMapping("/orders")
    @ResponseBody
    public ResponseEntity<String> orders(@RequestBody CartOrderDto cartOrderDto, HttpServletRequest request) {

        //장바구니에서 아무 상품도 체크하지 않을 경우
        if (cartOrderDto.getCartOrderDtoList().isEmpty()) {
            return new ResponseEntity<>("하나 이상의 상품을 주문하셔야 합니다.", HttpStatus.FORBIDDEN);
        }

        //CartController 에 작성해둔 세션 정보 조회하는 기능 공용으로 사용
        Member member = CartController.getMember(request);
        if (member == null) {
            return new ResponseEntity<>("로그인이 필요한 서비스입니다.", HttpStatus.UNAUTHORIZED);
        }

        try {
            orderService.orders(member.getId(), cartOrderDto);
        } catch (NotEnoughStockException e) {
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

        return ResponseEntity.ok("order Success");
    }

    /**
     * 주문 취소
     */
    @PostMapping("/order/{orderId}/cancel")
    @ResponseBody
    public ResponseEntity<String> cancelOrder(@PathVariable("orderId") Long orderId) {

        orderService.cancelOrder(orderId);

        return ResponseEntity.ok("Success");
    }
}
