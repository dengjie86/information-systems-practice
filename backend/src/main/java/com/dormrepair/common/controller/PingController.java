package com.dormrepair.common.controller;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ping")
public class PingController {

    @GetMapping
    public Map<String, Object> ping() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("message", "pong");
        result.put("service", "dorm-repair-backend");
        result.put("timestamp", LocalDateTime.now());
        return result;
    }
}

