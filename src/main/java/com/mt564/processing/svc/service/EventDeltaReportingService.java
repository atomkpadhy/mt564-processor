package com.mt564.processing.svc.service;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mt564.processing.svc.model.dto.Mt564EventDelta;
import com.mt564.processing.svc.model.entity.Mt564EventAudit;
import com.mt564.processing.svc.repository.Mt564EventAuditRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventDeltaReportingService {

    private final Mt564EventAuditRepository auditRepo;

    @Autowired
    private final ObjectMapper objectMapper;

    /**
     * Retrieves all field-level changes (deltas) in MT564 events between the given timestamps.
     *
     * This method performs the following steps:
     * - Fetches all audit records updated between the given 'from' and 'to' timestamps.
     * - Groups the audit records by a composite key made up of eventReference, financialInstrumentId, and senderBic.
     * - Within each group, it sorts the records by version number to establish a timeline of changes.
     * - Iterates over each adjacent pair of versions and computes field-level differences using computeDelta().
     * - If any changes are found between a pair, the corresponding Mt564EventDelta is added to the result list.
     *
     * Parameters:
     *   from - the start of the date-time range (inclusive)
     *   to   - the end of the date-time range (inclusive)
     *
     * Returns:
     *   A list of Mt564EventDelta objects, each representing the set of fields that changed
     *   between two versions of an MT564 event within the given time range.
     */
    public List<Mt564EventDelta> getFieldLevelDeltas(LocalDateTime from, LocalDateTime to) {

        // Fetch all audit records updated between the given 'from' and 'to' timestamps.
        List<Mt564EventAudit> audits = auditRepo.findByUpdatedAtBetween(from, to);

        // Group the audit records by a composite key made up of eventReference, financialInstrumentId, and senderBic.
        Map<String, List<Mt564EventAudit>> groupedAudits = audits.stream()
                .collect(Collectors.groupingBy(a -> a.getId().getEventReference()
                        + "|" + a.getId().getFinancialInstrumentId()
                        + "|" + a.getId().getSenderBic()));

        List<Mt564EventDelta> deltas = new ArrayList<>();
        for (List<Mt564EventAudit> versions : groupedAudits.values()) {
            // Within each group, sort the records by version number to establish a timeline of changes.
            versions.sort(Comparator.comparing(a -> a.getId().getVersionNumber()));
            // Iterate over each adjacent pair of versions and compute field-level differences using computeDelta()
            for (int i = 1; i < versions.size(); i++) {
                Mt564EventAudit prev = versions.get(i - 1);
                Mt564EventAudit curr = versions.get(i);
                Mt564EventDelta delta = computeDelta(prev, curr);
                // If any changes are found between a pair, the corresponding Mt564EventDelta is added to the result list.
                if (!delta.getChangedFields().isEmpty()) {
                    deltas.add(delta);
                }
            }
        }

        return deltas;
    }

    /**
     * Computes the field-level differences between two versions of an Mt564EventAudit snapshot.
     *
     * This method parses the JSON snapshots from both audit versions, compares their fields,
     * and identifies the ones that have changed. It records the old and new values of those fields
     * and returns an Mt564EventDelta object encapsulating the differences.
     *
     * Parameters:
     *   v1 - the earlier version of the audit snapshot
     *   v2 - the later version of the audit snapshot
     *
     * Returns:
     *   Mt564EventDelta - an object containing:
     *     - eventReference, financialInstrumentId, and senderBic from the ID
     *     - versionFrom and versionTo
     *     - a map of changed fields with their old and new values
     *     - the timestamp of the newer version
     *
     * Throws:
     *   RuntimeException if snapshot parsing or comparison fails
     */
    private Mt564EventDelta computeDelta(Mt564EventAudit v1, Mt564EventAudit v2) {
        try {
            JsonNode node1 = objectMapper.readTree(v1.getSnapshot());
            JsonNode node2 = objectMapper.readTree(v2.getSnapshot());

            Map<String, Object[]> diffs = new LinkedHashMap<>();

            Iterator<String> fields = node1.fieldNames();
            while (fields.hasNext()) {
                String field = fields.next();
                JsonNode val1 = node1.get(field);
                JsonNode val2 = node2.get(field);

                if (!Objects.equals(val1, val2)) {
                    diffs.put(field, new Object[]{
                            val1 != null ? val1.asText() : null,
                            val2 != null ? val2.asText() : null
                    });
                }
            }

            return Mt564EventDelta.builder()
                    .eventReference(v1.getId().getEventReference())
                    .financialInstrumentId(v1.getId().getFinancialInstrumentId())
                    .senderBic(v1.getId().getSenderBic())
                    .versionFrom(v1.getId().getVersionNumber())
                    .versionTo(v2.getId().getVersionNumber())
                    .changedFields(diffs)
                    .timestamp(v2.getUpdatedAt())
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Delta comparison failed", e);
        }
    }

}
