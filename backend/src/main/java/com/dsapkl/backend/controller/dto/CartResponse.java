package com.dsapkl.backend.controller.dto;

import com.dsapkl.backend.repository.query.CartQueryDto;
import lombok.Getter;
import lombok.AllArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
public class CartResponse {
    private int count;
    private List<CartQueryDto> items;
} 