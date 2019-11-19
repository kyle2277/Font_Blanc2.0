package main.swing_gui;

import main.encryption_engine.*;

import java.io.*;
import java.util.*;

public class FilePreferences {

    private String fileName;
    private String inPath;
    private String outPath;
    private char[] encryptKey;
    private boolean encrypt;
    private long fileLength;
    private Deque<Instruction> instructions;

    public FilePreferences(String filePath, boolean encrypt, long fileLength) {
        this.encrypt = encrypt;
        this.fileLength = fileLength;
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

    public void setInstructions(Deque<Instruction> instructions) {
        this.instructions = instructions;
    }

    public Deque<Instruction> getInstructions() {
        return instructions;
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

    public long getFileLength() {
        return fileLength;
    }

    public static void clean(FilePreferences fp) {
        // TODO clear entire encrypt key array on advanced reset
        Arrays.fill(fp.encryptKey, '0');
    }
}
