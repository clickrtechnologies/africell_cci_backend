package com.africell.cci_backend.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ActivationRequest {

    private Long msisdn;

    private String packName;

    private String toneCode;

    private String toneName;
}