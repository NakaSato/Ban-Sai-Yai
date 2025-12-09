package com.bansaiyai.bansaiyai.dto.dashboard;

import java.util.List;

public class MembershipTrendsDTO {
    private List<MonthlyMembershipData> trends;

    public MembershipTrendsDTO() {
    }

    public MembershipTrendsDTO(List<MonthlyMembershipData> trends) {
        this.trends = trends;
    }

    public List<MonthlyMembershipData> getTrends() {
        return trends;
    }

    public void setTrends(List<MonthlyMembershipData> trends) {
        this.trends = trends;
    }

    public static class MonthlyMembershipData {
        private String month;
        private int newMembers;
        private int resignations;

        public MonthlyMembershipData() {
        }

        public MonthlyMembershipData(String month, int newMembers, int resignations) {
            this.month = month;
            this.newMembers = newMembers;
            this.resignations = resignations;
        }

        public String getMonth() {
            return month;
        }

        public void setMonth(String month) {
            this.month = month;
        }

        public int getNewMembers() {
            return newMembers;
        }

        public void setNewMembers(int newMembers) {
            this.newMembers = newMembers;
        }

        public int getResignations() {
            return resignations;
        }

        public void setResignations(int resignations) {
            this.resignations = resignations;
        }
    }
}
