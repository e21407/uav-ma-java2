import com.lbc.ma.MarkovSolution;
import com.lbc.ma.structure.Link;
import com.lbc.ma.structure.Node;
import com.lbc.ma.tool.FileTool;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.Properties;

public class TestFunctional {
    final static String CONFIG_FILE = "config.properties";

    @Test
    public void testReadData(){
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
    }

}
