package com.statesmartlife.delivery.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PickupAddressDto {
    private String addressLine1;
    private String city;
    private String state;
    private String pincode;
}
