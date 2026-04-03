package com.zorvyn.finance.shared.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * Base class for domain events published via Spring ApplicationEventPublisher.
 * All domain events carry a correlation ID for distributed tracing.
 */
public abstract class DomainEvent {

    private final UUID eventId;
    private final Instant occurredAt;
    private final String correlationId;

    protected DomainEvent(String correlationId) {
        this.eventId = UUID.randomUUID();
        this.occurredAt = Instant.now();
        this.correlationId = correlationId;
    }

    public UUID getEventId() {
        return eventId;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public String getCorrelationId() {
        return correlationId;
    }
}
