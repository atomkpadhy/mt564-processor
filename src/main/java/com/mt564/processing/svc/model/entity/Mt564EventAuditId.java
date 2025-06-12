package com.mt564.processing.svc.model.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Mt564EventAuditId implements Serializable {
    private String eventReference;
    private String financialInstrumentId;
    private String senderBic;
    private int versionNumber;
}