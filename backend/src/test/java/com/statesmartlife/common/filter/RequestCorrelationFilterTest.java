package com.statesmartlife.common.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RequestCorrelationFilterTest {

    private RequestCorrelationFilter filter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        filter = new RequestCorrelationFilter();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = mock(FilterChain.class);
    }

    @Test
    @DisplayName("Should generate new X-Request-ID when header is missing in incoming request")
    void testGenerateCorrelationIdWhenMissing() throws ServletException, IOException {
        filter.doFilterInternal(request, response, filterChain);

        String correlationId = response.getHeader(RequestCorrelationFilter.CORRELATION_ID_HEADER);
        assertNotNull(correlationId);
        assertTrue(correlationId.startsWith("req_"));
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("Should preserve existing X-Request-ID when header is supplied by client")
    void testPreserveExistingCorrelationId() throws ServletException, IOException {
        String existingId = "req_custom_client_id_12345";
        request.addHeader(RequestCorrelationFilter.CORRELATION_ID_HEADER, existingId);

        filter.doFilterInternal(request, response, filterChain);

        String correlationId = response.getHeader(RequestCorrelationFilter.CORRELATION_ID_HEADER);
        assertEquals(existingId, correlationId);
        verify(filterChain, times(1)).doFilter(request, response);
    }
}
