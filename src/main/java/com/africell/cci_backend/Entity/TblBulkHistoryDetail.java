package com.africell.cci_backend.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_bulk_history_detail")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class TblBulkHistoryDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "preview_id", length = 20)
    private String previewId;

    @Column(name = "mobile")
    private Long mobile;

    @Column(name = "tone_id", length = 30)
    private String toneId;

    @Column(name = "tone_name", length = 255)
    private String toneName;

    @Column(name = "artist_name", length = 255)
    private String artistName;

    @Column(name = "package_plan", length = 20)
    private String packagePlan;

    @Column(name = "status")
    private Byte status;

    @Column(name = "message", length = 500)
    private String message;

    @Column(name = "transaction_date")
    private LocalDateTime transactionDate;
}
