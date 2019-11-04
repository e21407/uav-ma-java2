import com.lbc.ma.MarkovSolution;
import com.lbc.ma.structure.Link;
import com.lbc.ma.structure.Node;
import com.lbc.ma.structure.Task;
import com.lbc.ma.structure.Workflow;
import com.lbc.ma.tool.FileTool;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;

public class TestFunctional {
    final static String CONFIG_FILE = "config.properties";

    @Test
    public void testReadData() {
        Properties configProperties = FileTool.loadPropertiesFromFile(CONFIG_FILE);
        MarkovSolution solution = new MarkovSolution(configProperties);
        solution.initializeData();
        // read path information
        Map<Integer, String> pathDatabase = solution.getPaths();
        String pathsInfoFilePath = configProperties.getProperty("pathsInfoFilePath");
        String[] split = FileTool.getStringFromFile(pathsInfoFilePath).split("\r\n");
        Assert.assertEquals(split.length, pathDatabase.size());
        // read node information
        List<Node> nodes = solution.getNodes();
        String nodesInfoFilePath = configProperties.getProperty("nodesInfoFilePath");
        split = FileTool.getStringFromFile(nodesInfoFilePath).split("\r\n");
        Assert.assertEquals(split.length, nodes.size());
        // read link information
        List<Link> links = solution.getLinks();
        String linksInfoFilePath = configProperties.getProperty("linksInfoFilePath");
        split = FileTool.getStringFromFile(linksInfoFilePath).split("\r\n");
        Assert.assertEquals(split.length, links.size());
        // assign task
        solution.assignTask();
        int taskNum = 0;
        int flowNum = 0;
        for(Workflow wf : solution.getWorkflows()){
            taskNum += wf.getTasks().size();
            flowNum += wf.flows.size();
        }
        Assert.assertEquals(taskNum, solution.getxVars().size());
        Assert.assertEquals(flowNum, solution.getyVars().size());
    }

    @Test
    public void testSomething() {
        Set<Task> set = new HashSet<>();
        Task task = new Task(1,3,90.0);
        set.add(task);
        for(Task t : set){
            System.out.println(t.workflowId + " " + t.taskId + " " + t.neededResource);
        }
        task.neededResource = 66.6;
        for(Task t : set){
            System.out.println(t.workflowId + " " + t.taskId + " " + t.neededResource);
        }
    }

}
