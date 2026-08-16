package com.africell.cci_backend.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
@Entity
@Table(name = "tbl_try_n_buy")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TblTryNBuy {
    @Id
    @Column(name = "msisdn", nullable = false)
    private Long msisdn;

    @Column(name = "req_date")
    private LocalDateTime reqDate;

    @Column(name = "status")
    private Integer status;

    @Column(name = "tone_code")
    private String toneCode;
}

