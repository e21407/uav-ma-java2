package com.lbc.ma.structure;

public class Action {
     public int wfId;
     public int currTaskId;
     public int succTaskId;
     public int newPathId;
     public int oldNodeForSuccTask;
     public int newNodeForSuccTask;

    public Action(int wfId, int currTaskId, int succTaskId, int newPathId, int oldNodeForSuccTask, int newNodeForSuccTask) {
        this.wfId = wfId;
        this.currTaskId = currTaskId;
        this.succTaskId = succTaskId;
        this.newPathId = newPathId;
        this.oldNodeForSuccTask = oldNodeForSuccTask;
        this.newNodeForSuccTask = newNodeForSuccTask;
    }
}
