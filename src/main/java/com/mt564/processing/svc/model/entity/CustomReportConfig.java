package com.mt564.processing.svc.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "custom_report_config")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public
class CustomReportConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String reportName;

    @Column(length = 1000)
    private String selectedColumns; // Comma-separated columns

    @Column(length = 2000)
    private String filterConditions; // JSON string

    private LocalDateTime createdAt;
}
