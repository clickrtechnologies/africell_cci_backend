package com.africell.cci_backend.dto.response;

import lombok.*;

import java.util.List;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BulkPreviewResponse {
    private String previewId;
    private String fileName;
    private Integer totalRecords;
    private Integer validRecords;
    private Integer invalidRecords;
    private List<BulkRecordResponse> records;
}
