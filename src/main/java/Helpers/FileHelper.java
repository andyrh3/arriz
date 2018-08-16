package Helpers;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class FileHelper {

    public static String rootPath;
    static {
        try {
            rootPath = new File(".").getCanonicalPath();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeStringToFile(String content, File file){
        BufferedWriter bw;
        try {
            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, false), StandardCharsets.UTF_8));
            bw.write(content);
            bw.flush();
            bw.close();
        } catch (FileNotFoundException e) {
            System.out.println("File not found");
        } catch (IOException e) {
            System.out.println("Error initializing stream");
        }
    }

}
