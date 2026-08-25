package com.africell.cci_backend.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
@Entity
@Table(
        name = "tbl_bulk_history",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_preview_id", columnNames = "preview_id")
        }

)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TblBulkHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "preview_id", nullable = false, unique = true, length = 20)
    private String previewId;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "total_records", nullable = false)
    private Integer totalRecords;

    @Column(name = "success_records", nullable = false)
    private Integer successRecords;

    @Column(name = "failed_records", nullable = false)
    private Integer failedRecords;

    @Column(name = "status", nullable = false)
    private Byte status;

    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;
}