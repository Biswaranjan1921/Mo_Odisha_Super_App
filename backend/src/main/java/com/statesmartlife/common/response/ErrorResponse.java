package com.statesmartlife.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

/**
 * Standardized RFC 7807 Compliant API Error Response Envelope.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    @Builder.Default
    private String timestamp = Instant.now().toString();

    private int status;

    private String error;

    private String message;

    private String path;

    private String requestId;

    private Map<String, String> validationErrors;
}
