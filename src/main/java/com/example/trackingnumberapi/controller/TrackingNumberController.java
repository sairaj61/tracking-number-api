package com.example.trackingnumberapi.controller;

import com.example.trackingnumberapi.model.dto.TrackingNumberRequestParams;
import com.example.trackingnumberapi.model.dto.TrackingNumberResponse;
import com.example.trackingnumberapi.service.TrackingNumberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@Validated
@Tag(name = "Tracking Number API", description = "Endpoints for generating unique tracking numbers")
public class TrackingNumberController {

    private final TrackingNumberService trackingNumberService;

    public TrackingNumberController(TrackingNumberService trackingNumberService) {
        this.trackingNumberService = trackingNumberService;
    }

    @Operation(summary = "Generate a new unique tracking number",
            description = "Generates a new tracking number based on the provided order details. " +
                    "The generated number is guaranteed to be unique.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully generated tracking number",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = TrackingNumberResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
                    @ApiResponse(responseCode = "500", description = "Internal server error or failed to generate unique ID after retries")
            })
    @GetMapping("/next-tracking-number")
    public ResponseEntity<TrackingNumberResponse> getNextTrackingNumber(
            @Parameter(description = "Order's origin country code (ISO 3166-1 alpha-2, e.g., 'MY')", required = true)
            @RequestParam String origin_country_id,

            @Parameter(description = "Order's destination country code (ISO 3166-1 alpha-2, e.g., 'ID')", required = true)
            @RequestParam String destination_country_id,

            @Parameter(description = "Order's weight in kilograms (e.g., '1.234')", required = true)
            @RequestParam BigDecimal weight,

            @Parameter(description = "Order's creation timestamp (RFC 3339 format, e.g., '2018-11-20T19:29:32+08:00')", required = true)
            @RequestParam OffsetDateTime created_at,

            @Parameter(description = "Customer's UUID (e.g., 'de619854-b59b-425e-9db4-943979e1bd49')", required = true)
            @RequestParam UUID customer_id,

            @Parameter(description = "Customer's name (e.g., 'RedBox Logistics')", required = true)
            @RequestParam String customer_name,

            @Parameter(description = "Customer's name in slug-case/kebab-case (e.g., 'redbox-logistics')", required = true)
            @RequestParam String customer_slug
    ) {
        TrackingNumberRequestParams params = new TrackingNumberRequestParams();
        params.setOriginCountryId(origin_country_id);
        params.setDestinationCountryId(destination_country_id);
        params.setWeight(weight);
        params.setCreatedAt(created_at);
        params.setCustomerId(customer_id);
        params.setCustomerName(customer_name);
        params.setCustomerSlug(customer_slug);

        TrackingNumberResponse response = trackingNumberService.generateAndSaveTrackingNumber(params);
        return ResponseEntity.ok(response);
    }
}