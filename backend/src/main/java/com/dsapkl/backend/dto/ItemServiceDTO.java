package com.dsapkl.backend.dto;

import com.dsapkl.backend.entity.Category;
import com.dsapkl.backend.entity.Item;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ItemServiceDTO {

    private Long itemId;
    private String name;
    private int price;
    private int stockQuantity;
    private String description;
    private Category category;
    private List<MultipartFile> itemImages;
    private List<String> orderedStoreNames;
    private String showYn;


}
