package com.mt564.processing.svc.controller;

import com.mt564.processing.svc.model.dto.Mt564EventDelta;
import com.mt564.processing.svc.service.EventDeltaReportingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
@Slf4j
@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
class Mt564EventController {

    @Autowired
    EventDeltaReportingService eventDeltaReportingService;
    /**
     * API to retrieve field-level changes for MT564 events within a specified date range.
     *
     * Input: A date range (fromDate and toDate) as request parameters.
     *
     * Output: A list of change summaries for MT564 announcements, each entry includes:
     * - eventReference: Unique event identifier (:20C::CORP)
     * - financialInstrumentId: Instrument ID (:35B:)
     * - senderBic: Sender's BIC code
     * - versionFrom: Previous version number
     * - versionTo: Latest version number
     * - changedFields: Map of field names to their old and new values
     * - timestamp: Change detection timestamp
     *
     * Example JSON response:
     *
     * [
     *   {
     *     "eventReference": "EVT001",
     *     "financialInstrumentId": "ABC123",
     *     "senderBic": "BNPAFRPPXXX",
     *     "versionFrom": 2,
     *     "versionTo": 3,
     *     "changedFields": {
     *       "eventProcessingStatus": {
     *         "old": "INPROG",
     *         "new": "COMPLETED"
     *       },
     *       "announcementDate": {
     *         "old": "2024-06-05",
     *         "new": "2024-06-06"
     *       }
     *     },
     *     "timestamp": "2024-06-10T14:35:00"
     *   }
     * ]
     */
    @GetMapping("/delta")
    public ResponseEntity<List<Mt564EventDelta>> getFieldLevelDeltas(
            @RequestParam("fromDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam("toDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        try {
            List<Mt564EventDelta> deltas = eventDeltaReportingService.getFieldLevelDeltas(from.atStartOfDay(), to.plusDays(1).atStartOfDay());
            return ResponseEntity.ok(deltas);
        } catch (Exception ex) {
            log.error("Error Retrieving MT564 Events Delta [{}] [{}] [{}]", from, to, ex.getMessage(), ex);
            return ResponseEntity.internalServerError().body(null);
        }
    }



}
