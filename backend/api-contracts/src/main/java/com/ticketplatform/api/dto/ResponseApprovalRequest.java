package com.ticketplatform.api.dto;

import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;

/**
 * Request body for PUT /tickets/{id}/response-approval (admin approves/edits AI response).
 */
public record ResponseApprovalRequest(
        @NotBlank(message = "Approved response is required")
        String approvedResponse
) implements Serializable {}
