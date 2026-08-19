package com.africell.cci_backend.Entity;
import jakarta.persistence.*;

@Entity
@Table(name = "tbl_billing_id")
public class TblBillingId {
    @Id
    private Long id;

    private String productId;
    private String serviceId;
    private String amount;

    private Integer validity;

    private String chargeCode;
    private String messageId;
    private String userBalance;
    private String packType;
    private Integer fallbackFlag;
    private String fallbackId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public Integer getValidity() {
        return validity;
    }

    public void setValidity(Integer validity) {
        this.validity = validity;
    }

    public String getChargeCode() {
        return chargeCode;
    }

    public void setChargeCode(String chargeCode) {
        this.chargeCode = chargeCode;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getUserBalance() {
        return userBalance;
    }

    public void setUserBalance(String userBalance) {
        this.userBalance = userBalance;
    }

    public String getPackType() {
        return packType;
    }

    public void setPackType(String packType) {
        this.packType = packType;
    }

    public Integer getFallbackFlag() {
        return fallbackFlag;
    }

    public void setFallbackFlag(Integer fallbackFlag) {
        this.fallbackFlag = fallbackFlag;
    }

    public String getFallbackId() {
        return fallbackId;
    }

    public void setFallbackId(String fallbackId) {
        this.fallbackId = fallbackId;
    }
}
