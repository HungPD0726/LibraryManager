package com.group6.librarymanager.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("title", "Library Manager System");
        model.addAttribute("message", "Chào mừng đến với hệ thống quản lý thư viện!");
        return "index";
    }
}
