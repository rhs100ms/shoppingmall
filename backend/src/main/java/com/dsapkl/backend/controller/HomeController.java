package com.dsapkl.backend.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

@Controller
@Slf4j
@RequiredArgsConstructor
public class HomeController {

//    private final ItemService itemService;
//    private final ItemImageService itemImageService;
//    private final MainItemQueryRepository mainItemQueryRepository;
//    private final FileHandler fileHandler;

    public String home2(Model model){
//        model.addAttribute("result", result);
//        model.addAttribute("maxPage",10);

        return "home";
    }

}
