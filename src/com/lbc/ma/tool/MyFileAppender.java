package com.lbc.ma.tool;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.spi.ErrorCode;
/**
 * FileAppender是log4j包中的类，这里需要继承它
 * @author tang
 */
public class MyFileAppender extends FileAppender {

    public String LoggerNamePrefix = getCurrentClassName();
    private static final String DOT = ".";
    private static final String UNDERLINE = "_";

    public MyFileAppender() {
    }

    public MyFileAppender(Layout layout, String filename, boolean append,
                          boolean bufferedIO, int bufferSize) throws IOException {
        super(layout, filename, append, bufferedIO, bufferSize);
    }

    public MyFileAppender(Layout layout, String filename, boolean append)
            throws IOException {
        super(layout, filename, append);
    }

    public MyFileAppender(Layout layout, String filename) throws IOException {
        super(layout, filename);
    }

    /**
     * 这里获取执行主程序的类名,作为日志名称前缀
     * @author tang
     */
    public static String getCurrentClassName() {
        StackTraceElement[] ste = Thread.currentThread().getStackTrace();
        String className = ste[ste.length - 1].getClassName();
        int lastIndexOf = className.lastIndexOf(".") + 1;
        String currentClassName = className.substring(lastIndexOf);
        return currentClassName;
    }

    /**
     * 父类方法 fileName路径后面有个.log
     * @author tang
     */
    public void activateOptions() {
        if (fileName != null) {
            try {
                fileName = getNewLogFileName();
                setFile(fileName, fileAppend, bufferedIO, bufferSize);
            } catch (Exception e) {
                errorHandler.error("Error while activating log options", e,
                        ErrorCode.FILE_OPEN_FAILURE);
            }
        }
    }

    /**
     * 获取下一个要生成的日志的全路径
     * @return
     */
    private String getNewLogFileName() {
        if (fileName != null) {
            final String LEFTPARENTHESIS = "(";
            final String RIGHTPARENTHESIS = ")";
            final File logFile = new File(fileName);
            final String fileName = logFile.getName();
            final int dotIndex = fileName.indexOf(DOT);
            String newFileName = "";
            Integer number = -1;
            File[] files = logFile.getParentFile().listFiles(new CustomFilter());
            Pattern pattern = Pattern.compile("(?<=\\()[\\d]+");
            // 首次创建为false
            if (files != null && files.length > 0) {
                number++;
                for (File file : files) {
                    Matcher matcher = pattern.matcher(file.getName());
                    if (matcher.find()) {
                        if (number < Integer.valueOf(matcher.group(0))) {
                            number = Integer.valueOf(matcher.group(0));
                        }
                    }
                }
            } else {
                // 避免首次名称bug，补充else
                number++;
            }
            // 设置日志名
            if (dotIndex != -1) {
                String tempFileName = fileName.substring(0, dotIndex);
                final int parenthesis = tempFileName.indexOf(LEFTPARENTHESIS);
                if (parenthesis != -1) {
                    tempFileName = tempFileName.substring(parenthesis);
                }
                if (number > -1) {
                    newFileName = LoggerNamePrefix + UNDERLINE + tempFileName
                            + LEFTPARENTHESIS + (++number) + RIGHTPARENTHESIS
                            + fileName.substring(dotIndex);
                } else {
                    newFileName = LoggerNamePrefix + UNDERLINE + tempFileName
                            + fileName.substring(dotIndex);
                }
            } else {
                // 是否存在文件名中存在()
                if (number > -1) {
                    newFileName = LoggerNamePrefix + UNDERLINE + fileName
                            + LEFTPARENTHESIS + (++number) + RIGHTPARENTHESIS;
                } else {
                    newFileName = LoggerNamePrefix + UNDERLINE + fileName;
                }
            }
            // 格式化日志名
            return logFile.getParent() + File.separator + newFileName;
        }
        return null;
    }

    // 获取当前文件夹下的日志文件
    class CustomFilter implements FilenameFilter {
        public boolean accept(File dir, String name) {
            File logFile = new File(fileName);
            String fileName = logFile.getName();
            int indexDot = fileName.lastIndexOf(DOT);
            if (indexDot != -1) {
                return name.startsWith(LoggerNamePrefix + UNDERLINE
                        + fileName.substring(0, indexDot));
            } else {
                return name.startsWith(LoggerNamePrefix + UNDERLINE + fileName);
            }
        }
    }
}