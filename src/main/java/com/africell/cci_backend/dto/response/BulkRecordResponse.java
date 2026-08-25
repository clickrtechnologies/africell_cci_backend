package com.africell.cci_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.util.List;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class BulkRecordResponse {
    private String mobileNumber;

    private String toneId;

    private String toneName;

    private String packagePlan;

    private String artistName;

    private boolean valid;

    private List<String> errors;
}
