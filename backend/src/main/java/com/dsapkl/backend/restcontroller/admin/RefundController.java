package com.dsapkl.backend.restcontroller.admin;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Refund;
import com.stripe.param.RefundCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin/api")
public class RefundController {

    @Value("${stripe.secret.key}")
    private String secretKey;

    // 환불 처리
    @PostMapping("/refund")
    public Map<String, String> createRefund(@RequestParam String paymentIntentId) {
        Stripe.apiKey = secretKey;

        Map<String, String> response = new HashMap<>();
        try {
            RefundCreateParams params = RefundCreateParams.builder()
                    .setPaymentIntent(paymentIntentId)
                    .build();

            Refund refund = Refund.create(params);
            response.put("status", "success");
            response.put("refundId", refund.getId());
        } catch (StripeException e) {
            response.put("status", "failed");
            response.put("error", e.getMessage());
        }
        return response;
    }

}
