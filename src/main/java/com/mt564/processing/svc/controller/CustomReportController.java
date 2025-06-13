package com.mt564.processing.svc.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mt564.processing.svc.model.dto.CustomReportRequest;
import com.mt564.processing.svc.model.entity.CustomReportConfig;
import com.mt564.processing.svc.repository.CustomReportConfigRepository;
import com.mt564.processing.svc.service.CustomReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports/custom")
public class CustomReportController {

    @Autowired
    private CustomReportConfigRepository configRepository;

    @Autowired
    private CustomReportService customReportService;

    @Autowired
    private ObjectMapper objectMapper;

    /*
    Frontend UI :
    - Text Box to name the report
    - multi-select dropdown or checkboxes to allow users to select fields(select from DB Catalog tables).
    - check box with a text box besides each item for filters.
        {
              "reportName": "DIV Report",
              "columns": ["event_reference", "financial_instrument_id", "sender_bic", "record_date", "ex_date"],
              "filters": {
                "corporate_action_event_type": "DIV"
              }
        }
     */
    @PostMapping("/save")
    public CustomReportConfig saveCustomReport(@RequestBody CustomReportRequest request) {
        CustomReportConfig config = new CustomReportConfig();
        config.setReportName(request.getReportName());
        config.setSelectedColumns(String.join(",", request.getColumns()));

        try {
            config.setFilterConditions(objectMapper.writeValueAsString(request.getFilters()));
        } catch (Exception e) {
            throw new RuntimeException("Error serializing filters");
        }

        config.setCreatedAt(LocalDateTime.now());
        return configRepository.save(config);
    }

    @GetMapping("/{id}/execute")
    public List<Map<String, Object>> executeCustomReport(@PathVariable Long id) {
        return customReportService.executeReport(id);
    }
}