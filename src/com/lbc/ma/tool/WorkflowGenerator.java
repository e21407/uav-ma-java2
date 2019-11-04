package com.lbc.ma.tool;


import com.lbc.ma.structure.Flow;
import com.lbc.ma.structure.Task;
import com.lbc.ma.structure.Workflow;

import java.util.*;

/**
 * 这个类采用某个模板生产workflow
 *
 * @author liubaichuan
 * @since 2018-10-11
 */
public class WorkflowGenerator {
    // 单例模式
    private static WorkflowGenerator workflowGenerator;

    /**
     * 全局workflow id计数
     */
    static int workflow_idx = 0;

    /**
     * 工作流样例
     */
    static Map<Integer, Vector<Flow>> exampleWorkflows = new HashMap<>();

    private WorkflowGenerator() {}

    public static synchronized WorkflowGenerator getWorkflowGenerator(String workflowModelFilePath) {
        if (null == workflowGenerator){
            initialize(workflowModelFilePath);
        }
        return workflowGenerator;
    }

    private static  void initialize(String workflowModelFilePath) {
        workflowGenerator = new WorkflowGenerator();
        String strFileContent = FileTool.getStringFromFile(workflowModelFilePath);

        String[] lineOfContent = strFileContent.split("\r\n");
        for (String line : lineOfContent) {
            String[] lineContent = line.split("\t");
            Integer model_ID = Integer.valueOf(lineContent[1]);
            Integer currTaskID = Integer.valueOf(lineContent[3]);
            Integer succTaskID = Integer.valueOf(lineContent[5]);
            Task currTask = new Task(null, currTaskID, null);
            Task succTask = new Task(null, succTaskID, null);
            Flow flow = new Flow(currTask, succTask, null);
            Vector<Flow> example = exampleWorkflows.get(model_ID);
            if (example != null) {
                example.add(flow);
            } else {
                Vector<Flow> exam = new Vector<>();
                exam.add(flow);
                exampleWorkflows.put(model_ID, exam);
            }
        }
    }

    /**
     * 第二个版本
     * 根据WorkflowGenerator里面的工作流模板获取一个工作流，其工作时长符合指数分布。
     *
     * @param idx WorkflowGenerator模板集合中第idx个模板，idx>=0，当idx大于模板集合的长度时候，对其进行取余操作
     * @return
     */
    public Workflow generateWorkflow(int idx, int taskBaseCap, int taskRdmCap, int baseBandwidth, int rdmBandwidth) {
        if (idx < 0) {
            return null;
        }
        int exampleSetSize = exampleWorkflows.size();
        idx = idx % exampleSetSize;
        Integer WF_ID = ++workflow_idx;
        Workflow wf = new Workflow(WF_ID, null);
        Set<Task> tasks = new HashSet<>();
        Vector<Flow> exampleWorkflow = exampleWorkflows.get(idx);
        Random random = new Random();
        for (Flow f : exampleWorkflow) {
            double bandwidth = random.nextInt(rdmBandwidth) + baseBandwidth;
            double curTaskCap = random.nextInt(taskRdmCap) + taskBaseCap;
            double sucTaskCap = random.nextInt(taskRdmCap) + taskBaseCap;
            Task currTask = new Task(WF_ID, f.currTask.taskId, curTaskCap);
            if (!tasks.add(currTask)) {
                currTask = getTaskFromSet(tasks, f.currTask.taskId);
            }
            Task succTask = new Task(WF_ID, f.succTask.taskId, sucTaskCap);
            if (!tasks.add(succTask)) {
                succTask = getTaskFromSet(tasks, f.succTask.taskId);
            }
            Flow toAddFlow = new Flow(currTask, succTask, bandwidth);
            wf.addFlow(toAddFlow);
        }
        return wf;
    }

    private Task getTaskFromSet(Set<Task> taskSet, Integer taskId) {
        for (Task task : taskSet) {
            if (task.taskId.intValue() == taskId.intValue()) {
                return task;
            }
        }
        return null;
    }

}
