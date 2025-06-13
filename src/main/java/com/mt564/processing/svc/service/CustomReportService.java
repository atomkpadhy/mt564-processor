package com.mt564.processing.svc.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mt564.processing.svc.model.entity.CustomReportConfig;
import com.mt564.processing.svc.repository.CustomReportConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public
class CustomReportService {

    private final CustomReportConfigRepository configRepository;
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Executes a dynamic report based on a saved custom report configuration.
     *
     * This method:
     * - Retrieves the report configuration by its ID.
     * - Parses the selected columns and filter conditions from the configuration.
     * - Builds a dynamic SQL SELECT query using the selected columns.
     * - Adds WHERE clause conditions based on the filters, using named parameters.
     * - Executes the query using NamedParameterJdbcTemplate.
     *
     * Example:
     *         {
     *               "reportName": "DIV Report",
     *               "columns": ["event_reference", "financial_instrument_id", "sender_bic", "record_date", "ex_date"],
     *               "filters": {
     *                 "corporate_action_event_type": "DIV"
     *               }
     *         }
     * it generates and runs:
     * SELECT event_reference, financial_instrument_id, sender_bic, record_date, ex_date
     *      FROM mt564_event
     *      WHERE corporate_action_event_type = :corporate_action_event_type
     *
     * @param id the ID of the custom report configuration to execute
     * @return list of result rows, where each row is a map of column names to values
     * @throws RuntimeException if the report config is not found or filter JSON is invalid
     */
    public List<Map<String, Object>> executeReport(Long id) {
        CustomReportConfig config = configRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Report config not found"));

        List<String> columns = Arrays.asList(config.getSelectedColumns().split(","));
        String selectClause = String.join(", ", columns);

        StringBuilder query = new StringBuilder("SELECT ").append(selectClause)
                .append(" FROM mt564_event");

        Map<String, Object> filters;
        try {
            filters = objectMapper.readValue(config.getFilterConditions(), new TypeReference<>() {});
        } catch (Exception e) {
            throw new RuntimeException("Invalid filter JSON");
        }

        if (!filters.isEmpty()) {
            query.append(" WHERE ");
            List<String> conditions = new ArrayList<>();
            for (String key : filters.keySet()) {
                conditions.add(key + " = :" + key);
            }
            query.append(String.join(" AND ", conditions));
        }

        return jdbcTemplate.queryForList(query.toString(), new MapSqlParameterSource(filters));
    }
}