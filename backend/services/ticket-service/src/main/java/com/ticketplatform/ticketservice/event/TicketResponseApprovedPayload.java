package com.ticketplatform.ticketservice.event;

import java.util.UUID;

public record TicketResponseApprovedPayload(
        UUID ticketId,
        String approvedResponse
) {}
