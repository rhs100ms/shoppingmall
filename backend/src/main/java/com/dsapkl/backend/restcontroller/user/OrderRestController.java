package com.dsapkl.backend.restcontroller.user;

import com.dsapkl.backend.config.AuthenticatedUser;
import com.dsapkl.backend.dto.CartForm;
import com.dsapkl.backend.dto.CartOrderDto;
import com.dsapkl.backend.entity.Member;
import com.dsapkl.backend.exception.NotEnoughStockException;
import com.dsapkl.backend.service.OrderService;
import com.dsapkl.backend.util.SessionUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class OrderRestController {

    private final OrderService orderService;

    /**
     * 주문 취소
     */
    @PostMapping("/order/{orderId}/cancel")
    public ResponseEntity<String> cancelOrder(@PathVariable("orderId") Long orderId) {
        orderService.cancelOrder(orderId);
        return ResponseEntity.ok("Success");
    }

}
