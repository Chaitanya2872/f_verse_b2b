package com.acs.crm.api;

import java.util.Map;

public class DashboardSummary {
    private long totalValue;
    private long weightedPipelineValue;
    private int openDeals;
    private int pendingApprovals;
    private int inDelivery;
    private int stalledDeals;
    private Map<String, Integer> funnelCounts;

    public DashboardSummary(long totalValue, long weightedPipelineValue, int openDeals, int pendingApprovals, int inDelivery, int stalledDeals,
                            Map<String, Integer> funnelCounts) {
        this.totalValue = totalValue;
        this.weightedPipelineValue = weightedPipelineValue;
        this.openDeals = openDeals;
        this.pendingApprovals = pendingApprovals;
        this.inDelivery = inDelivery;
        this.stalledDeals = stalledDeals;
        this.funnelCounts = funnelCounts;
    }

    public long getTotalValue() {
        return totalValue;
    }

    public long getWeightedPipelineValue() {
        return weightedPipelineValue;
    }

    public int getOpenDeals() {
        return openDeals;
    }

    public int getPendingApprovals() {
        return pendingApprovals;
    }

    public int getInDelivery() {
        return inDelivery;
    }

    public int getStalledDeals() {
        return stalledDeals;
    }

    public Map<String, Integer> getFunnelCounts() {
        return funnelCounts;
    }
}
