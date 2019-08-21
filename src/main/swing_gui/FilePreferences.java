package main.swing_gui;
import java.util.*;

public class FilePreferences {

    private String fileName;
    private String inPath;
    private String outPath;
    private char[] encryptKey;
    private boolean encrypt;

    public FilePreferences(String filePath, boolean encrypt) {
        inPath = filePath;
        outPath = null;
        this.encrypt = encrypt;
        splitPath(filePath);
    }

    /*
    Splits the input file path into two components: path to the file name and the file name itself
    */
    private void splitPath(String filePath) {
        String[] split = filePath.split("/", 0);
        int splitLen = split.length;
        StringBuilder path = new StringBuilder();
        inPath = "";
        if(splitLen > 1) {
            //set global file path var
            for(int i = 0; i < splitLen - 1; i++) {
                path.append(split[i]);
                path.append("/");
            }
            //path.append("/");
        } else {
            path.append("./");
        }
        inPath = path.toString();
        //File name var
        fileName = split[splitLen - 1];
    }

    private void setOutPath(String outPath) {
        this.outPath = outPath;
    }

    private void setEncryptKey(char[] encryptKey) {
        this.encryptKey = encryptKey;
    }

    private static void clean(FilePreferences fp) {
        Arrays.fill(fp.encryptKey, '0');
    }
}
