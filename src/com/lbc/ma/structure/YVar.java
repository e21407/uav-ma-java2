package com.lbc.ma.structure;

public class YVar {
    public int workflowId;
    public int pathId;
    public int currTaskId;
    public int succTaskId;

    public YVar(int workflowId, int pathId, int currTaskId, int succTaskId) {
        this.workflowId = workflowId;
        this.pathId = pathId;
        this.currTaskId = currTaskId;
        this.succTaskId = succTaskId;
    }
}
