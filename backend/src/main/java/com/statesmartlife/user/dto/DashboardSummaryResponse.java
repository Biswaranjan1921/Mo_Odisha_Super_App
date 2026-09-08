package com.statesmartlife.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryResponse {

    private UserProfileResponse profile;
    private int activeOrdersCount;
    private int activeAppointmentsCount;
    private int activeTransitPassesCount;
    private String systemNotice;
}
