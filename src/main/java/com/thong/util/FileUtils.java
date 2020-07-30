package com.thong.util;

import org.apache.commons.lang3.RandomStringUtils;

import java.io.File;
import java.io.FileWriter;

public class FileUtils {

    public static void createCsv(String fileName) {
        try {

            File file = new File(fileName);
            FileWriter writer = new FileWriter(file);

            StringBuffer stringBuffer = new StringBuffer();
            while (stringBuffer.length() <= 6e6) { // 6mb
                stringBuffer.append("9" + RandomStringUtils.randomNumeric(9));
                stringBuffer.append('\n');
            }
            writer.write(stringBuffer.toString());
            writer.flush();
            writer.close();
            System.out.println("end");
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
