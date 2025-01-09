package com.dsapkl.backend.controller.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CartForm {

    private Long itemId;
    private Long cartItemId;
    private int count;

    public CartForm(Long itemId, Long cartItemId, int count) {
        this.itemId = itemId;
        this.cartItemId = cartItemId;
        this.count = count;
    }
}
