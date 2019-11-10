package com.lbc.ma.structure;

public class Action {
     public int wfId;
     public int currTaskId;
     public int succTaskId;
     public int newPathId;
     public int oldNodeForSuccTask;
     public int newNodeForSuccTask;
     public SystemMetrics oldSystemMetrics;
     public SystemMetrics newSystemMetrics;

    public Action(int wfId, int currTaskId, int succTaskId, int newPathId, int oldNodeForSuccTask,
                  int newNodeForSuccTask, SystemMetrics oldSystemMetrics, SystemMetrics newSystemMetrics) {
        this.wfId = wfId;
        this.currTaskId = currTaskId;
        this.succTaskId = succTaskId;
        this.newPathId = newPathId;
        this.oldNodeForSuccTask = oldNodeForSuccTask;
        this.newNodeForSuccTask = newNodeForSuccTask;
        this.oldSystemMetrics = oldSystemMetrics;
        this.newSystemMetrics = newSystemMetrics;
    }
}
