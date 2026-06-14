package com.cpt202.controller.common;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Routes browser entry points to the Vue single-page application.
 */
@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "forward:/index.html";
    }

    @GetMapping({"/login", "/register", "/forgot-password", "/reset-password", "/student/**", "/teacher/**", "/admin/**"})
    public String spa() {
        return "forward:/index.html";
    }

    @GetMapping("/auth/login")
    public String login() {
        return "redirect:/login";
    }

    @GetMapping("/auth/register")
    public String register() {
        return "redirect:/register";
    }

    @GetMapping("/auth/forgot-password")
    public String forgotPassword() {
        return "redirect:/login#reset";
    }

    @GetMapping("/auth/reset-password")
    public String resetPassword(@RequestParam(name = "token", required = false) String token) {
        if (token == null || token.isBlank()) {
            return "redirect:/login#reset";
        }
        return "redirect:/login?token=" + token + "#reset";
    }
}
