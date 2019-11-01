package com.lbc.ma.structure;

public class Node {
    private String type;
    private Integer nodeId;
    private Double Capacity;

    public Node(String type, Integer nodeId, Double capacity) {
        this.type = type;
        this.nodeId = nodeId;
        Capacity = capacity;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getNodeId() {
        return nodeId;
    }

    public void setNodeId(Integer nodeId) {
        this.nodeId = nodeId;
    }

    public Double getCapacity() {
        return Capacity;
    }

    public void setCapacity(Double capacity) {
        Capacity = capacity;
    }
}
