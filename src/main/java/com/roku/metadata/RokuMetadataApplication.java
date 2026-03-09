package com.roku.metadata;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * Main application class for Roku Content Metadata Engine.
 * 
 * This microservice provides Roku-compliant content metadata feeds
 * with Redis caching for high-performance delivery to millions of devices.
 */
@SpringBootApplication
@EnableCaching
public class RokuMetadataApplication {

    public static void main(String[] args) {
        SpringApplication.run(RokuMetadataApplication.class, args);
    }
}
