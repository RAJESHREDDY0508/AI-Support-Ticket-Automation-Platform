package com.ticketplatform.gateway.controller;

import com.ticketplatform.gateway.dto.EchoRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Sample endpoint demonstrating validation. Used for health/connectivity checks.
 */
@RestController
@RequestMapping("/api")
public class EchoController {

    @PostMapping("/echo")
    public ResponseEntity<Map<String, String>> echo(@Valid @RequestBody EchoRequest request) {
        return ResponseEntity.ok(Map.of("echo", request.message()));
    }
}
