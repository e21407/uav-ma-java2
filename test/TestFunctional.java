import com.lbc.ma.MarkovSolution;
import com.lbc.ma.structure.*;
import com.lbc.ma.tool.FileTool;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.ExecutionException;

public class TestFunctional {
    final static String CONFIG_FILE = "config.properties";

    @Test
    public void testReadData() throws ExecutionException, InterruptedException {
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
        for (Workflow wf : solution.getWorkflows()) {
            taskNum += wf.getTasks().size();
            flowNum += wf.flows.size();
        }
        Assert.assertEquals(taskNum, solution.getxVars().size());
        Assert.assertEquals(flowNum, solution.getyVars().size());
        // calculate system metrics
        SystemMetrics systemMetrics = solution.calculateSystemMetrics(solution.getxVars(), solution.getyVars());
        Assert.assertNotNull(systemMetrics);
        // switch solution
        Action action = solution.selectNextSolution();
        Assert.assertNotNull(action);

    }

    @Test
    public void testSomething() {
        Set<Task> set = new HashSet<>();
        Task task = new Task(1, 3, 90.0);
        set.add(task);
        for (Task t : set) {
            System.out.println(t.workflowId + " " + t.taskId + " " + t.neededResource);
        }
        task.neededResource = 66.6;
        for (Task t : set) {
            System.out.println(t.workflowId + " " + t.taskId + " " + t.neededResource);
        }
    }

    @Test
    public void teatExp(){
        int i = 0;
        double result = 0;
        while (result < Double.MAX_VALUE){
            result = Math.exp(i);
            System.out.println(result);
            i++;
        }
        System.out.println(i);
    }

    @Test
    public void testScale(){
        int MaxScale = 711;

    }

}
