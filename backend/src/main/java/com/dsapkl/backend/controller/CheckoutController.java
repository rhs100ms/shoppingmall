package com.dsapkl.backend.controller;

import com.dsapkl.backend.dto.CheckoutRequest;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.Refund;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.RefundCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class CheckoutController {

    @Value("${stripe.secret.key}")
    private String secretKey;

    @PostMapping("/checkout/create-checkout-session")
    public Map<String, String> createCheckoutSession(@RequestBody CheckoutRequest request) throws StripeException {
        // Stripe 비밀키 설정
        Stripe.apiKey = secretKey;

        // Line Item 설정 (결제할 상품 정보)
        SessionCreateParams.LineItem lineItem =
                SessionCreateParams.LineItem.builder()
                        .setPriceData(
                                SessionCreateParams.LineItem.PriceData.builder()
                                        .setCurrency("krw") // 통화 설정
                                        .setUnitAmount(request.getAmount()) // 금액 설정 (단위: cents)
                                        .setProductData(
                                                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                        .setName(request.getProductName()) // 상품명
                                                        .build()
                                        )
                                        .build()
                        )
                        .setQuantity(1L)
                        .build();

        // Checkout 세션 생성
        SessionCreateParams params =
                SessionCreateParams.builder()
                        .addLineItem(lineItem)
                        .setMode(SessionCreateParams.Mode.PAYMENT) // 결제 모드
                        .setSuccessUrl("http://localhost:8888/cart") // 성공 시 리다이렉트 URL
                        .setCancelUrl("http://localhost:8888/members") // 취소 시 리다이렉트 URL
                        .build();

        Session session = Session.create(params);

        // 클라이언트에 세션 ID 반환
        Map<String, String> responseData = new HashMap<>();
        responseData.put("sessionId", session.getId());
        return responseData;
    }

//    // 환불 처리
//    @PostMapping("/refund")
//    public Map<String, String> createRefund(@RequestParam String paymentIntentId) {
//        Stripe.apiKey = secretKey;
//
//        Map<String, String> response = new HashMap<>();
//        try {
//            RefundCreateParams params = RefundCreateParams.builder()
//                    .setPaymentIntent(paymentIntentId)
//                    .build();
//
//            Refund refund = Refund.create(params);
//            response.put("status", "success");
//            response.put("refundId", refund.getId());
//        } catch (StripeException e) {
//            response.put("status", "failed");
//            response.put("error", e.getMessage());
//        }
//        return response;
//    }

//    // Webhook 처리
//    @PostMapping("/webhook")
//    public ResponseEntity<String> handleWebhook(@RequestBody String payload,
//                                                @RequestHeader("Stripe-Signature") String sigHeader) {
//        String endpointSecret = "your_webhook_secret";
//        Event event;
//
//        try {
//            event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
//        } catch (SignatureVerificationException e) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
//        }
//
//        if ("payment_intent.payment_failed".equals(event.getType())) {
//            // 결제 실패 시 처리할 로직
//            System.out.println("Payment failed for intent: " + event.getId());
//        }
//
//        return ResponseEntity.ok("");
//    }

    @Value("${stripe.webhook.secret}")
    private String endpointSecret;

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(@RequestBody String payload,
                                                @RequestHeader("Stripe-Signature") String sigHeader) {
        Event event;

        try {
            event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
        } catch (SignatureVerificationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
        }

        if ("checkout.session.completed".equals(event.getType())) {
            // 결제 성공 이벤트 처리 로직
            Session session = (Session) event.getData().getObject();

            // 여기에 주문 정보를 DB에 저장하는 로직 추가
            System.out.println("결제가 완료되었습니다. 주문을 저장합니다.");
        }

        return ResponseEntity.ok("");
    }


}
