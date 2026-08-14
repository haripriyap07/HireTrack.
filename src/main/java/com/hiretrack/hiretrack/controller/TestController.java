package com.hiretrack.hiretrack.controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*")
@RestController
public class TestController {
    @GetMapping("/test")
    public String testApi() {
        return "HireTrack Backend is running 🚀";
    }
}
