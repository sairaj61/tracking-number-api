package com.example.trackingnumberapi.model.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "tracking_numbers", indexes = {
        @Index(name = "idx_tracking_number_unq", columnList = "trackingNumber", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
public class TrackingNumberRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO) // Or GenerationType.IDENTITY if preferred and DB supports it well
    private Long id; // Internal database ID

    @NotNull
    @Column(unique = true, nullable = false, length = 16)
    @Pattern(regexp = "^[A-Z0-9]{1,16}$", message = "Tracking number must match the pattern ^[A-Z0-9]{1,16}$")
    private String trackingNumber; // The generated tracking number

    @CreationTimestamp // Automatically set by Hibernate on creation
    @Column(nullable = false, updatable = false)
    private OffsetDateTime generatedAt; // Timestamp when this record was persisted

    // Fields from the request, stored for auditing/context
    @Size(min = 2, max = 2, message = "Origin country ID must be 2 characters")
    @Column(length = 2)
    private String originCountryId;

    @Size(min = 2, max = 2, message = "Destination country ID must be 2 characters")
    @Column(length = 2)
    private String destinationCountryId;

    @Column(precision = 10, scale = 3) // e.g., 1234567.890
    private BigDecimal weight; // Weight in kilograms

    private OffsetDateTime orderCreatedAt; // Order's creation timestamp from request

    private UUID customerId;

    @Size(max = 255)
    private String customerName;

    @Size(max = 255)
    @Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$", message = "Customer slug must be in kebab-case")
    private String customerSlug;


    public TrackingNumberRecord(String trackingNumber, String originCountryId, String destinationCountryId,
                                BigDecimal weight, OffsetDateTime orderCreatedAt, UUID customerId,
                                String customerName, String customerSlug) {
        this.trackingNumber = trackingNumber;
        this.originCountryId = originCountryId;
        this.destinationCountryId = destinationCountryId;
        this.weight = weight;
        this.orderCreatedAt = orderCreatedAt;
        this.customerId = customerId;
        this.customerName = customerName;
        this.customerSlug = customerSlug;
    }
}

