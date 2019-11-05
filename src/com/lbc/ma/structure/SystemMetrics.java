package com.lbc.ma.structure;

public class SystemMetrics {
    public double throughput;
    public double computeCost;
    public double routingCost;

    public SystemMetrics(double throughput, double computeCost, double routingCost) {
        this.throughput = throughput;
        this.computeCost = computeCost;
        this.routingCost = routingCost;
    }

    public double getPerformance(){
        return  throughput - computeCost - routingCost;
    }
}
