package com.example.trackingnumberapi.model.repository;

import com.example.trackingnumberapi.model.entity.TrackingNumberRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TrackingNumberRecordRepository extends JpaRepository<TrackingNumberRecord, Long> {

    /**
     * Checks if a tracking number already exists.
     * This is more efficient than fetching the whole entity if you only need to check existence.
     * @param trackingNumber The tracking number to check.
     * @return true if it exists, false otherwise.
     */
    boolean existsByTrackingNumber(String trackingNumber);

    /**
     * Finds a tracking number record by its tracking number string.
     * @param trackingNumber The tracking number to find.
     * @return An Optional containing the record if found.
     */
    Optional<TrackingNumberRecord> findByTrackingNumber(String trackingNumber);
}
