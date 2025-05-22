package com.example.trackingnumberapi.config;

import com.example.trackingnumberapi.service.TrackingIdGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    /**
     * Defines trackingIdGenerator as a Spring Bean.
     * Spring will inject the 'tracking.worker.id' value from application.properties
     * into the 'workerId' parameter of the trackingIdGenerator constructor.
     * This provides an explicit way for Spring to manage and instantiate this bean.
     */
    @Bean // Marks the method's return value as a Spring-managed bean
    public TrackingIdGenerator trackingIdGenerator(@Value("${tracking.worker.id}") long workerId) {
        // Spring will call this method to create the IdGenerator instance,
        // providing the workerId value from application.properties.
        return new TrackingIdGenerator(workerId);
    }
}