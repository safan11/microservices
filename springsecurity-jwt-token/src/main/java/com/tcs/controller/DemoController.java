package com.tcs.controller;

import org.springframework.web.bind.annotation.*;

@RestController
public class DemoController {

    @GetMapping("/user")
    public String userApi() {
        return "Hello USER – Access Granted";
    }

    @GetMapping("/admin")
    public String adminApi() {
        return "Hello ADMIN – Access Granted";
    }
}