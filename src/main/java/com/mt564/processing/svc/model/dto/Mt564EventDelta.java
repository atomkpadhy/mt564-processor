package com.mt564.processing.svc.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
/* Delta API Response Body Object.
 *   Mt564EventDelta - an object containing:
 *     - eventReference, financialInstrumentId, and senderBic from the ID
 *     - versionFrom and versionTo
 *     - a map of changed fields with their old and new values
 *     - the timestamp of the newer version
 */
public class Mt564EventDelta {
    private String eventReference;
    private String financialInstrumentId;
    private String senderBic;
    private int versionFrom;
    private int versionTo;
    private Map<String, Object[]> changedFields;
    private LocalDateTime timestamp;
}