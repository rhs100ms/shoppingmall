package com.dsapkl.backend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/admin")
    public String adminPage() {
        
        return "auth/admin";
    }

    @GetMapping("/user")
    public String userPage() {
        return "auth/user";
    }

//    @GetMapping("/home")
//    public String homePage() {
//        return "home";
//    }
}
