package com.statesmartlife.events.controller;

import com.statesmartlife.common.exception.BusinessRuleException;
import com.statesmartlife.events.dto.BookVenueRequest;
import com.statesmartlife.events.dto.EventBookingResponse;
import com.statesmartlife.events.dto.EventVenueResponse;
import com.statesmartlife.events.enums.EventBookingStatus;
import com.statesmartlife.events.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @GetMapping("/venues")
    public ResponseEntity<List<EventVenueResponse>> getAllVenues(
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(eventService.getAllVenues(search));
    }

    @GetMapping("/venues/{id}")
    public ResponseEntity<EventVenueResponse> getVenueById(@PathVariable UUID id) {
        return ResponseEntity.ok(eventService.getVenueById(id));
    }

    @PostMapping("/bookings")
    public ResponseEntity<EventBookingResponse> bookVenue(
            @Valid @RequestBody BookVenueRequest request) {
        UUID plannerId = extractAuthenticatedUserId();
        return ResponseEntity.status(HttpStatus.CREATED).body(eventService.bookVenue(plannerId, request));
    }

    @GetMapping("/bookings/me")
    public ResponseEntity<List<EventBookingResponse>> getMyVenueBookings() {
        UUID plannerId = extractAuthenticatedUserId();
        return ResponseEntity.ok(eventService.getMyVenueBookings(plannerId));
    }

    @PutMapping("/bookings/{id}/status")
    public ResponseEntity<EventBookingResponse> updateVenueBookingStatus(
            @PathVariable UUID id,
            @RequestParam EventBookingStatus status) {
        UUID userId = extractAuthenticatedUserId();
        return ResponseEntity.ok(eventService.updateVenueBookingStatus(id, status, userId));
    }

    private UUID extractAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null || authentication.getName().equals("anonymousUser")) {
            throw new BusinessRuleException("UNAUTHORIZED", "User is not authenticated", HttpStatus.UNAUTHORIZED);
        }
        try {
            return UUID.fromString(authentication.getName());
        } catch (IllegalArgumentException e) {
            throw new BusinessRuleException("INVALID_USER_ID", "Invalid user security principal", HttpStatus.UNAUTHORIZED);
        }
    }
}
