package com.lbc.ma;

import com.lbc.ma.tool.FileTool;
import org.apache.log4j.Logger;

import java.math.BigDecimal;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class Main {
    final static String CONFIG_FILE = "config.properties";
    static protected Logger logger = Logger.getLogger(MarkovSolution.class);

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        long startTime = System.currentTimeMillis();
        Properties configProperties = FileTool.loadPropertiesFromFile(CONFIG_FILE);
        MarkovSolution solution = new MarkovSolution(configProperties);
        solution.initializeData();
        solution.markovFunction();
        // 计算程序运行时间
        long endTime = System.currentTimeMillis();
        BigDecimal durTime = new BigDecimal(endTime - startTime);
        durTime = durTime.divide(new BigDecimal(1000));
        logger.info("# 程序运行耗时：" + durTime.setScale(4) + "s");
        System.exit(0);
    }
}
