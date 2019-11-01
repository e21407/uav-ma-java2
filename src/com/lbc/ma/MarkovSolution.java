package com.lbc.ma;

import com.lbc.ma.structure.Link;
import com.lbc.ma.structure.Node;
import com.lbc.ma.structure.Workflow;
import com.lbc.ma.tool.FileTool;
import com.lbc.ma.tool.WorkflowGenerator;
import org.apache.log4j.Logger;

import java.math.BigDecimal;
import java.util.*;

public class MarkovSolution {
    static protected Logger logger = Logger.getLogger(MarkovSolution.class);
    Properties configProperties;

    private List<Node> nodes;
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

    public MarkovSolution(Properties properties) {
        this.configProperties = properties;
        nodes = new ArrayList<>();
        links = new ArrayList<>();
        candPathIdFor2Nodes = new HashMap<>();
        paths = new HashMap<>();
        workflows = new ArrayList<>();
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

    private void generateWorkflow(){
        WorkflowGenerator workflowGenerator = WorkflowGenerator.getWorkflowGenerator();

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
}
