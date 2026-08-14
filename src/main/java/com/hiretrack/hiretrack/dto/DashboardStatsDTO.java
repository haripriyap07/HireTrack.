package com.hiretrack.hiretrack.dto;

import java.util.HashMap;
import java.util.Map;

public class DashboardStatsDTO {

    private Map<String, Long> counts = new HashMap<>();

    public DashboardStatsDTO() {}

    public DashboardStatsDTO(Map<String, Long> counts) {
        this.counts = counts;
    }

    public Map<String, Long> getCounts() {
        return counts;
    }

    public void setCounts(Map<String, Long> counts) {
        this.counts = counts;
    }

    public void put(String key, Long value) {
        counts.put(key, value);
    }
}
