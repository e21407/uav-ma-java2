package com.lbc.ma.tool;

import java.io.*;
import java.util.Properties;

public class FileTool {

    public static String getStringFromFile(String filePath) {
        File file = new File(filePath);
        Long filelength = file.length();
        byte[] fileContent = new byte[filelength.intValue()];
        try {
            FileInputStream in = new FileInputStream(file);
            in.read(fileContent);
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String strFileContent = null;
        try {
            strFileContent = new String(fileContent, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return strFileContent;
    }

    public static Properties loadPropertiesFromFile(String propertyFilePath) {
        Properties properties = new Properties();
        InputStream in = null;
        try {
            in = new BufferedInputStream(new FileInputStream(new File(propertyFilePath)));
            properties.load(in);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return  properties;
    }

}
