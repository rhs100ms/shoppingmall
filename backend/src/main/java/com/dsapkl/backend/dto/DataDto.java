package com.dsapkl.backend.dto;

import com.dsapkl.backend.repository.query.CartQueryDto;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DataDto {
    private List<CartQueryDto> cartQueryDto;

}
