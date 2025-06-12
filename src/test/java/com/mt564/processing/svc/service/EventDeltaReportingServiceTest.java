package com.mt564.processing.svc.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mt564.processing.svc.model.dto.Mt564EventDelta;
import com.mt564.processing.svc.model.entity.Mt564EventAudit;
import com.mt564.processing.svc.model.entity.Mt564EventAuditId;
import com.mt564.processing.svc.repository.Mt564EventAuditRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EventDeltaReportingServiceTest {

    @Mock
    private Mt564EventAuditRepository auditRepo;

    private ObjectMapper objectMapper;

    @InjectMocks
    private EventDeltaReportingService deltaReportingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();
        deltaReportingService = new EventDeltaReportingService(auditRepo, objectMapper);
    }

    @Test
    void testGetFieldLevelDeltas_withDifferences() {
        LocalDateTime from = LocalDateTime.now().minusDays(1);
        LocalDateTime to = LocalDateTime.now();

        Mt564EventAuditId id1 = new Mt564EventAuditId("EVT001", "ISIN001", "BIC001", 1);
        Mt564EventAuditId id2 = new Mt564EventAuditId("EVT001", "ISIN001", "BIC001", 2);

        Mt564EventAudit v1 = new Mt564EventAudit();
        v1.setId(id1);
        v1.setUpdatedAt(from);
        v1.setSnapshot("{\"field1\":\"value1\",\"field2\":\"value2\"}");

        Mt564EventAudit v2 = new Mt564EventAudit();
        v2.setId(id2);
        v2.setUpdatedAt(to);
        v2.setSnapshot("{\"field1\":\"value1-changed\",\"field2\":\"value2\"}");

        when(auditRepo.findByUpdatedAtBetween(from, to)).thenReturn(Arrays.asList(v1, v2));

        List<Mt564EventDelta> deltas = deltaReportingService.getFieldLevelDeltas(from, to);

        assertEquals(1, deltas.size());
        Mt564EventDelta delta = deltas.get(0);
        assertEquals("EVT001", delta.getEventReference());
        assertEquals("field1", delta.getChangedFields().keySet().iterator().next());
        assertEquals("value1", delta.getChangedFields().get("field1")[0]);
        assertEquals("value1-changed", delta.getChangedFields().get("field1")[1]);
    }

    @Test
    void testGetFieldLevelDeltas_noChanges() {
        LocalDateTime from = LocalDateTime.now().minusDays(1);
        LocalDateTime to = LocalDateTime.now();

        Mt564EventAuditId id1 = new Mt564EventAuditId("EVT002", "ISIN002", "BIC002", 1);
        Mt564EventAuditId id2 = new Mt564EventAuditId("EVT002", "ISIN002", "BIC002", 2);

        Mt564EventAudit v1 = new Mt564EventAudit();
        v1.setId(id1);
        v1.setUpdatedAt(from);
        v1.setSnapshot("{\"field1\":\"same\"}");

        Mt564EventAudit v2 = new Mt564EventAudit();
        v2.setId(id2);
        v2.setUpdatedAt(to);
        v2.setSnapshot("{\"field1\":\"same\"}");

        when(auditRepo.findByUpdatedAtBetween(from, to)).thenReturn(Arrays.asList(v1, v2));

        List<Mt564EventDelta> deltas = deltaReportingService.getFieldLevelDeltas(from, to);

        assertTrue(deltas.isEmpty());
    }

    @Test
    void testComputeDelta_invalidJson_throwsException() {

        LocalDateTime from = LocalDateTime.now().minusDays(1);
        LocalDateTime to = LocalDateTime.now();

        Mt564EventAuditId id1 = new Mt564EventAuditId("EVT003", "ISIN003", "BIC003", 1);
        Mt564EventAuditId id2 = new Mt564EventAuditId("EVT003", "ISIN003", "BIC003", 2);

        Mt564EventAudit v1 = new Mt564EventAudit();
        v1.setId(id1);
        v1.setSnapshot("{invalid-json");

        Mt564EventAudit v2 = new Mt564EventAudit();
        v2.setId(id2);
        v2.setSnapshot("{\"field\":\"value\"}");

        when(auditRepo.findByUpdatedAtBetween(from, to)).thenReturn(Arrays.asList(v1, v2));

        assertThrows(RuntimeException.class, () -> {
            deltaReportingService.getFieldLevelDeltas(from, to);
        });
    }
}
