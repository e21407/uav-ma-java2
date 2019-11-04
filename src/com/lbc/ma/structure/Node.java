package com.lbc.ma.structure;

public class Node {
    public String type;
    public Integer nodeId;
    public Double capacity;

    public Node(String type, Integer nodeId, Double capacity) {
        this.type = type;
        this.nodeId = nodeId;
        this.capacity = capacity;
    }
}
