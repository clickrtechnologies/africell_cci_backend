package com.africell.cci_backend.dto;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
@Getter
@Setter

public class SubscriberResponse {
    private String mobileNumber;
    private String subscribePlan;   //new
    private String toneCode;
    private String toneName;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime billingDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime renewalDate;
    private String currentActivity;
}
