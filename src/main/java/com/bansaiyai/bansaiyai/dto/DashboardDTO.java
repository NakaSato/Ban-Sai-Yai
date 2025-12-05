package com.bansaiyai.bansaiyai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public class DashboardDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActivityItem {
        private String id;
        private ActivityType type;
        private String action;
        private String description;
        private String performedBy;
        private LocalDateTime timestamp;
        private String entityId;
        private String entityName;
    }

    public enum ActivityType {
        MEMBER,
        LOAN,
        SAVINGS,
        PAYMENT
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChartData {
        private List<String> labels;
        private List<DataSet> datasets;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataSet {
        private String label;
        private List<Number> data;
        private String borderColor;
        private String backgroundColor;
        private Boolean fill;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuickAction {
        private String id;
        private String title;
        private String description;
        private String icon;
        private String route;
        private String permission;
        private Integer priority;
    }
}
