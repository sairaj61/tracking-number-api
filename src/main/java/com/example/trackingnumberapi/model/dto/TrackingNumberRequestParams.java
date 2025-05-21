package com.example.trackingnumberapi.model.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

// Using a @Data class for request parameters can be convenient for validation
// even if they come as individual @RequestParam.
// This also makes it easy to pass them around.
@Data // from Lombok: generates getters, setters, toString, equals, hashCode
public class TrackingNumberRequestParams {

    @NotBlank(message = "Origin country ID is required")
    @Size(min = 2, max = 2, message = "Origin country ID must be 2 characters (ISO 3166-1 alpha-2)")
    @Pattern(regexp = "^[A-Z]{2}$", message = "Origin country ID must be 2 uppercase letters")
    private String originCountryId;

    @NotBlank(message = "Destination country ID is required")
    @Size(min = 2, max = 2, message = "Destination country ID must be 2 characters (ISO 3166-1 alpha-2)")
    @Pattern(regexp = "^[A-Z]{2}$", message = "Destination country ID must be 2 uppercase letters")
    private String destinationCountryId;

    @NotNull(message = "Weight is required")
    @DecimalMin(value = "0.001", message = "Weight must be positive") // Assuming weight cannot be zero or negative
    @Digits(integer = 7, fraction = 3, message = "Weight can have up to 7 integer and 3 decimal places") // e.g. 1234567.123
    private BigDecimal weight;

    @NotNull(message = "Order created_at timestamp is required")
    // Spring Boot will automatically try to parse RFC 3339 format for OffsetDateTime
    private OffsetDateTime createdAt; // order's creation timestamp

    @NotNull(message = "Customer ID is required")
    private UUID customerId;

    @NotBlank(message = "Customer name is required")
    @Size(max = 255, message = "Customer name cannot exceed 255 characters")
    private String customerName;

    @NotBlank(message = "Customer slug is required")
    @Size(max = 255, message = "Customer slug cannot exceed 255 characters")
    @Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$", message = "Customer slug must be in kebab-case (e.g., 'redbox-logistics')")
    private String customerSlug;
}

