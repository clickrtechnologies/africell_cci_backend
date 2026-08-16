package com.africell.cci_backend.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_subscription")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class TblSubscription {
    @Id
    @Column(name = "msisdn", nullable = false)
    private Long msisdn;

    @Column(name = "sub_date")
    private LocalDateTime subDate;

    @Column(name = "req_date")
    private LocalDateTime reqDate;

    @Column(name = "billing_date")
    private LocalDateTime billingDate;

    @Column(name = "renew_date")
    private LocalDateTime renewDate;

    @Column(name = "pack_name")
    private String packName;

    @Column(name = "trans_id")
    private String transId;

    @Column(name = "service_id")
    private String serviceId;

    @Column(name = "product_id")
    private String productId;

    @Column(name = "reqMode")
    private String reqMode;

    @Column(name = "toneCode")
    private String toneCode;

    @Column(name = "lang")
    private String lang;

    @Column(name = "status")
    private Integer status;

    @Column(name = "amount")
    private Integer amount;

    @Column(name = "charging_status")
    private Byte chargingStatus;

    @Column(name = "no_of_retries")
    private Byte noOfRetries;

    @Column(name = "fallback_string")
    private String fallbackString;

    @Column(name = "fallback_packs")
    private String fallbackPacks;

    @Column(name = "user_status")
    private String userStatus;

    @Column(name = "promoName")
    private String promoName;

    @Column(name = "promoId")
    private String promoId;

    @Column(name = "currentBalance")
    private String currentBalance;

}
