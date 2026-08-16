package com.africell.cci_backend.Entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_tone_catalogue")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TblToneCatalogue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "tone_code")
    private String toneCode;

    @Column(name = "tone_url")
    private String toneUrl;

    @Column(name = "tone_name")
    private String toneName;

    @Column(name = "lang")
    private String lang;

    @Column(name = "status")
    private Byte status;

    @Column(name = "category")
    private String category;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @Column(name = "priority")
    private Integer priority;

    @Column(name = "artist_name")
    private String artistName;
}
