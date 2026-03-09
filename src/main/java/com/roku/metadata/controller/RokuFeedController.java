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
 * REST Controller for Roku Direct Publisher Feed API.
 * 
 * Exposes endpoints that serve Roku-compliant content metadata with
 * appropriate caching headers for CDN and device-level caching.
 */
@RestController
@RequestMapping("/api/v1/roku")
@RequiredArgsConstructor
@Slf4j
public class RokuFeedController {

    private final RokuFeedService rokuFeedService;

    /**
     * Get complete Roku content feed with optional filtering.
     * 
     * Endpoint: GET /api/v1/roku/feed
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
            RokuFeedResponse feedResponse;
            
            // Apply filters if provided
            if (genre != null || language != null) {
                feedResponse = rokuFeedService.getContentByFilters(genre, language);
            } else {
                feedResponse = rokuFeedService.getAllContent();
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

        } catch (Exception e) {
            log.error("Error generating Roku feed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Health check endpoint for monitoring.
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("{\"status\": \"UP\", \"service\": \"roku-metadata-engine\"}");
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
    public record ErrorResponse(String code, String message) {}
}
