package com.dsapkl.backend.service;

import com.dsapkl.backend.controller.dto.CartForm;
import com.dsapkl.backend.controller.dto.CartOrderDto;
import com.dsapkl.backend.repository.*;
import com.dsapkl.backend.entity.*;
import com.stripe.exception.StripeException;
import com.stripe.model.LineItem;
import com.stripe.model.StripeCollection;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionListLineItemsParams;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class OrderService {

    private final ItemRepository itemRepository;
    private final MemberRepository memberRepository;
    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final OrderItemRepository orderItemRepository;

    public OrderService(ItemRepository itemRepository, MemberRepository memberRepository, OrderRepository orderRepository, CartService cartService, OrderItemRepository orderItemRepository) {
        this.itemRepository = itemRepository;
        this.memberRepository = memberRepository;
        this.orderRepository = orderRepository;
        this.cartService = cartService;
        this.orderItemRepository = orderItemRepository;
    }

    /**
     * 단일 주문
     */
    public Long order(Long memberId, Long itemId, int count, String paymentIntentId) {

        List<OrderItem> orderItemList = new ArrayList<>();
        Item findItem = itemRepository.findById(itemId).orElseGet(() -> null);

        Member findMember = memberRepository
                .findById(memberId).orElseGet(() -> null);
        int orderPrice = findItem.getPrice() * count;

        OrderItem orderItem = OrderItem.createOrderItem(count, orderPrice, findItem);
        orderItemList.add(orderItem);

        OrderStatus orderStatus = OrderStatus.ORDER;

        Order order = Order.createOrder(orderStatus , findMember, paymentIntentId, orderItemList);

        Order save = orderRepository.save(order);

        return save.getId();

    }


    /**
     * 주문 목록 조회
     */
    @Transactional(readOnly = true)
    public List<OrderDto> findOrdersDetail(Long memberId, OrderStatus orderStatus) {
        List<OrderDto> orders = orderRepository.findOrderDetail(memberId, orderStatus);
        return orders;
    }

    /**
     * 장바구니 상품들 주문
     */
    public Long orders(Long memberId, CartOrderDto cartOrderDto) {

        List<OrderItem> orderItemList = new ArrayList<>();

        Member findMember = memberRepository.findById(memberId)
            .orElseThrow(() -> new
                    NoSuchElementException("Member with ID " + memberId + " not found"));

        List<CartForm> cartOrderDtoList = cartOrderDto.getCartOrderDtoList();

        String paymentIntentId = null;

        for (CartForm cartForm : cartOrderDtoList) {
            Item findItem = itemRepository.findById(cartForm.getItemId()).orElse(null);
            int orderPrice = findItem.getPrice() * cartForm.getCount();

            OrderItem orderItem = OrderItem.createOrderItem(cartForm.getCount(), orderPrice, findItem);
            orderItemList.add(orderItem);

            paymentIntentId = cartForm.getPaymentIntentId();
        }

        OrderStatus orderStatus = OrderStatus.ORDER;

        Order order = Order.createOrder(orderStatus, findMember, paymentIntentId, orderItemList);

        Order save = orderRepository.save(order);

        //주문한 상품은 장바구니에서 제거
        deleteCartItem(cartOrderDto);

        return save.getId();

    }

    private void deleteCartItem(CartOrderDto cartOrderDto) {
        List<CartForm> cartOrderDtoList = cartOrderDto.getCartOrderDtoList();
        for (CartForm cartForm : cartOrderDtoList) {
            cartService.deleteCartItem(cartForm.getCartItemId());
        }
    }

    /*
     * 주문 취소
     */
    public void cancelOrder(Long orderId) {
        Order findOrder = orderRepository.findById(orderId).orElseGet(() -> null);
        findOrder.cancelOrder();
        orderRepository.flush();
    }

    public List<LineItem> retrieveLineItems(String sessionId) throws StripeException {

        Session session = Session.retrieve(sessionId);

        //Line Items 조회
        SessionListLineItemsParams params = SessionListLineItemsParams.builder().build();
        StripeCollection<LineItem> lineItemCollection = session.listLineItems(params);

        return lineItemCollection.getData();
    }

    public List<OrderDto> findOrderById(Long orderId) {

        Optional<Order> findOrder = Optional.ofNullable(orderRepository.findById(orderId).orElseGet(() -> null));

        return findOrder.map(order -> List.of(new OrderDto(order.getId(), order.getTotalPrice(),order.getOrderDate(),order.getStatus(), order.getPaymentIntentId())))
                .orElseGet(Collections::emptyList);
    }
}
