package com.lbc.ma;

import com.lbc.ma.structure.*;
import com.lbc.ma.tool.FileTool;
import com.lbc.ma.tool.WorkflowGenerator;
import org.apache.log4j.Logger;

import java.math.BigDecimal;
import java.util.*;

public class MarkovSolution {
    static protected Logger logger = Logger.getLogger(MarkovSolution.class);
    Properties configProperties;
    WorkflowGenerator workflowGenerator;
    static Random random = new Random();

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

    private Set<XVar> xVars;

    private Set<YVar> yVars;

    public MarkovSolution(Properties properties) {
        this.configProperties = properties;
        nodes = new ArrayList<>();
        uavNodes = new ArrayList<>();
        links = new ArrayList<>();
        candPathIdFor2Nodes = new HashMap<>();
        paths = new HashMap<>();
        workflows = new ArrayList<>();
        xVars = new HashSet<>();
        yVars = new HashSet<>();
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

    private int selectRandomPathFor2Nodes(int u, int v) {
        String pathSetKey = u < v ? u + "_" + v : v + "_" + u;
        List<Integer> candPathIds = candPathIdFor2Nodes.get(pathSetKey);
        if (candPathIds == null) {
            System.out.println("hit");
        }
        int pathIdx = random.nextInt(candPathIds.size());
        return candPathIds.get(pathIdx);
    }

    // todo 人工检查一下计算结果是否正确
    public SystemMetrics calculateSystemMetrics(Set<XVar> xVars, Set<YVar> yVars) {
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
            for (int i = 0; i < nodesInPath.length - 1; i++){
                Link link = findLink(Integer.valueOf(nodesInPath[i]), Integer.valueOf(nodesInPath[i+1]));
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

    private int findNodeIdOfTask(int wfId, int taskId, Set<XVar> xVars) {
        for (XVar xVar : xVars) {
            if (xVar.workflowId == wfId && xVar.taskId == taskId) {
                return xVar.taskId;
            }
        }
        return -1;
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


    public Set<XVar> getxVars() {
        return xVars;
    }

    public Set<YVar> getyVars() {
        return yVars;
    }
}
