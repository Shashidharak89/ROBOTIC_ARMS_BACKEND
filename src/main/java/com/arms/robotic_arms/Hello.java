package com.arms.robotic_arms;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController
public class Hello {
    @GetMapping("/")
    public String sayhello(){
        return "Hello";
    }
}
