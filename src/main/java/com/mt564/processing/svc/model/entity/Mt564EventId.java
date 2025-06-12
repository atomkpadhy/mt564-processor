package com.mt564.processing.svc.model.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Mt564EventId implements Serializable {

    private String eventReference;
    private String financialInstrumentId;
    private String senderBic;

    public static Mt564EventId of (String eventReference, String financialInstrumentId, String senderBic) {
        return new Mt564EventId(eventReference, financialInstrumentId, senderBic);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Mt564EventId)) return false;
        Mt564EventId that = (Mt564EventId) o;
        return Objects.equals(eventReference, that.eventReference)
                && Objects.equals(financialInstrumentId, that.financialInstrumentId)
                && Objects.equals(senderBic, that.senderBic);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventReference, financialInstrumentId, senderBic);
    }
}
