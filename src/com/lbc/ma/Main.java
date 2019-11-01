package com.lbc.ma;

import com.lbc.ma.tool.FileTool;

import java.util.Properties;

public class Main {
    final static String CONFIG_FILE = "config.properties";

    public static void main(String[] args) {
        Properties configProperties = FileTool.loadPropertiesFromFile(CONFIG_FILE);
        System.out.println(configProperties.getProperty("linkInfoFilePath"));
    }
}
