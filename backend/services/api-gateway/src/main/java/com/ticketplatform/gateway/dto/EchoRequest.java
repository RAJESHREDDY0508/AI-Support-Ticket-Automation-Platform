package com.ticketplatform.gateway.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EchoRequest(
        @NotBlank(message = "Message cannot be empty")
        @Size(max = 500)
        String message
) {}
