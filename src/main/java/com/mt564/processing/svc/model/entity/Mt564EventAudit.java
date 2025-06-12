package com.mt564.processing.svc.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "mt564_events_audit")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Mt564EventAudit {
    @EmbeddedId
    private Mt564EventAuditId id;

    private String businessHash;
    private LocalDateTime updatedAt;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String snapshot;
}
