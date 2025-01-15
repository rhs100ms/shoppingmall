package com.dsapkl.backend.repository;

import com.dsapkl.backend.entity.Category;
import com.dsapkl.backend.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long>, JpaSpecificationExecutor<Item> {


    // 검색 기능
    List<Item> findByNameContainingIgnoreCase(String name);

    List<Item> findByCategory(Category category);

    List<Item> findByCategoryAndNameContainingIgnoreCase(Category category, String name);
}
