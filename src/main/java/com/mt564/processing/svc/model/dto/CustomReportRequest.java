package com.mt564.processing.svc.model.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class CustomReportRequest {
    private String reportName;
    private List<String> columns;
    private Map<String, Object> filters;
}