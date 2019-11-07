package com.lbc.ma;

import com.lbc.ma.tool.FileTool;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class Main {
    final static String CONFIG_FILE = "config.properties";

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        Properties configProperties = FileTool.loadPropertiesFromFile(CONFIG_FILE);
        MarkovSolution solution = new MarkovSolution(configProperties);
        solution.initializeData();
        solution.markovFunction();
        System.exit(0);
    }
}
