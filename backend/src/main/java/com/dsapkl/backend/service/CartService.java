package com.dsapkl.backend.service;

import com.dsapkl.backend.entity.Cart;
import com.dsapkl.backend.entity.CartItem;
import com.dsapkl.backend.entity.Item;
import com.dsapkl.backend.entity.Member;
import com.dsapkl.backend.repository.CartItemRepository;
import com.dsapkl.backend.repository.CartRepository;
import com.dsapkl.backend.repository.ItemRepository;
import com.dsapkl.backend.repository.MemberRepository;
import com.dsapkl.backend.repository.query.CartQueryDto;
import com.dsapkl.backend.repository.query.CartQueryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CartService {
    private final CartRepository cartRepository;
    private final CartQueryRepository cartQueryRepository;
    private final MemberRepository memberRepository;
    private final ItemRepository itemRepository;
    private final CartItemRepository cartItemRepository;

    @Transactional(readOnly = true)
    public Cart findCart(Long memberId) {
        return cartRepository.findByMemberId(memberId).orElse(null);
    }

    @Transactional(readOnly = true)
    public CartItem findCartItem(Long cartItemId) {
        return cartItemRepository.findById(cartItemId).orElse(null);
    }

    /**
     * 장바구니 조회
     */
    @Transactional(readOnly = true)
    public List<CartQueryDto> findCartItems(Long memberId) {
        Cart cart = cartRepository.findByMemberId(memberId).orElseThrow(EntityNotFoundException::new);  // () -> new EntityNotFoundException()
        List<CartQueryDto> cartQueryDtos = cartQueryRepository.findCartQueryDtos(cart.getId());
        return cartQueryDtos;
    }


    /**
     * 장바구니 담기(추가)
     */
    public Long addCart(Long memberId, Long itemId, int count) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));
        
        Cart cart = cartRepository.findByMemberId(memberId)
            .orElseGet(() -> {
                Cart newCart = Cart.createCart(member);
                return cartRepository.save(newCart);
            });
        
        Item item = itemRepository.findById(itemId)
            .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다."));

        CartItem cartItem = cartItemRepository.findByCartIdAndItemId(cart.getId(), item.getId()).orElse(null);

        if (cartItem == null) {
            cartItem = CartItem.createCartItem(count, cart, item);
            cartItemRepository.save(cartItem);
        } else {
            cartItem.changeCount(count);
        }
        return cartItem.getId();
    }

    /**
     * 장바구니 삭제
     */
    public void deleteCartItem(Long itemId) {
        CartItem findCartItem = cartItemRepository.findById(itemId).orElse(null);
        if (findCartItem == null) {
            throw new IllegalArgumentException("Cart item not found with id: " + itemId);
        }
        cartItemRepository.delete(findCartItem);
    }

}
