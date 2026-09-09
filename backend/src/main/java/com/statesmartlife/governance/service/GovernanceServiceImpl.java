package com.statesmartlife.governance.service;

import com.statesmartlife.auth.repository.UserRepository;
import com.statesmartlife.commerce.repository.StoreRepository;
import com.statesmartlife.emergency.enums.EmergencyStatus;
import com.statesmartlife.emergency.repository.EmergencyRequestRepository;
import com.statesmartlife.governance.dto.DistrictAnalyticsResponse;
import com.statesmartlife.governance.dto.StateOverviewResponse;
import com.statesmartlife.governance.entity.DistrictAnalyticsEntity;
import com.statesmartlife.governance.entity.OdishaDistrictEntity;
import com.statesmartlife.governance.repository.DistrictAnalyticsRepository;
import com.statesmartlife.governance.repository.OdishaDistrictRepository;
import com.statesmartlife.healthcare.enums.AppointmentStatus;
import com.statesmartlife.healthcare.repository.AppointmentRepository;
import com.statesmartlife.order.entity.OrderEntity;
import com.statesmartlife.order.dto.OrderStatus;
import com.statesmartlife.order.repository.OrderRepository;
import com.statesmartlife.tourism.repository.TourismGuideRepository;
import com.statesmartlife.trust.enums.TicketStatus;
import com.statesmartlife.trust.repository.IncidentTicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GovernanceServiceImpl implements GovernanceService {

    private final OdishaDistrictRepository districtRepository;
    private final DistrictAnalyticsRepository analyticsRepository;
    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final OrderRepository orderRepository;
    private final EmergencyRequestRepository emergencyRepository;
    private final AppointmentRepository appointmentRepository;
    private final IncidentTicketRepository incidentTicketRepository;
    private final TourismGuideRepository guideRepository;

    @Override
    @Transactional(readOnly = true)
    public StateOverviewResponse getStateOverview() {
        long totalCitizens = userRepository.count();
        long totalStores = storeRepository.count();
        long totalOrders = orderRepository.count();

        // Calculate commerce revenue strictly from PAID orders
        BigDecimal totalRevenue = orderRepository.findAll().stream()
                .filter(o -> o.getStatus() == OrderStatus.PAID)
                .map(OrderEntity::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Active SOS Emergency requests
        long activeSos = emergencyRepository.findAll().stream()
                .filter(e -> e.getStatus() == EmergencyStatus.REPORTED ||
                             e.getStatus() == EmergencyStatus.DISPATCHED ||
                             e.getStatus() == EmergencyStatus.EN_ROUTE ||
                             e.getStatus() == EmergencyStatus.ON_SCENE)
                .count();

        // Telehealth Bookings (BOOKED, CONFIRMED, COMPLETED)
        long telehealthBookings = appointmentRepository.findAll().stream()
                .filter(a -> a.getStatus() == AppointmentStatus.BOOKED ||
                             a.getStatus() == AppointmentStatus.CONFIRMED ||
                             a.getStatus() == AppointmentStatus.COMPLETED)
                .count();

        // Disputes under review
        long disputesUnderReview = incidentTicketRepository.findAll().stream()
                .filter(t -> t.getStatus() == TicketStatus.UNDER_REVIEW)
                .count();

        // Verified tour guides
        long verifiedGuides = guideRepository.findByIsVerifiedTrueAndIsAvailableTrue().size();

        return StateOverviewResponse.builder()
                .totalCitizens(totalCitizens)
                .totalStores(totalStores)
                .totalOrders(totalOrders)
                .totalCommerceRevenue(totalRevenue)
                .activeSosEmergencies(activeSos)
                .telehealthBookings(telehealthBookings)
                .disputesUnderReview(disputesUnderReview)
                .verifiedTourGuides(verifiedGuides)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DistrictAnalyticsResponse> getDistrictAnalytics() {
        List<OdishaDistrictEntity> districts = districtRepository.findByIsActiveTrueOrderByDistrictNameAsc();

        return districts.stream().map(district -> {
            Optional<DistrictAnalyticsEntity> analyticsOpt = analyticsRepository.findByDistrictId(district.getId());

            if (analyticsOpt.isPresent()) {
                DistrictAnalyticsEntity a = analyticsOpt.get();
                return DistrictAnalyticsResponse.builder()
                        .districtId(district.getId())
                        .districtCode(district.getDistrictCode())
                        .districtName(district.getDistrictName())
                        .totalCitizens(a.getTotalCitizens())
                        .totalStores(a.getTotalStores())
                        .totalOrders(a.getTotalOrders())
                        .totalRevenue(a.getTotalRevenue())
                        .activeSosAlerts(a.getActiveSosAlerts())
                        .telehealthBookings(a.getTelehealthBookings())
                        .disputesUnderReview(a.getDisputesUnderReview())
                        .verifiedGuides(a.getVerifiedGuides())
                        .build();
            } else {
                // Zero-activity district rendering guarantee
                return DistrictAnalyticsResponse.builder()
                        .districtId(district.getId())
                        .districtCode(district.getDistrictCode())
                        .districtName(district.getDistrictName())
                        .totalCitizens(0)
                        .totalStores(0)
                        .totalOrders(0)
                        .totalRevenue(BigDecimal.ZERO)
                        .activeSosAlerts(0)
                        .telehealthBookings(0)
                        .disputesUnderReview(0)
                        .verifiedGuides(0)
                        .build();
            }
        }).collect(Collectors.toList());
    }
}
