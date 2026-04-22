package com.dormrepair.common.controller;

import com.dormrepair.common.result.Result;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ping")
public class PingController {

    private final PasswordEncoder passwordEncoder;

    public PingController(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public Result<Map<String, Object>> ping() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("message", "pong");
        data.put("service", "dorm-repair-backend");
        data.put("timestamp", LocalDateTime.now());
        return Result.success(data);
    }

    // FIXME: 上线前删掉，仅开发调试用
    @GetMapping("/hash")
    public Result<String> hash(@RequestParam String raw) {
        return Result.success(passwordEncoder.encode(raw));
    }
}
