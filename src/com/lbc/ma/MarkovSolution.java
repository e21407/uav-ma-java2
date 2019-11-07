package com.lbc.ma;

import com.lbc.ma.structure.*;
import com.lbc.ma.tool.FileTool;
import com.lbc.ma.tool.WorkflowGenerator;
import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.util.Pair;
import org.apache.log4j.Logger;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;

public class MarkovSolution {
    static protected Logger logger = Logger.getLogger(MarkovSolution.class);
    Properties configProperties;
    WorkflowGenerator workflowGenerator;
    static Random random = new Random();

    private static int MAX_THREAD_COUNT = 16;
    private Executor executor = Executors.newFixedThreadPool(MAX_THREAD_COUNT);

    private List<Node> nodes;
    private List<Node> uavNodes;
    private List<Link> links;
    /**
     * <src_dst, pathIds>: <"1_2", [1,2,3,...]>
     */
    private Map<String, List<Integer>> candPathIdFor2Nodes;
    /**
     * <pathId, pathContent>: <1, "1>2>3>5" >
     */
    private Map<Integer, String> paths;

    private List<Workflow> workflows;

    private List<XVar> xVars;

    private List<YVar> yVars;

    public MarkovSolution(Properties properties) {
        this.configProperties = properties;
        nodes = new ArrayList<>();
        uavNodes = new ArrayList<>();
        links = new ArrayList<>();
        candPathIdFor2Nodes = new HashMap<>();
        paths = new HashMap<>();
        workflows = new ArrayList<>();
        xVars = new ArrayList<>();
        yVars = new ArrayList<>();
        workflowGenerator = getWorkflowGenerator();
        generateOriginalWorkflow();
    }

    // ↓↓↓↓↓↓↓↓↓↓↓↓↓read data from file↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓
    public void initializeData() {
        logger.info("开始初始化读取数据...");
        long startTime = System.currentTimeMillis();
        //读取节点信息
        readNodeInfo();
        //读取link信息
        readLinkInfo();
        // 读取路径信息
        readPathInfo();
        // 计算读取准备数据文件的时间
        long endTime = System.currentTimeMillis();
        BigDecimal durTime = new BigDecimal(endTime - startTime);
        durTime = durTime.divide(new BigDecimal(1000));
        logger.info("读取处理文件耗时：" + durTime.setScale(4) + "s");
    }

    private void readLinkInfo() {
        String strFromFile = FileTool.getStringFromFile(configProperties.getProperty("linksInfoFilePath"));
        String[] lines = strFromFile.split("\r\n");
        for (String aLine : lines) {
            if (aLine.trim().equals("")) {
                continue;
            }
            String[] lineContent = aLine.split("\t");
            Integer srcNodeId = Integer.valueOf(lineContent[1]);
            Integer dstNodeId = Integer.valueOf(lineContent[3]);
            Double bandwidth = Double.valueOf(lineContent[5]);
            Link link = new Link(srcNodeId, dstNodeId, bandwidth);
            links.add(link);
        }
    }

    private void readNodeInfo() {
        String strFromFile = FileTool.getStringFromFile(configProperties.getProperty("nodesInfoFilePath"));
        String[] lines = strFromFile.split("\r\n");
        for (String aLine : lines) {
            if (aLine.trim().equals("")) {
                continue;
            }
            String[] lineContent = aLine.split("\t");
            String nodeType = lineContent[0];
            Integer nodeId = Integer.valueOf(lineContent[1]);
            Double nodeCapacity = Double.valueOf(lineContent[3]);
            Node node = new Node(nodeType, nodeId, nodeCapacity);
            nodes.add(node);
            if ("U_ID".equals(nodeType)) {
                uavNodes.add(node);
            }
        }
    }

    private void readPathInfo() {
        int pathId_idx = 1;
        String strFromFile = FileTool.getStringFromFile(configProperties.getProperty("pathsInfoFilePath"));
        String[] lines = strFromFile.split("\r\n");
        for (String aLine : lines) {
            if (aLine.trim().equals("")) {
                continue;
            }
            String[] lineContent = aLine.split("\t");
            int srcNodeId = Integer.valueOf(lineContent[1]);
            int dstNodeId = Integer.valueOf(lineContent[3]);
            String pathContent = lineContent[5];
            paths.put(pathId_idx, pathContent);
            String pathKey = srcNodeId < dstNodeId ? srcNodeId + "_" + dstNodeId : dstNodeId + "_" + srcNodeId;
            List<Integer> pathIds = candPathIdFor2Nodes.get(pathKey);
            if (null == pathIds) {
                pathIds = new ArrayList<>();
                candPathIdFor2Nodes.put(pathKey, pathIds);
            }
            pathIds.add(pathId_idx++);
        }
    }
    // ↑↑↑↑↑↑↑↑↑↑↑↑↑↑read data from file↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑

    private WorkflowGenerator getWorkflowGenerator() {
        String workflowModeFilePath = configProperties.getProperty("workflowModeFilePath");
        WorkflowGenerator workflowGenerator = WorkflowGenerator.getWorkflowGenerator(workflowModeFilePath);
        return workflowGenerator;
    }

    private void generateOriginalWorkflow() {
        int originalWorkflowNum = Integer.valueOf(configProperties.getProperty("originalWorkflowNum"));
        int taskBaseCap = Integer.valueOf(configProperties.getProperty("taskBaseCap"));
        int taskRdmCap = Integer.valueOf(configProperties.getProperty("taskRdmCap"));
        int bandwidthBase = Integer.valueOf(configProperties.getProperty("bandwidthBase"));
        int bandwidthRdm = Integer.valueOf(configProperties.getProperty("bandwidthRdm"));
        while (originalWorkflowNum-- > 0) {
            Workflow workflow = workflowGenerator.generateWorkflow(0, taskBaseCap, taskRdmCap, bandwidthBase, bandwidthRdm);
            workflows.add(workflow);
        }
    }

    public void assignTask() {
        for (Workflow wf : workflows) {
            for (Flow flow : wf.flows) {
                // 分配任务到节点
                int currTaskNodeId = checkWhetherATaskHasAssignment(flow.currTask);
                int succTaskNodeId = checkWhetherATaskHasAssignment(flow.succTask);
                if (-1 == currTaskNodeId) {
                    int nodeIdx = random.nextInt(uavNodes.size());
                    currTaskNodeId = uavNodes.get(nodeIdx).nodeId;
                    // 避免succTask已分配的情况下currTask和succTask分配到同一个节点
                    while (currTaskNodeId == succTaskNodeId) {
                        nodeIdx = random.nextInt(uavNodes.size());
                        currTaskNodeId = uavNodes.get(nodeIdx).nodeId;
                    }
                    XVar xVar = new XVar(wf.workflowId, flow.currTask.taskId, currTaskNodeId);
                    xVars.add(xVar);
                }
                if (-1 == succTaskNodeId) {
                    int nodeIdx = random.nextInt(uavNodes.size());
                    succTaskNodeId = uavNodes.get(nodeIdx).nodeId;
                    // 避免currTask和succTask分配到同一个节点
                    while (currTaskNodeId == succTaskNodeId) {
                        nodeIdx = random.nextInt(uavNodes.size());
                        succTaskNodeId = uavNodes.get(nodeIdx).nodeId;
                    }
                    XVar xVar = new XVar(wf.workflowId, flow.succTask.taskId, succTaskNodeId);
                    xVars.add(xVar);
                }

                // 为flow选择通讯路径
                int pathId = selectRandomPathFor2Nodes(currTaskNodeId, succTaskNodeId);
                YVar yVar = new YVar(wf.workflowId, pathId, flow.currTask.taskId, flow.succTask.taskId);
                yVars.add(yVar);
            }
        }
    }

    /**
     * 检查一个任务是否已经被分配
     *
     * @param task
     * @return 若已分配返回该任务分配的nodeId，否则返回-1
     */
    private int checkWhetherATaskHasAssignment(Task task) {
        for (XVar xVar : xVars) {
            if (xVar.workflowId == task.workflowId && xVar.taskId == task.taskId) {
                return xVar.nodeId;
            }
        }
        return -1;
    }

    /**
     * @param u 开始节点的id
     * @param v 终点节点的id
     * @return 路径id
     */
    private int selectRandomPathFor2Nodes(int u, int v) {
        String pathSetKey = u < v ? u + "_" + v : v + "_" + u;
        List<Integer> candPathIds = candPathIdFor2Nodes.get(pathSetKey);
        if (candPathIds == null) {
            System.out.println("hit");
        }
        int pathIdx = random.nextInt(candPathIds.size());
        return candPathIds.get(pathIdx);
    }

    public SystemMetrics calculateSystemMetrics(List<XVar> xVars, List<YVar> yVars) {
        Map<Integer, Double> nodeLoadInfo = new HashMap<>();
        for (XVar xVar : xVars) {
            int nodeId = xVar.nodeId;
            Node node = findNodeById(nodeId);
            Task task = findTask(xVar.workflowId, xVar.taskId);
            double newNodeLoad = task.neededResource / node.capacity;
            Double nodeLoad = nodeLoadInfo.get(nodeId);
            if (null == nodeLoad) {
                nodeLoad = 0.0;
            }
            nodeLoad += newNodeLoad;
            nodeLoadInfo.put(nodeId, nodeLoad);
        }
        Map<Link, Double> linkLoadInfo = new HashMap<>();
        double throughput = 0;
        for (YVar yVar : yVars) {
            Flow flow = findFlow(yVar.workflowId, yVar.currTaskId, yVar.succTaskId);
            throughput += flow.neededBandwidth;
            String pathContent = paths.get(yVar.pathId);
            String[] nodesInPath = pathContent.split(">");
            for (int i = 0; i < nodesInPath.length - 1; i++) {
                Link link = findLink(Integer.parseInt(nodesInPath[i]), Integer.parseInt(nodesInPath[i + 1]));
                double newLinkLoad = flow.neededBandwidth / link.bandwidth;
                Double linkLoad = linkLoadInfo.get(link);
                if (null == linkLoad) {
                    linkLoad = 0.0;
                }
                linkLoad += newLinkLoad;
                linkLoadInfo.put(link, linkLoad);
            }
        }

        double computeCost = 0;
        for (Map.Entry<Integer, Double> nodeLoadEntry : nodeLoadInfo.entrySet()) {
            computeCost += Math.pow(nodeLoadEntry.getValue(), 2);
        }
        double routingCost = 0;
        for (Map.Entry<Link, Double> linkLoadEntry : linkLoadInfo.entrySet()) {
            routingCost += Math.pow(linkLoadEntry.getValue(), 2);
        }
        return new SystemMetrics(throughput, computeCost, routingCost);
    }

    private Flow findFlow(int wfId, int currTaskId, int succTaskId) {
        for (Workflow wf : workflows) {
            if (wf.workflowId != wfId)
                continue;
            for (Flow f : wf.flows) {
                if (f.currTask.taskId == currTaskId && f.succTask.taskId == succTaskId) {
                    return f;
                }
            }
        }
        return null;
    }

    private Link findLink(int u, int v) {
        for (Link link : links) {
            if (link.srcNodeId == u && link.dstNodeId == v) {
                return link;
            }
            if (link.dstNodeId == u && link.srcNodeId == v) {
                return link;
            }
        }
        return null;
    }

    private Task findTask(int wfId, int taskId) {
        for (Workflow wf : workflows) {
            if (wf.workflowId != wfId) {
                continue;
            }
            Set<Task> tasks = wf.getTasks();
            for (Task t : tasks) {
                if (t.taskId == taskId) {
                    return t;
                }
            }
        }
        return null;
    }

    private Node findNodeById(int nodeId) {
        for (Node node : nodes) {
            if (node.nodeId == nodeId) {
                return node;
            }
        }
        return null;
    }

    private int findNodeIdOfTask(int wfId, int taskId, List<XVar> xVars) {
        for (XVar xVar : xVars) {
            if (xVar.workflowId == wfId && xVar.taskId == taskId) {
                return xVar.taskId;
            }
        }
        return -1;
    }

    public Action selectNextSolution() throws InterruptedException, ExecutionException {
        int count = 16;
        List<List<Flow>> flowList = split(workflows, count);
        CompletionService<List<Pair<Action, Double>>> completionService = new ExecutorCompletionService<>(executor);
        for (List<Flow> flowsToSetAction : flowList) {
            completionService.submit(() -> setActions(flowsToSetAction, xVars, yVars));
        }
        List<Pair<Action, Double>> actions = new ArrayList<>();
        for (int i = 0; i < flowList.size(); i++) {
            List<Pair<Action, Double>> resultActions = completionService.take().get();
            if (null == resultActions) {
                System.out.println("多线程设置Action失败");
                System.exit(-1);
            }
            actions.addAll(resultActions);
        }
        EnumeratedDistribution<Action> distribution = new EnumeratedDistribution<>(actions);
        return distribution.sample();
    }

    private List<Pair<Action, Double>> setActions(List<Flow> flows, List<XVar> xVars, List<YVar> yVars) throws CloneNotSupportedException {
        List<Pair<Action, Double>> ret = new ArrayList<>();
        for (Flow flow : flows) {
            Integer workflowId = flow.currTask.workflowId;
            Task currTask = flow.currTask;
            Task succTask = flow.succTask;
            int nodeIdOfSuccTask = findNodeIdOfTask(succTask.workflowId, succTask.taskId, xVars);
            int nodeIdx = random.nextInt(nodes.size());
            Node newNodeForSuccTask = nodes.get(nodeIdx);
            // 避免分配到原来节点
            while (nodeIdOfSuccTask == newNodeForSuccTask.nodeId) {
                nodeIdx = random.nextInt(nodes.size());
                newNodeForSuccTask = nodes.get(nodeIdx);
            }
            List<XVar> xVarsCopy = new ArrayList<>();
            for (XVar x : xVars) {
                xVarsCopy.add((XVar) x.clone());
            }
            removeXVar(workflowId, succTask.taskId, xVarsCopy);
            XVar newXVar = new XVar(workflowId, succTask.taskId, newNodeForSuccTask.nodeId);
            xVarsCopy.add(newXVar);

            List<YVar> yVarsCopy = new ArrayList<>();
            for (YVar y : yVars) {
                yVarsCopy.add((YVar) y.clone());
            }
            removeYVar(workflowId, currTask.taskId, succTask.taskId, yVarsCopy);
            int nodeIdOfCurrTask = findNodeIdOfTask(workflowId, currTask.taskId, xVarsCopy);
            int newPathId = selectRandomPathFor2Nodes(nodeIdOfCurrTask, newNodeForSuccTask.nodeId);
            YVar newYVar = new YVar(workflowId, newPathId, currTask.taskId, succTask.taskId);
            yVarsCopy.add(newYVar);

            SystemMetrics oldSystemMetrics = calculateSystemMetrics(xVars, yVars);
            SystemMetrics newSystemMetrics = calculateSystemMetrics(xVarsCopy, yVarsCopy);
            double improvement = calculateImprovement(oldSystemMetrics, newSystemMetrics);
            Action action = new Action(workflowId, currTask.taskId, succTask.taskId, newPathId, nodeIdOfSuccTask, newNodeForSuccTask.nodeId);
            Pair<Action, Double> pair = new Pair<>(action, improvement);
            ret.add(pair);
        }
        return ret;
    }

    private double calculateImprovement(SystemMetrics oldSystemMetrics, SystemMetrics newSystemMetrics) {
        double oldPerformance = oldSystemMetrics.getPerformance();
        double newPerformance = newSystemMetrics.getPerformance();
        double beta = Double.parseDouble((String) configProperties.get("beta"));
        double ret = Math.exp(0.5 * beta * (newPerformance - oldPerformance));
        ret = Math.min(ret, Double.MAX_VALUE);
        ret = Math.max(ret, Double.MIN_VALUE);
        return ret;
    }

    private void removeYVar(int wfId, int currTaskId, int succTaskId, List<YVar> yVars) {
        Iterator<YVar> iterator = yVars.iterator();
        while (iterator.hasNext()) {
            YVar yVar = iterator.next();
            if (yVar.workflowId == wfId && yVar.currTaskId == currTaskId && yVar.succTaskId == succTaskId) {
                iterator.remove();
                return;
            }
        }
    }

    private void removeXVar(int wfId, int taskId, List<XVar> xVars) {
        Iterator<XVar> iterator = xVars.iterator();
        while (iterator.hasNext()) {
            XVar xVar = iterator.next();
            if (xVar.workflowId == wfId && xVar.taskId == taskId) {
                iterator.remove();
                return;
            }
        }
    }

    /**
     * 拆分工作流集合
     *
     * @param workflows 要拆分的工作流
     * @param count     每个集合元素个数
     * @return 返回拆分后的各个flow集合
     */
    public List<List<Flow>> split(List<Workflow> workflows, int count) {
        if (null == workflows) {
            return null;
        }
        List<Flow> flows = new ArrayList<>();
        List<List<Flow>> ret = new ArrayList<>();
        for (Workflow wf : workflows) {
            flows.addAll(wf.flows);
        }
        int size = flows.size();
        if (size <= count) {
            ret.add(flows);
        } else {
            int pre = size / count;
            int last = size % count;
            // 前pre个集合，每个大小都是count个元素
            for (int i = 0; i < pre; i++) {
                List<Flow> itemList = new ArrayList<>();
                for (int j = 0; j < count; j++) {
                    itemList.add(flows.get(i * count + j));
                }
                ret.add(itemList);
            }
            if (last > 0) {
                List<Flow> itemList = new ArrayList<>();
                for (int i = 0; i < last; i++) {
                    itemList.add(flows.get(pre * count + i));
                }
                ret.add(itemList);
            }
        }
        return ret;
    }

    private void printSystemMetrics(int t, SystemMetrics metrics) {
        double performance = metrics.getPerformance();
        double throughput = metrics.throughput;
        double computeCost = metrics.computeCost;
        double routingCost = metrics.routingCost;
        String infoStr = String.format("t: %d\tp: %.2f\tthr: %.2f\tcCost: %.2f\trCost: %.2f", t, performance, throughput,
                computeCost, routingCost);
        System.out.println(infoStr);
        // todo log
    }

    private void doAction(Action action) {
        removeXVar(action.wfId, action.succTaskId, this.xVars);
        XVar xVar = new XVar(action.wfId, action.succTaskId, action.newNodeForSuccTask);
        this.xVars.add(xVar);
        removeYVar(action.wfId, action.currTaskId, action.succTaskId, this.yVars);
        YVar yVar = new YVar(action.wfId, action.newPathId, action.currTaskId, action.succTaskId);
        this.yVars.add(yVar);
    }

    private void printNodeLoadInfo(List<XVar> xVars) {
        String[] nodeInfo = new String[nodes.size()];
        for(int i = 0; i < nodeInfo.length; i++){
            nodeInfo[i] = "";
        }
        for (XVar xVar : xVars) {
            String loadInfo = "(" + xVar.workflowId + "," + xVar.taskId + ")";
            nodeInfo[xVar.nodeId-1] += loadInfo + "\t";
        }
        for (int i = 0; i < nodeInfo.length; i++) {
            System.out.print("[" + (i + 1) + "]: ");
            System.out.println(nodeInfo[i]);
        }
    }

    public void markovFunction() throws ExecutionException, InterruptedException {
        assignTask();
        int T = Integer.parseInt(configProperties.getProperty("iterationCount"));
        int t = 0;
        SystemMetrics systemMetrics = calculateSystemMetrics(this.xVars, this.yVars);
        printSystemMetrics(t, systemMetrics);
        while (t++ < T) {
            Action action = selectNextSolution();
            doAction(action);
            systemMetrics = calculateSystemMetrics(this.xVars, this.yVars);
            printSystemMetrics(t, systemMetrics);
        }
        printNodeLoadInfo(this.xVars);
    }

    /////////////////////////////////////////////////////////////////////////
    public List<Node> getNodes() {
        return nodes;
    }

    public List<Link> getLinks() {
        return links;
    }

    public Map<Integer, String> getPaths() {
        return paths;
    }

    public List<Workflow> getWorkflows() {
        return workflows;
    }

    public List<XVar> getxVars() {
        return xVars;
    }

    public List<YVar> getyVars() {
        return yVars;
    }
}
