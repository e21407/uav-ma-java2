package com.lbc.ma.structure;

public class Link {
    public Integer srcNodeId;
    public Integer dstNodeId;
    public Double bandwidth;

    public Link(Integer srcNodeId, Integer dstNodeId, Double bandwidth) {
        this.srcNodeId = srcNodeId;
        this.dstNodeId = dstNodeId;
        this.bandwidth = bandwidth;
    }
}
