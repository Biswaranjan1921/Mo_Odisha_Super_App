package com.statesmartlife.events.service;

import com.statesmartlife.events.dto.BookVenueRequest;
import com.statesmartlife.events.dto.EventBookingResponse;
import com.statesmartlife.events.dto.EventVenueResponse;
import com.statesmartlife.events.entity.EventBookingEntity;
import com.statesmartlife.events.entity.EventVenueEntity;
import com.statesmartlife.events.enums.EventBookingStatus;
import com.statesmartlife.events.exception.VenueDateUnavailableException;
import com.statesmartlife.events.exception.VenueNotFoundException;
import com.statesmartlife.events.repository.EventBookingRepository;
import com.statesmartlife.events.repository.EventVenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventVenueRepository venueRepository;
    private final EventBookingRepository bookingRepository;

    @Override
    @Transactional(readOnly = true)
    public List<EventVenueResponse> getAllVenues(String keyword) {
        List<EventVenueEntity> venues;
        if (keyword != null && !keyword.isBlank()) {
            venues = venueRepository.findByNameContainingIgnoreCaseAndIsActiveTrue(keyword);
        } else {
            venues = venueRepository.findByIsActiveTrue();
        }
        return venues.stream().map(this::mapToVenueResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public EventVenueResponse getVenueById(UUID id) {
        EventVenueEntity venue = venueRepository.findById(id)
                .orElseThrow(() -> new VenueNotFoundException("Event venue not found with id: " + id));
        return mapToVenueResponse(venue);
    }

    @Override
    @Transactional
    public EventBookingResponse bookVenue(UUID plannerId, BookVenueRequest request) {
        EventVenueEntity venue = venueRepository.findById(request.getVenueId())
                .orElseThrow(() -> new VenueNotFoundException("Event venue not found with id: " + request.getVenueId()));

        if (!venue.isActive()) {
            throw new VenueDateUnavailableException("Event venue is currently inactive");
        }

        boolean dateTaken = bookingRepository.existsByVenueIdAndEventDateAndStatusIn(
                venue.getId(),
                request.getEventDate(),
                List.of(EventBookingStatus.PENDING, EventBookingStatus.CONFIRMED)
        );

        if (dateTaken) {
            throw new VenueDateUnavailableException("Event venue is already reserved on " + request.getEventDate());
        }

        EventBookingEntity booking = EventBookingEntity.builder()
                .venueId(venue.getId())
                .plannerId(plannerId)
                .eventDate(request.getEventDate())
                .totalBudget(venue.getPricePerDay())
                .vendorRequirements(request.getVendorRequirements())
                .status(EventBookingStatus.CONFIRMED)
                .build();

        EventBookingEntity saved = bookingRepository.save(booking);
        return mapToBookingResponse(saved, venue.getName());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventBookingResponse> getMyVenueBookings(UUID plannerId) {
        return bookingRepository.findByPlannerIdOrderByEventDateDesc(plannerId)
                .stream()
                .map(b -> {
                    String venueName = venueRepository.findById(b.getVenueId()).map(EventVenueEntity::getName).orElse("Public Venue");
                    return mapToBookingResponse(b, venueName);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventBookingResponse updateVenueBookingStatus(UUID bookingId, EventBookingStatus status, UUID userId) {
        EventBookingEntity booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new VenueNotFoundException("Event booking not found with id: " + bookingId));

        booking.setStatus(status);
        EventBookingEntity saved = bookingRepository.save(booking);
        String venueName = venueRepository.findById(saved.getVenueId()).map(EventVenueEntity::getName).orElse("Public Venue");
        return mapToBookingResponse(saved, venueName);
    }

    private EventVenueResponse mapToVenueResponse(EventVenueEntity entity) {
        return EventVenueResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .address(entity.getAddress())
                .latitude(entity.getLatitude())
                .longitude(entity.getLongitude())
                .capacity(entity.getCapacity())
                .pricePerDay(entity.getPricePerDay())
                .amenities(entity.getAmenities())
                .isActive(entity.isActive())
                .build();
    }

    private EventBookingResponse mapToBookingResponse(EventBookingEntity entity, String venueName) {
        return EventBookingResponse.builder()
                .id(entity.getId())
                .venueId(entity.getVenueId())
                .venueName(venueName)
                .plannerId(entity.getPlannerId())
                .eventDate(entity.getEventDate())
                .totalBudget(entity.getTotalBudget())
                .vendorRequirements(entity.getVendorRequirements())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
