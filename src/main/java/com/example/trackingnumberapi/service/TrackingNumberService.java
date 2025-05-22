package com.example.trackingnumberapi.service;

import com.example.trackingnumberapi.model.dto.TrackingNumberRequestParams;
import com.example.trackingnumberapi.model.dto.TrackingNumberResponse;
import com.example.trackingnumberapi.model.entity.TrackingNumberRecord;
import com.example.trackingnumberapi.model.repository.TrackingNumberRecordRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@Slf4j
public class TrackingNumberService {


    private final TrackingIdGenerator idGenerator;
    private final TrackingNumberRecordRepository trackingNumberRepository;

    private static final int MAX_GENERATION_RETRIES = 5; // Max retries for generating a unique ID if a collision occurs

    public TrackingNumberService(TrackingIdGenerator idGenerator, TrackingNumberRecordRepository trackingNumberRepository) {
        this.idGenerator = idGenerator;
        this.trackingNumberRepository = trackingNumberRepository;
    }

    public TrackingNumberResponse generateAndSaveTrackingNumber(TrackingNumberRequestParams params) {
        String generatedTrackingNumber = null;
        TrackingNumberRecord savedRecord = null;
        int attempts = 0;

        while (attempts < MAX_GENERATION_RETRIES) {
            try {
                generatedTrackingNumber = idGenerator.nextIdBase36(); // Uses the ID Generator

                // Check against regex (Base36 should always match if length is okay)
                if (!generatedTrackingNumber.matches("^[A-Z0-9]{1,16}$")) {
                    log.warn("Generated ID {} does not match regex. Retrying... Attempt: {}", generatedTrackingNumber, attempts + 1);
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

                // The database's unique constraint is the final arbiter of uniqueness
                savedRecord = trackingNumberRepository.save(newRecord); // Interacts with the repository
                log.info("Successfully saved tracking number: {}", generatedTrackingNumber);
                break; // If save is successful, break the loop

            } catch (DataIntegrityViolationException e) {
                log.warn("Collision detected for tracking number: {}. Retrying... Attempt: {}. Error: {}", generatedTrackingNumber, (attempts + 1), e.getMessage());
            } catch (DataAccessException e) {
                log.error("Database access error while generating/saving tracking number for customer {}: {}", params.getCustomerId(), e.getMessage(), e);
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error: Failed to save tracking number.");
            } catch (IllegalStateException e) {
                log.error("ID generation error: {}", e.getMessage());
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to generate unique ID due to clock issues or generator configuration.");
            } catch (Exception e) {
                log.error("An unexpected error occurred during tracking number generation/saving for customer {}: {}", params.getCustomerId(), e.getMessage(), e);
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred. Please try again later.");
            }
            attempts++;
        }

        if (savedRecord == null || savedRecord.getGeneratedAt() == null) {
            log.error("Failed to generate and save a unique tracking number after {} attempts for customer {}", MAX_GENERATION_RETRIES, params.getCustomerId());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to generate a unique tracking number after multiple attempts. Please try again later.");
        }

        return new TrackingNumberResponse(
                savedRecord.getTrackingNumber(),
                savedRecord.getGeneratedAt()
        );
    }
}