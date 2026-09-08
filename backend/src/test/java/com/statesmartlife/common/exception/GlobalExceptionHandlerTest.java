package com.statesmartlife.common.exception;

import com.statesmartlife.common.response.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/test");
        request.addHeader("X-Request-ID", "req_test_123");
    }

    @Test
    @DisplayName("Should handle BusinessRuleException and format RFC 7807 ErrorResponse")
    void testHandleBusinessRuleException() {
        BusinessRuleException ex = new BusinessRuleException("INVALID_ORDER", "Order minimum amount not met", HttpStatus.UNPROCESSABLE_ENTITY);

        ResponseEntity<ErrorResponse> responseEntity = exceptionHandler.handleBusinessRuleException(ex, request);

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, responseEntity.getStatusCode());
        ErrorResponse body = responseEntity.getBody();
        assertNotNull(body);
        assertEquals(422, body.getStatus());
        assertEquals("INVALID_ORDER", body.getError());
        assertEquals("Order minimum amount not met", body.getMessage());
        assertEquals("/api/v1/test", body.getPath());
        assertEquals("req_test_123", body.getRequestId());
    }
}
