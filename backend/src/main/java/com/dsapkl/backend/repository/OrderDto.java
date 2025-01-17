package com.dsapkl.backend.repository;

import com.dsapkl.backend.entity.OrderStatus;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Locale;

@Data
@EqualsAndHashCode(of = "orderId")
public class OrderDto {
    private Long orderId;
    private int totalPrice;  //총 주문 가격
    private String orderDate;
    private OrderStatus orderStatus;
    private String paymentIntentId;
    private List<OrderItemDto> orderItemDtoList;


    @QueryProjection
    public OrderDto(Long orderId, int totalPrice, LocalDateTime orderDate, OrderStatus orderStatus, String paymentIntentId) {
        this.orderId = orderId;
        this.totalPrice = totalPrice;
        this.orderDate = orderDate.format(
            DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a", Locale.ENGLISH)
        );  // Format in English
        this.orderStatus = orderStatus;
        this.paymentIntentId = paymentIntentId;
    }
}
