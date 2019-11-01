package com.lbc.ma.structure;

public class Link {
    private Integer srcNodeId;
    private Integer dstNodeId;
    private Double bandwidth;

    public Link(Integer srcNodeId, Integer dstNodeId, Double bandwidth) {
        this.srcNodeId = srcNodeId;
        this.dstNodeId = dstNodeId;
        this.bandwidth = bandwidth;
    }

    public Integer getSrcNodeId() {
        return srcNodeId;
    }

    public void setSrcNodeId(Integer srcNodeId) {
        this.srcNodeId = srcNodeId;
    }

    public Integer getDstNodeId() {
        return dstNodeId;
    }

    public void setDstNodeId(Integer dstNodeId) {
        this.dstNodeId = dstNodeId;
    }

    public Double getBandwidth() {
        return bandwidth;
    }

    public void setBandwidth(Double bandwidth) {
        this.bandwidth = bandwidth;
    }
}
