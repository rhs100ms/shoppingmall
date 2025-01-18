package com.dsapkl.backend.controller.user;

import com.dsapkl.backend.dto.CartForm;
import com.dsapkl.backend.dto.CartItemForm;
import com.dsapkl.backend.dto.CartOrderDto;
import com.dsapkl.backend.entity.Member;
import com.dsapkl.backend.repository.query.CartQueryDto;
import com.dsapkl.backend.service.CartService;
import com.dsapkl.backend.service.OrderService;
import com.dsapkl.backend.util.SessionUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
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

    private final CartService cartService;
    private final OrderService orderService;

    /**
     *  장바구니 조회
     */

    @GetMapping("/cart")
    public String cartView(Model model, HttpServletRequest request) throws JsonProcessingException {
        List<CartQueryDto> cartViews = cartViewDetails(null, model, request);
        model.addAttribute("cartItemListForm", cartViews);
        return "cart/cartView";
    }

    @GetMapping("/cart/success")
    public String cartSuccessView(@RequestParam(required = false) String sessionId, Model model, HttpServletRequest request) throws JsonProcessingException {
        List<CartQueryDto> cartViews = cartViewDetails(sessionId, model, request);
        model.addAttribute("cartItemListForm", cartViews);
        return "redirect:/cart";
    }

    private List<CartQueryDto> cartViewDetails(@RequestParam(required = false) String sessionId, Model model, HttpServletRequest request)
    throws JsonProcessingException {
        Stripe.apiKey = "sk_test_51QclmbPPwZvRdRPfWv7wXxklQBavqLzNsxg3hsnaErdkjaZSvWCncfJXaQ9yUbvxCaUPRfEMsp2GXGwvSd2QHcHn00XH6z4sld";

        Member member = SessionUtil.getMember(request);

        List<CartQueryDto> cartItemListForm = Collections.emptyList();

        if (sessionId != null) {
            try{
                Session session = Session.retrieve(sessionId);
                String jsessionId = request.getSession().getId();
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

                RestTemplate restTemplate = new RestTemplate();
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.add("Cookie", "JSESSIONID=" + jsessionId);

                HttpEntity<CartOrderDto> requestEntity = new HttpEntity<>(cartOrderDto, headers);
                ResponseEntity<String> response = restTemplate.postForEntity("http://localhost:8888/orders", requestEntity, String.class);
                System.out.println(response.getBody());
                cartItemListForm = cartService.findCartItems(member.getId());

            } catch (StripeException e) {
                e.printStackTrace();
                model.addAttribute("error", "결제 정보를 불러오는 데 실패했습니다.");
            }
        } else {
            cartItemListForm = cartService.findCartItems(member.getId());
        }

        return cartItemListForm;
    }

}
