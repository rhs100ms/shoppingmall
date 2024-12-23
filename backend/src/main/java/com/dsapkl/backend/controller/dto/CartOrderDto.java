package com.dsapkl.backend.controller.dto;


import java.util.List;

public class CartOrderDto {

    List<CartForm> cartOrderDtoList;

    public CartOrderServiceDto toServiceDto() {
        return CartOrderServiceDto.builder()
                .cartOrderDtoList(cartOrderDtoList)
                .build();
    }

}
