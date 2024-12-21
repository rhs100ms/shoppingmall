package com.dsapkl.backend.controller;

import com.dsapkl.backend.controller.dto.ItemSearchCondition;
import com.dsapkl.backend.repository.query.MainItemDto;
import com.dsapkl.backend.repository.query.MainItemQueryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.query.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Optional;

@Controller
@Slf4j
@RequiredArgsConstructor
public class HomeController {
    private final MainItemQueryRepository mainItemQueryRepository;


//    private final ItemService itemService;
//    private final ItemImageService itemImageService;
//    private final MainItemQueryRepository mainItemQueryRepository;
//    private final FileHandler fileHandler;

    @GetMapping("/")
    public String home2(@ModelAttribute Optional<Integer> page, ItemSearchCondition itemSearchCondition, Model model) {

        PageRequest pageRequest = PageRequest.of(page.orElseGet(() -> 0), 3);

        Page<MainItemDto> result = mainItemQueryRepository.findMainItem(pageRequest, itemSearchCondition);

        model.addAttribute("result", result);
        model.addAttribute("maxPage", 10);


        return "home";
}


}
