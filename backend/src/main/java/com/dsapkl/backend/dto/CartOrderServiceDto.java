package com.dsapkl.backend.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class CartOrderServiceDto {
    List<CartForm> cartOrderDtoList;
}
