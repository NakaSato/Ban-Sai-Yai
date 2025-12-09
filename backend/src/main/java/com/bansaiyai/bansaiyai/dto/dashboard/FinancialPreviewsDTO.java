package com.bansaiyai.bansaiyai.dto.dashboard;

import java.util.List;
import java.util.Map;

public class FinancialPreviewsDTO {
    private IncomeChartData incomeData;
    private BalanceSheetChartData balanceSheetData;

    public FinancialPreviewsDTO() {
    }

    public FinancialPreviewsDTO(IncomeChartData incomeData, BalanceSheetChartData balanceSheetData) {
        this.incomeData = incomeData;
        this.balanceSheetData = balanceSheetData;
    }

    public IncomeChartData getIncomeData() {
        return incomeData;
    }

    public void setIncomeData(IncomeChartData incomeData) {
        this.incomeData = incomeData;
    }

    public BalanceSheetChartData getBalanceSheetData() {
        return balanceSheetData;
    }

    public void setBalanceSheetData(BalanceSheetChartData balanceSheetData) {
        this.balanceSheetData = balanceSheetData;
    }

    public static class IncomeChartData {
        private List<String> labels;
        private Map<String, Object> datasets;

        public IncomeChartData() {
        }

        public IncomeChartData(List<String> labels, Map<String, Object> datasets) {
            this.labels = labels;
            this.datasets = datasets;
        }

        public List<String> getLabels() {
            return labels;
        }

        public void setLabels(List<String> labels) {
            this.labels = labels;
        }

        public Map<String, Object> getDatasets() {
            return datasets;
        }

        public void setDatasets(Map<String, Object> datasets) {
            this.datasets = datasets;
        }
    }

    public static class BalanceSheetChartData {
        private List<String> labels;
        private Map<String, Object> datasets;

        public BalanceSheetChartData() {
        }

        public BalanceSheetChartData(List<String> labels, Map<String, Object> datasets) {
            this.labels = labels;
            this.datasets = datasets;
        }

        public List<String> getLabels() {
            return labels;
        }

        public void setLabels(List<String> labels) {
            this.labels = labels;
        }

        public Map<String, Object> getDatasets() {
            return datasets;
        }

        public void setDatasets(Map<String, Object> datasets) {
            this.datasets = datasets;
        }
    }
}
