package com.africell.cci_backend.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ActivationResponse {

    private Long msisdn;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime subDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime reqDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime billingDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime renewDate;

    private String packName;

    private String transId;

    private String serviceId;

    private String productId;

    private String reqMode;

    private String toneCode;

    private String toneName;

    private String lang;

    private Integer status;

    private Integer amount;

    private Byte chargingStatus;

    private Byte noOfRetries;

    private String fallbackString;

    private String fallbackPacks;

    private String userStatus;

    private String promoName;

    private String promoId;

    private String currentBalance;
}