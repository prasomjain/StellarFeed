package com.roku.metadata.model;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enum representing Roku's supported media types.
 * Matches the Direct Publisher specification for content categorization.
 */
public enum MediaType {
    MOVIE("movie"),
    SERIES("series"),
    SHORTFORM("shortFormVideo");

    private final String rokuValue;

    MediaType(String rokuValue) {
        this.rokuValue = rokuValue;
    }

    @JsonValue
    public String getRokuValue() {
        return rokuValue;
    }
}
