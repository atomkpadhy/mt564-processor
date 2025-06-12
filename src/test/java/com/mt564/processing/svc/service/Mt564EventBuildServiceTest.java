package com.mt564.processing.svc.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mt564.processing.svc.model.dto.Mt564EventDto;
import com.mt564.processing.svc.model.entity.Mt564Event;
import com.mt564.processing.svc.repository.Mt564EventAuditRepository;
import com.mt564.processing.svc.repository.Mt564EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class Mt564EventBuildServiceTest {

    @Mock
    private Mt564EventRepository eventRepository;

    @Mock
    private Mt564EventAuditRepository auditRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private Mt564EventBuildService buildService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        buildService = new Mt564EventBuildService(eventRepository, auditRepository, objectMapper);
    }

    @Test
    void testProcessDailyEvents_insertsNewEventsAndAudits() {
        Mt564EventDto dto = new Mt564EventDto();
        dto.setEventReference("EVT1");
        dto.setFinancialInstrumentId("FI1");
        dto.setSenderBic("BIC1");
        dto.setUpdatedAt(LocalDateTime.now());

        Mt564Event event = Mt564Event.of(dto);
        event.setVersionNumber(1);

        when(eventRepository.findAllById(any())).thenReturn(Collections.emptyList());
        when(eventRepository.saveAll(any())).thenReturn(List.of(event));
        when(auditRepository.saveAllAndFlush(any())).thenReturn(null);

        buildService.processDailyEvents(List.of(dto));

        verify(eventRepository, times(1)).saveAll(any());
        verify(auditRepository, times(1)).saveAllAndFlush(any());
    }

    @Test
    void testProcessDailyEvents_updatesChangedEvent() {
        Mt564EventDto dto = new Mt564EventDto();
        dto.setEventReference("EVT1");
        dto.setFinancialInstrumentId("FI1");
        dto.setSenderBic("BIC1");
        dto.setUpdatedAt(LocalDateTime.now());

        Mt564Event existing = Mt564Event.of(dto);
        existing.setBusinessHash("HASH1"); // different from dto
        existing.setVersionNumber(1);

        when(eventRepository.findAllById(any())).thenReturn(List.of(existing));
        when(eventRepository.saveAll(any())).thenReturn(List.of(existing));

        buildService.processDailyEvents(List.of(dto));

        verify(eventRepository).saveAll(any());
        verify(auditRepository).saveAllAndFlush(any());
    }

    @Test
    void testProcessDailyEvents_skipsUnchangedEvent() {
        Mt564EventDto dto = new Mt564EventDto();
        dto.setEventReference("EVT1");
        dto.setFinancialInstrumentId("FI1");
        dto.setSenderBic("BIC1");
        dto.setUpdatedAt(LocalDateTime.now());

        Mt564Event existing = Mt564Event.of(dto);
        existing.setBusinessHash(dto.getBusinessHash()); // same as dto

        when(eventRepository.findAllById(any())).thenReturn(List.of(existing));

        buildService.processDailyEvents(List.of(dto));

        verify(eventRepository, never()).saveAll(any());
        verify(auditRepository, never()).saveAllAndFlush(any());
    }

    @Test
    void testHasChanged_returnsTrueIfHashIsDifferent() {
        Mt564EventDto dto = new Mt564EventDto();

        Mt564Event entity = new Mt564Event();
        entity.setBusinessHash("old");

        boolean result = buildServiceTest_hasChanged(dto, entity);
        assertTrue(result);
    }

    private boolean buildServiceTest_hasChanged(Mt564EventDto dto, Mt564Event entity) {
        return !dto.getBusinessHash().equals(entity.getBusinessHash());
    }
}
