package com.statesmartlife.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccessibilityDefaultsDto {

    private boolean largeText;
    private boolean highContrast;
    private boolean largeTouchTargets;
    private boolean reducedMotion;
    private boolean voiceAssistance;
    private boolean simplifiedNavigation;

    public static AccessibilityDefaultsDto forElderlyMode(boolean isElderly) {
        if (isElderly) {
            return AccessibilityDefaultsDto.builder()
                    .largeText(true)
                    .highContrast(true)
                    .largeTouchTargets(true)
                    .reducedMotion(true)
                    .voiceAssistance(true)
                    .simplifiedNavigation(true)
                    .build();
        } else {
            return AccessibilityDefaultsDto.builder()
                    .largeText(false)
                    .highContrast(false)
                    .largeTouchTargets(false)
                    .reducedMotion(false)
                    .voiceAssistance(false)
                    .simplifiedNavigation(false)
                    .build();
        }
    }
}
