package com.dsapkl.backend.controller;

import com.dsapkl.backend.controller.dto.CartForm;
import com.dsapkl.backend.repository.OrderDto;
import com.dsapkl.backend.entity.Member;
import com.dsapkl.backend.entity.OrderStatus;
import com.dsapkl.backend.exception.NotEnoughStockException;
import com.dsapkl.backend.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

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
    public ResponseEntity<String> order(@ModelAttribute CartForm cartForm, HttpServletRequest request) {

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
    public String findOrder(OrderStatus status, Model model, HttpServletRequest request) {

        Member member = CartController.getMember(request);

        List<OrderDto> findOrders = orderService.findOrdersDetail(member.getId(), status);

        model.addAttribute("orderDetails", findOrders);
        return "orders/orderList";

    }


}
