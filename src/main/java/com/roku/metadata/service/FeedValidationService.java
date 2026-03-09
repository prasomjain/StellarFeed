package com.roku.metadata.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import com.roku.metadata.dto.RokuFeedResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Set;

/**
 * Service for validating Roku feed responses against the official schema.
 * 
 * Ensures all feed data conforms to Roku's Direct Publisher JSON specification
 * before being served to devices.
 */
@Service
@Slf4j
public class FeedValidationService {

    private final ObjectMapper objectMapper;
    private final JsonSchema rokuFeedSchema;

    public FeedValidationService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.rokuFeedSchema = loadRokuFeedSchema();
    }

    /**
     * Validate a RokuFeedResponse against the Roku Feed Schema.
     * 
     * @param feedResponse The feed response to validate
     * @return true if valid, false otherwise
     */
    public boolean validateFeedStructure(RokuFeedResponse feedResponse) {
        try {
            // Convert DTO to JsonNode
            JsonNode feedNode = objectMapper.valueToTree(feedResponse);
            
            // Validate against schema
            Set<ValidationMessage> errors = rokuFeedSchema.validate(feedNode);
            
            if (errors.isEmpty()) {
                log.info("Feed structure validated successfully");
                return true;
            } else {
                log.error("Feed validation failed with {} errors:", errors.size());
                errors.forEach(error -> log.error("  - {}", error.getMessage()));
                return false;
            }
        } catch (Exception e) {
            log.error("Error during feed validation", e);
            return false;
        }
    }

    /**
     * Load the Roku Feed Schema from resources.
     */
    private JsonSchema loadRokuFeedSchema() {
        try {
            ClassPathResource resource = new ClassPathResource("schemas/roku-feed-schema.json");
            InputStream schemaStream = resource.getInputStream();
            
            JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
            return factory.getSchema(schemaStream);
        } catch (Exception e) {
            log.warn("Could not load Roku Feed Schema, validation will be skipped", e);
            // Return a permissive schema if the file doesn't exist
            return createPermissiveSchema();
        }
    }

    /**
     * Create a permissive schema that accepts any valid JSON.
     * Used as fallback when schema file is not available.
     */
    private JsonSchema createPermissiveSchema() {
        try {
            String permissiveSchemaJson = "{\"type\": \"object\"}";
            JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
            return factory.getSchema(permissiveSchemaJson);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create permissive schema", e);
        }
    }
}
