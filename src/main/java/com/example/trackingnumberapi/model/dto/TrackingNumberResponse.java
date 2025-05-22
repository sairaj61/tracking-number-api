package com.example.trackingnumberapi.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
public class TrackingNumberResponse {
    @NotNull
    private String trackingNumber;

    @NotNull
    private String createdAt; // RFC 3339 format timestamp of when the tracking number was generated

    private String message;

    public TrackingNumberResponse(String trackingNumber, OffsetDateTime createdAt) {
        this.trackingNumber = trackingNumber;
        this.createdAt = createdAt.toString(); // Converts to RFC 3339 string
    }
    public TrackingNumberResponse(String trackingNumber, OffsetDateTime createdAt, String message) {
        this(trackingNumber, createdAt);
        this.message = message;
    }
}
