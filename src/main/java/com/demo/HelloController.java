package com.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/hello")
    public String hello() {
        String env = System.getenv("ENV_NAME");
        return "Hello World from " + (env != null ? env : "local");
    }
}
