package com.statesmartlife.events.service;

import com.statesmartlife.events.dto.BookVenueRequest;
import com.statesmartlife.events.dto.EventBookingResponse;
import com.statesmartlife.events.dto.EventVenueResponse;
import com.statesmartlife.events.enums.EventBookingStatus;

import java.util.List;
import java.util.UUID;

public interface EventService {
    List<EventVenueResponse> getAllVenues(String keyword);
    EventVenueResponse getVenueById(UUID id);
    EventBookingResponse bookVenue(UUID plannerId, BookVenueRequest request);
    List<EventBookingResponse> getMyVenueBookings(UUID plannerId);
    EventBookingResponse updateVenueBookingStatus(UUID bookingId, EventBookingStatus status, UUID userId);
}
