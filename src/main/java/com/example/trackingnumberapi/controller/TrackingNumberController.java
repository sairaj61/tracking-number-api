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
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@Validated
@Tag(name = "Tracking Number API", description = "Endpoints for generating unique tracking numbers")
@Slf4j
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
        log.info("Received request to generate tracking number for customer: {}", customer_id);

        TrackingNumberRequestParams params = new TrackingNumberRequestParams();
        params.setOriginCountryId(origin_country_id);
        params.setDestinationCountryId(destination_country_id);
        params.setWeight(weight);
        params.setCreatedAt(created_at);
        params.setCustomerId(customer_id);
        params.setCustomerName(customer_name);
        params.setCustomerSlug(customer_slug);

        try {
            TrackingNumberResponse response = trackingNumberService.generateAndSaveTrackingNumber(params);
            log.info("Successfully generated tracking number: {} for customer: {}", response.getTrackingNumber(), customer_id);
            return ResponseEntity.ok(response);
        } catch (ResponseStatusException e) {
            log.warn("API Error: {} - {}", e.getMessage(), e.getReason());
            throw e; // Re-throw to be handled by the @ExceptionHandler
        } catch (Exception e) {
            log.error("An unexpected error occurred while generating tracking number for customer {}: {}", customer_id, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred. Please try again later.");
        }
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<String> handleResponseStatusException(ResponseStatusException ex) {
        return ResponseEntity.status(ex.getStatusCode()).body(ex.getReason());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGenericException(Exception ex) {
        log.error("An unhandled exception occurred: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An internal server error occurred.");
    }
}