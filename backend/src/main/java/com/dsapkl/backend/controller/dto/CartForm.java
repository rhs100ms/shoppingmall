package com.dsapkl.backend.controller.dto;


import com.dsapkl.backend.entity.Item;
import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CartForm {

    private Long itemId;




    private int count;
}
