package com.mt564.processing.svc.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mt564.processing.svc.model.entity.CustomReportConfig;
import com.mt564.processing.svc.repository.CustomReportConfigRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomReportServiceTest {

    @Mock
    private CustomReportConfigRepository configRepository;

    @Mock
    private NamedParameterJdbcTemplate jdbcTemplate;

    @InjectMocks
    private CustomReportService customReportService;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    private CustomReportConfig sampleConfig;

    @BeforeEach
    void setup() throws JsonProcessingException {
        Map<String, Object> filters = new HashMap<>();
        filters.put("corporate_action_event_type", "DIV");

        String filterJson = objectMapper.writeValueAsString(filters);

        sampleConfig = CustomReportConfig.builder()
                .id(1L)
                .reportName("DIV Report")
                .selectedColumns("event_reference,financial_instrument_id,sender_bic,record_date,ex_date")
                .filterConditions(filterJson)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void testExecuteReport_successful() {
        Long reportId = 1L;
        when(configRepository.findById(reportId)).thenReturn(Optional.of(sampleConfig));

        Map<String, Object> mockRow = new HashMap<>();
        mockRow.put("event_reference", "EVT123");
        mockRow.put("financial_instrument_id", "INFRA123");
        mockRow.put("sender_bic", "SBININBB");
        mockRow.put("record_date", "2024-05-01");
        mockRow.put("ex_date", "2024-04-29");

        List<Map<String, Object>> expectedResults = List.of(mockRow);

        String expectedQueryString = "SELECT event_reference, financial_instrument_id, sender_bic, record_date, ex_date FROM mt564_event WHERE corporate_action_event_type = :corporate_action_event_type";

        Map<String, Object> filterParams = Map.of("corporate_action_event_type", "DIV");

        when(jdbcTemplate.queryForList(anyString(), any(MapSqlParameterSource.class)))
                .thenReturn(expectedResults);

        List<Map<String, Object>> result = customReportService.executeReport(reportId);

        assertEquals(1, result.size());
        assertEquals("EVT123", result.get(0).get("event_reference"));

        verify(configRepository).findById(reportId);
        verify(jdbcTemplate).queryForList(matches(expectedQueryString), any(MapSqlParameterSource.class));
    }

    @Test
    void testExecuteReport_configNotFound() {
        Long reportId = 999L;
        when(configRepository.findById(reportId)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            customReportService.executeReport(reportId);
        });

        assertEquals("Report config not found", ex.getMessage());
    }

    @Test
    void testExecuteReport_invalidFilterJson() {
        CustomReportConfig badJsonConfig = CustomReportConfig.builder()
                .id(2L)
                .reportName("Bad Filter Report")
                .selectedColumns("event_reference")
                .filterConditions("{invalid-json")
                .createdAt(LocalDateTime.now())
                .build();

        when(configRepository.findById(2L)).thenReturn(Optional.of(badJsonConfig));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            customReportService.executeReport(2L);
        });

        assertEquals("Invalid filter JSON", ex.getMessage());
    }
}
