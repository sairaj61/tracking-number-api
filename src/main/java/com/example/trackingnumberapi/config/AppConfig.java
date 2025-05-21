package com.example.trackingnumberapi.config;

import com.example.trackingnumberapi.service.SnowflakeIdGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration // Marks this class as a source of bean definitions
public class AppConfig {

    /**
     * Defines SnowflakeIdGenerator as a Spring Bean.
     * Spring will inject the 'snowflake.worker.id' value from application.properties
     * into the 'workerId' parameter of the SnowflakeIdGenerator constructor.
     * This provides an explicit way for Spring to manage and instantiate this bean.
     */
    @Bean // Marks the method's return value as a Spring-managed bean
    public SnowflakeIdGenerator snowflakeIdGenerator(@Value("${snowflake.worker.id}") long workerId) {
        // Spring will call this method to create the SnowflakeIdGenerator instance,
        // providing the workerId value from application.properties.
        return new SnowflakeIdGenerator(workerId);
    }
}