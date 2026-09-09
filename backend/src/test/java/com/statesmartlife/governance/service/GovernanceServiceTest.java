package com.statesmartlife.governance.service;

import com.statesmartlife.auth.repository.UserRepository;
import com.statesmartlife.commerce.repository.StoreRepository;
import com.statesmartlife.emergency.entity.EmergencyRequestEntity;
import com.statesmartlife.emergency.enums.EmergencyStatus;
import com.statesmartlife.emergency.repository.EmergencyRequestRepository;
import com.statesmartlife.governance.dto.DistrictAnalyticsResponse;
import com.statesmartlife.governance.dto.StateOverviewResponse;
import com.statesmartlife.governance.entity.DistrictAnalyticsEntity;
import com.statesmartlife.governance.entity.OdishaDistrictEntity;
import com.statesmartlife.governance.repository.DistrictAnalyticsRepository;
import com.statesmartlife.governance.repository.OdishaDistrictRepository;
import com.statesmartlife.healthcare.entity.AppointmentEntity;
import com.statesmartlife.healthcare.enums.AppointmentStatus;
import com.statesmartlife.healthcare.repository.AppointmentRepository;
import com.statesmartlife.order.entity.OrderEntity;
import com.statesmartlife.order.dto.OrderStatus;
import com.statesmartlife.order.repository.OrderRepository;
import com.statesmartlife.tourism.entity.TourismGuideEntity;
import com.statesmartlife.tourism.repository.TourismGuideRepository;
import com.statesmartlife.trust.entity.IncidentTicketEntity;
import com.statesmartlife.trust.enums.TicketStatus;
import com.statesmartlife.trust.repository.IncidentTicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GovernanceServiceTest {

    private OdishaDistrictRepository districtRepository;
    private DistrictAnalyticsRepository analyticsRepository;
    private UserRepository userRepository;
    private StoreRepository storeRepository;
    private OrderRepository orderRepository;
    private EmergencyRequestRepository emergencyRepository;
    private AppointmentRepository appointmentRepository;
    private IncidentTicketRepository incidentTicketRepository;
    private TourismGuideRepository guideRepository;
    private GovernanceServiceImpl governanceService;

    @BeforeEach
    void setUp() {
        districtRepository = mock(OdishaDistrictRepository.class);
        analyticsRepository = mock(DistrictAnalyticsRepository.class);
        userRepository = mock(UserRepository.class);
        storeRepository = mock(StoreRepository.class);
        orderRepository = mock(OrderRepository.class);
        emergencyRepository = mock(EmergencyRequestRepository.class);
        appointmentRepository = mock(AppointmentRepository.class);
        incidentTicketRepository = mock(IncidentTicketRepository.class);
        guideRepository = mock(TourismGuideRepository.class);

        governanceService = new GovernanceServiceImpl(
                districtRepository,
                analyticsRepository,
                userRepository,
                storeRepository,
                orderRepository,
                emergencyRepository,
                appointmentRepository,
                incidentTicketRepository,
                guideRepository
        );
    }

    @Test
    @DisplayName("getStateOverview - Calculates live KPIs correctly including paid order revenue")
    void getStateOverview_CalculatesLiveMetricsCorrectly() {
        when(userRepository.count()).thenReturn(1500L);
        when(storeRepository.count()).thenReturn(120L);
        when(orderRepository.count()).thenReturn(350L);

        OrderEntity paidOrder = OrderEntity.builder().status(OrderStatus.PAID).totalAmount(new BigDecimal("1500.00")).build();
        OrderEntity pendingOrder = OrderEntity.builder().status(OrderStatus.PENDING_PAYMENT).totalAmount(new BigDecimal("2500.00")).build();
        OrderEntity cancelledOrder = OrderEntity.builder().status(OrderStatus.CANCELLED).totalAmount(new BigDecimal("500.00")).build();

        when(orderRepository.sumTotalAmountByStatus(OrderStatus.PAID)).thenReturn(new BigDecimal("1500.00"));
        when(emergencyRepository.countByStatusIn(any())).thenReturn(1L);
        when(appointmentRepository.countByStatusIn(any())).thenReturn(1L);
        when(incidentTicketRepository.countByStatus(TicketStatus.UNDER_REVIEW)).thenReturn(1L);
        when(guideRepository.countByIsVerifiedTrueAndIsAvailableTrue()).thenReturn(1L);

        StateOverviewResponse response = governanceService.getStateOverview();

        assertNotNull(response);
        assertEquals(1500L, response.getTotalCitizens());
        assertEquals(120L, response.getTotalStores());
        assertEquals(350L, response.getTotalOrders());
        assertEquals(new BigDecimal("1500.00"), response.getTotalCommerceRevenue()); // 1500 (pending and cancelled excluded)
        assertEquals(1L, response.getActiveSosEmergencies());
        assertEquals(1L, response.getTelehealthBookings());
        assertEquals(1L, response.getDisputesUnderReview());
        assertEquals(1L, response.getVerifiedTourGuides());
    }

    @Test
    @DisplayName("getDistrictAnalytics - Guarantees all active districts are returned even with 0 activity")
    void getDistrictAnalytics_GuaranteesAllDistricts() {
        UUID khordhaId = UUID.randomUUID();
        UUID deogarhId = UUID.randomUUID();

        OdishaDistrictEntity khordha = OdishaDistrictEntity.builder().id(khordhaId).districtCode("OD-KHO").districtName("Khordha").build();
        OdishaDistrictEntity deogarh = OdishaDistrictEntity.builder().id(deogarhId).districtCode("OD-DGH").districtName("Deogarh").build();

        when(districtRepository.findByIsActiveTrueOrderByDistrictNameAsc()).thenReturn(List.of(deogarh, khordha));

        DistrictAnalyticsEntity khordhaAnalytics = DistrictAnalyticsEntity.builder()
                .districtId(khordhaId)
                .totalCitizens(12000)
                .totalRevenue(new BigDecimal("500000.00"))
                .build();

        when(analyticsRepository.findByDistrictId(khordhaId)).thenReturn(Optional.of(khordhaAnalytics));
        when(analyticsRepository.findByDistrictId(deogarhId)).thenReturn(Optional.empty()); // Deogarh has 0 metrics

        List<DistrictAnalyticsResponse> response = governanceService.getDistrictAnalytics();

        assertEquals(2, response.size());

        // Deogarh zero-activity baseline check
        DistrictAnalyticsResponse deogarhResponse = response.stream().filter(r -> r.getDistrictCode().equals("OD-DGH")).findFirst().orElseThrow();
        assertEquals("Deogarh", deogarhResponse.getDistrictName());
        assertEquals(0, deogarhResponse.getTotalCitizens());
        assertEquals(BigDecimal.ZERO, deogarhResponse.getTotalRevenue());

        // Khordha check
        DistrictAnalyticsResponse khordhaResponse = response.stream().filter(r -> r.getDistrictCode().equals("OD-KHO")).findFirst().orElseThrow();
        assertEquals(12000, khordhaResponse.getTotalCitizens());
        assertEquals(new BigDecimal("500000.00"), khordhaResponse.getTotalRevenue());
    }
}
