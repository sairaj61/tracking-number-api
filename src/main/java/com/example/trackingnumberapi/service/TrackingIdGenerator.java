
package com.example.trackingnumberapi.service;

import org.springframework.beans.factory.annotation.Value;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;


public class TrackingIdGenerator {

    private static final int TIMESTAMP_BITS = 41;
    private static final int WORKER_ID_BITS = 10;
    private static final int SEQUENCE_BITS = 12;

    private static final long MAX_WORKER_ID = (1L << WORKER_ID_BITS) - 1;
    private static final long MAX_SEQUENCE = (1L << SEQUENCE_BITS) - 1;

    private static final long TIMESTAMP_SHIFT = WORKER_ID_BITS + SEQUENCE_BITS;
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;

    // Custom Epoch (January 1, 2020, 00:00:00 UTC)
    private static final OffsetDateTime CUSTOM_EPOCH_DT = OffsetDateTime.of(2020, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
    private static final long CUSTOM_EPOCH_MS = CUSTOM_EPOCH_DT.toInstant().toEpochMilli();

    private final long workerId;
    private final long epochMs;

    private long lastTimestampMs = -1L;
    private long sequence = 0L;
    private final Object lock = new Object();

    /**
     * Constructor for TrackingIdGenerator.
     * Worker ID is injected from application properties.
     * This is the constructor Spring should be using.
     *
     * @param workerId Injected worker ID from application properties (e.g., tracking.worker.id).
     */
    public TrackingIdGenerator(@Value("${tracking.worker.id}") long workerId) {
        this(workerId, CUSTOM_EPOCH_MS);
    }

    /**
     * Overloaded constructor for explicit workerId and epoch setting (e.g., for testing).
     *
     * @param workerId A unique ID for this worker/process.
     * @param epochMs  Custom epoch in milliseconds.
     */
    public TrackingIdGenerator(long workerId, long epochMs) {
        if (workerId < 0 || workerId > MAX_WORKER_ID) {
            throw new IllegalArgumentException(String.format("Worker ID must be between 0 and %d. Configured: %d", MAX_WORKER_ID, workerId));
        }
        this.workerId = workerId;
        this.epochMs = epochMs;
        System.out.println("trackingIdGenerator initialized with Worker ID: " + this.workerId + " and Epoch (ms): " + this.epochMs);
    }

    private long currentTimeMs() {
        return Instant.now().toEpochMilli();
    }

    private long tilNextMillis(long lastTimestampMs) {
        long timestampMs = currentTimeMs();
        while (timestampMs <= lastTimestampMs) {
            timestampMs = currentTimeMs();
        }
        return timestampMs;
    }

    public long nextId() {
        synchronized (lock) {
            long currentTimestampMs = currentTimeMs();

            if (currentTimestampMs < lastTimestampMs) {
                System.err.printf("Clock moved backwards. Refusing to generate id for %d milliseconds%n",
                        lastTimestampMs - currentTimestampMs);
                throw new IllegalStateException(String.format(
                        "Clock moved backwards. Refusing to generate id for %d milliseconds",
                        lastTimestampMs - currentTimestampMs));
            }

            if (lastTimestampMs == currentTimestampMs) {
                sequence = (sequence + 1) & MAX_SEQUENCE;
                if (sequence == 0) {
                    currentTimestampMs = tilNextMillis(lastTimestampMs);
                }
            } else {
                sequence = 0L;
            }

            lastTimestampMs = currentTimestampMs;

            return ((currentTimestampMs - epochMs) << TIMESTAMP_SHIFT) |
                    (this.workerId << WORKER_ID_SHIFT) |
                    sequence;
        }
    }

    public String nextIdBase36() {
        long id = nextId();
        return Long.toString(id, 36).toUpperCase();
    }

    public long getTimestampFromId(long id) {
        return (id >> TIMESTAMP_SHIFT) + epochMs;
    }

    public long getWorkerIdFromId(long id) {
        return (id >> WORKER_ID_SHIFT) & MAX_WORKER_ID;
    }

    public long getSequenceFromId(long id) {
        return id & MAX_SEQUENCE;
    }

    public OffsetDateTime getDateTimeFromId(long id) {
        return OffsetDateTime.ofInstant(Instant.ofEpochMilli(getTimestampFromId(id)), ZoneOffset.UTC);
    }
}