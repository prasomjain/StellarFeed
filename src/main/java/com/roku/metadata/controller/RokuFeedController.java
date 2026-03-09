package com.roku.metadata.controller;

import com.roku.metadata.dto.RokuFeedResponse;
import com.roku.metadata.service.RokuFeedService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

/**
 * REST Controller for Video Streaming Metadata Feed API.
 * 
 * Exposes endpoints that serve content metadata with
 * appropriate caching headers for CDN and device-level caching.
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class RokuFeedController {

    private final RokuFeedService rokuFeedService;

    /**
     * Get complete content feed with optional filtering.
     * 
     * Endpoint: GET /api/v1/feed
     * 
     * Query Parameters:
     * - genre (optional): Filter by content genre (e.g., "Action", "Drama")
     * - language (optional): Filter by language code (e.g., "en", "es")
     * 
     * @return RokuFeedResponse with categorized content
     */
    @GetMapping(value = "/feed", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RokuFeedResponse> getRokuFeed(
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String language) {
        
        log.info("Received feed request - genre: {}, language: {}", genre, language);

        try {
            // Validate and sanitize input parameters
            String sanitizedGenre = sanitizeParameter(genre, 50, "genre");
            String sanitizedLanguage = sanitizeParameter(language, 10, "language");
            
            RokuFeedResponse feedResponse;
            
            // Apply filters if provided
            if (sanitizedGenre != null || sanitizedLanguage != null) {
                feedResponse = rokuFeedService.getContentByFilters(sanitizedGenre, sanitizedLanguage);
            } else {
                feedResponse = rokuFeedService.getAllContent();
            }

            // Validate response is not null
            if (feedResponse == null) {
                log.error("Service returned null feed response");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }

            // Set cache control headers for CDN and client caching
            CacheControl cacheControl = CacheControl.maxAge(1, TimeUnit.HOURS)
                .cachePublic()
                .mustRevalidate();

            return ResponseEntity.ok()
                .cacheControl(cacheControl)
                .header("X-Content-Type-Options", "nosniff")
                .header("X-Frame-Options", "DENY")
                .body(feedResponse);

        } catch (IllegalArgumentException e) {
            log.warn("Invalid request parameters: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error generating feed - genre: {}, language: {}", genre, language, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Sanitize and validate input parameters to prevent injection attacks.
     */
    private String sanitizeParameter(String param, int maxLength, String paramName) {
        if (param == null) {
            return null;
        }
        
        // Trim whitespace
        String sanitized = param.trim();
        
        // Return null if empty after trimming
        if (sanitized.isEmpty()) {
            return null;
        }
        
        // Check max length
        if (sanitized.length() > maxLength) {
            log.warn("Parameter {} exceeds max length of {}: {}", paramName, maxLength, sanitized.length());
            throw new IllegalArgumentException(paramName + " exceeds maximum length of " + maxLength);
        }
        
        // Validate alphanumeric with hyphens and underscores only
        if (!sanitized.matches("^[a-zA-Z0-9\\-_]+$")) {
            log.warn("Parameter {} contains invalid characters: {}", paramName, sanitized);
            throw new IllegalArgumentException(paramName + " contains invalid characters");
        }
        
        return sanitized;
    }

    /**
     * Health check endpoint for monitoring.
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("{\"status\": \"UP\", \"service\": \"stellarfeed-api\"}");
    }

    /**
     * Exception handler for illegal arguments.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("Invalid request parameter: {}", e.getMessage());
        ErrorResponse error = new ErrorResponse("INVALID_PARAMETER", e.getMessage());
        return ResponseEntity.badRequest().body(error);
    }

    /**
     * Generic exception handler.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception e) {
        log.error("Unexpected error in controller", e);
        ErrorResponse error = new ErrorResponse("INTERNAL_ERROR", "An unexpected error occurred");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    /**
     * Error response DTO.
     */
    public static record ErrorResponse(String code, String message) {}
}
