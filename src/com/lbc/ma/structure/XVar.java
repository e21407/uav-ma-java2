package com.lbc.ma.structure;

public class XVar implements Cloneable{
    public int workflowId;
    public int taskId;
    public int nodeId;

    public XVar(int workflowId, int taskId, int nodeId) {
        this.workflowId = workflowId;
        this.taskId = taskId;
        this.nodeId = nodeId;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
