package com.africell.cci_backend.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
@Getter
@Setter

public class SubscriberResponse {
    private String mobileNumber;
    private String subscribedPlan;
    private String toneCode;
    private String toneName;
    private LocalDateTime billingDate;
    private LocalDateTime renewalDate;
    private String currentActivity;
}
