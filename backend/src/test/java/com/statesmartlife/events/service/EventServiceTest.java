package com.statesmartlife.events.service;

import com.statesmartlife.events.dto.BookVenueRequest;
import com.statesmartlife.events.dto.EventBookingResponse;
import com.statesmartlife.events.entity.EventBookingEntity;
import com.statesmartlife.events.entity.EventVenueEntity;
import com.statesmartlife.events.enums.EventBookingStatus;
import com.statesmartlife.events.exception.VenueDateUnavailableException;
import com.statesmartlife.events.repository.EventBookingRepository;
import com.statesmartlife.events.repository.EventVenueRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EventServiceTest {

    private EventVenueRepository venueRepository;
    private EventBookingRepository bookingRepository;
    private EventServiceImpl eventService;

    private UUID venueId;
    private UUID plannerId;

    @BeforeEach
    void setUp() {
        venueRepository = mock(EventVenueRepository.class);
        bookingRepository = mock(EventBookingRepository.class);
        eventService = new EventServiceImpl(venueRepository, bookingRepository);

        venueId = UUID.randomUUID();
        plannerId = UUID.randomUUID();
    }

    @Test
    @DisplayName("bookVenue - Books venue and snapshots price per day")
    void bookVenue_Success() {
        EventVenueEntity venue = EventVenueEntity.builder()
                .id(venueId)
                .name("Kalinga Exhibition Hall")
                .pricePerDay(new BigDecimal("25000.00"))
                .isActive(true)
                .build();

        when(venueRepository.findById(venueId)).thenReturn(Optional.of(venue));
        when(bookingRepository.existsByVenueIdAndEventDateAndStatusIn(any(), any(), any())).thenReturn(false);
        when(bookingRepository.save(any(EventBookingEntity.class))).thenAnswer(i -> {
            EventBookingEntity entity = i.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        BookVenueRequest request = BookVenueRequest.builder()
                .venueId(venueId)
                .eventDate(LocalDate.now().plusDays(5))
                .vendorRequirements("Sound system & catering setup")
                .build();

        EventBookingResponse response = eventService.bookVenue(plannerId, request);

        assertNotNull(response);
        assertEquals(new BigDecimal("25000.00"), response.getTotalBudget());
        assertEquals(EventBookingStatus.CONFIRMED, response.getStatus());
    }

    @Test
    @DisplayName("bookVenue - Throws VenueDateUnavailableException if venue already booked on date")
    void bookVenue_DoubleBooking_ThrowsException() {
        EventVenueEntity venue = EventVenueEntity.builder().id(venueId).isActive(true).build();
        when(venueRepository.findById(venueId)).thenReturn(Optional.of(venue));
        when(bookingRepository.existsByVenueIdAndEventDateAndStatusIn(any(), any(), any())).thenReturn(true);

        BookVenueRequest request = BookVenueRequest.builder()
                .venueId(venueId)
                .eventDate(LocalDate.now().plusDays(5))
                .build();

        assertThrows(VenueDateUnavailableException.class, () -> eventService.bookVenue(plannerId, request));
    }
}
