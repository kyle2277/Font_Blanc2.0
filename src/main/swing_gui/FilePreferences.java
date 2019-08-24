package main.swing_gui;

import java.io.*;
import java.util.*;

public class FilePreferences {

    private String fileName;
    private String inPath;
    private String outPath;
    private char[] encryptKey;
    private boolean encrypt;

    public FilePreferences(String filePath, boolean encrypt) {
        this.encrypt = encrypt;
        splitPath(filePath);
        outPath = inPath;
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

    public String getFileName() {
        return fileName;
    }

    public String getInPath() {
        return inPath;
    }

    public String getOutPath() {
        return outPath;
    }

    public char[] getEncryptKey() {
        return encryptKey;
    }

    public boolean isEncrypt() {
        return encrypt;
    }

    public void setOutPath(String outPath) {
        this.outPath = pathExists(outPath) ? outPath : null;
    }

    private boolean pathExists(String path) {
        return new File(path).exists();
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setEncryptKey(char[] encryptKey) {
        this.encryptKey = encryptKey;
    }

    public void setEncrypt(boolean encrypt) {
        this.encrypt = encrypt;
    }

    public static void clean(FilePreferences fp) {
        Arrays.fill(fp.encryptKey, '0');
    }
}
