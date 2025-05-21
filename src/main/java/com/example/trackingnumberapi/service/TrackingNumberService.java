package com.example.trackingnumberapi.service;



import com.example.trackingnumberapi.model.dto.TrackingNumberRequestParams;
import com.example.trackingnumberapi.model.dto.TrackingNumberResponse;
import com.example.trackingnumberapi.model.entity.TrackingNumberRecord;
import com.example.trackingnumberapi.model.repository.TrackingNumberRecordRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TrackingNumberService {

    private final SnowflakeIdGenerator idGenerator;
    private final TrackingNumberRecordRepository trackingNumberRepository;

    private static final int MAX_GENERATION_RETRIES = 5; // Max retries for generating a unique ID if a collision occurs

    public TrackingNumberService(SnowflakeIdGenerator idGenerator, TrackingNumberRecordRepository trackingNumberRepository) {
        this.idGenerator = idGenerator;
        this.trackingNumberRepository = trackingNumberRepository;
    }

    public TrackingNumberResponse generateAndSaveTrackingNumber(TrackingNumberRequestParams params) {
        String generatedTrackingNumber = null;
        TrackingNumberRecord savedRecord = null;
        int attempts = 0;

        while (attempts < MAX_GENERATION_RETRIES) {
            generatedTrackingNumber = idGenerator.nextIdBase36(); // Uses the SnowflakeIdGenerator

            // Check against regex (Snowflake Base36 should always match if length is okay)
            if (!generatedTrackingNumber.matches("^[A-Z0-9]{1,16}$")) {
                System.err.println("Generated ID " + generatedTrackingNumber + " does not match regex. Retrying...");
                attempts++;
                continue;
            }

            TrackingNumberRecord newRecord = new TrackingNumberRecord(
                    generatedTrackingNumber,
                    params.getOriginCountryId(),
                    params.getDestinationCountryId(),
                    params.getWeight(),
                    params.getCreatedAt(),
                    params.getCustomerId(),
                    params.getCustomerName(),
                    params.getCustomerSlug()
            );

            try {
                // The database's unique constraint is the final arbiter of uniqueness
                savedRecord = trackingNumberRepository.save(newRecord); // Interacts with the repository
                // If save is successful, break the loop
                break;
            } catch (DataIntegrityViolationException e) {
                System.err.println("Collision detected for tracking number: " + generatedTrackingNumber + ". Retrying... Attempt: " + (attempts + 1));
            }
            attempts++;
        }

        if (savedRecord == null || savedRecord.getGeneratedAt() == null) {
            System.err.println("Failed to generate and save a unique tracking number after " + MAX_GENERATION_RETRIES + " attempts.");
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to generate a unique tracking number. Please try again later.");
        }

        return new TrackingNumberResponse(
                savedRecord.getTrackingNumber(),
                savedRecord.getGeneratedAt()
        );
    }
}