package com.dsapkl.backend.service;

import com.dsapkl.backend.repository.*;
import com.dsapkl.backend.entity.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    private final ItemRepository itemRepository;
    private final MemberRepository memberRepository;
    private final OrderRepository orderRepository;

    public OrderService(ItemRepository itemRepository, MemberRepository memberRepository, OrderRepository orderRepository) {
        this.itemRepository = itemRepository;
        this.memberRepository = memberRepository;
        this.orderRepository = orderRepository;
    }

    /**
     * 단일 주문
     */
    public Long order(Long memberId, Long itemId, int count) {

        List<OrderItem> orderItemList = new ArrayList<>();
        Item findItem = itemRepository.findById(itemId).orElseGet(() -> null);
        Member findMember = memberRepository
                .findById(memberId).orElseGet(() -> null);
        int orderPrice = findItem.getPrice() * count;

        OrderItem orderItem = OrderItem.createOrderItem(count, orderPrice, findItem);
//        orderItemList.add(orderItem);

        OrderStatus orderStatus = OrderStatus.ORDER;

        Order order = Order.createOrder(orderStatus ,findMember, orderItem);

        Order save = orderRepository.save(order);
        return save.getId();

    }


    /**
     * 주문 목록 조회
     */
    @Transactional(readOnly = true)
    public List<OrderDto> findOrdersDetail(Long memberId, OrderStatus orderStatus) {
        return OrderRepository.findOrderDetail(memberId, orderStatus);
    }


}
