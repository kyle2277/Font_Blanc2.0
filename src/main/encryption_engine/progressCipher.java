package main.encryption_engine;

import main.swing_gui.Font_Blanc2_App;
import java.util.*;
import java.io.*;

/*
A subclass of Cipher that updates progress of the encryption and supports multithreading
 */
public class progressCipher extends Cipher implements Runnable {

    private Font_Blanc2_App app;

    public progressCipher(Globals g, String fileName, String fileInPath, String fileOutPath,
                          boolean encrypt, Deque<Instruction> instructions, Font_Blanc2_App app) {
        super(g, fileName, fileInPath, fileOutPath, encrypt, instructions);
        this.app = app;
    }

    @Override
    public void run() {
        long startTime = System.currentTimeMillis();
        try {
            app.setRunning(true);
            app.setProgressLabel("Working");
            if(isEncrypt()) {   writeOut(getInstructions());    }
            app.setProgressBar(0);
            execute();
            app.setProgressBar(100);
            app.setProgressLabel("Done");
            System.out.println("Done");
            this.close();
            long endTime = System.currentTimeMillis();
            System.out.println("Program execution time: " + ((double)endTime - (double)startTime)/1000 + "s");
        } catch (Exception e) {
            System.out.println("Thread interrupted");
            e.printStackTrace();
            System.exit(1);
        }
        app.cleanCurFile();
        app.setRunning(false);
    }

    @Override
    protected void permutCipher(int dimension) {
        super.permutCipher(dimension);
        app.setProgressBar((int)(((double)getBytesProcessed()/(double)getFileLength())*100));
    }

    private void appendBuffer(StringBuilder strBuff, Instruction i) {
        strBuff.append(i.getEncryptKey());
        strBuff.append(" - ");
        strBuff.append(i.getDimension());
        strBuff.append("\n");
    }

    private void writeOut(Deque<Instruction> instructions) {
        StringBuilder strBuff = new StringBuilder();
        for (Instruction i : instructions) {
            appendBuffer(strBuff, i);
        }
        String writeBuffer = strBuff.toString();
        String [] execute = {"./FB_WO", "Encrypted", getJustPath(), getFileName(), writeBuffer};
        ProcessBuilder p = new ProcessBuilder(execute);
        String cwd = System.getProperty("user.dir");
        p.directory(new File(cwd));
        try {
            Process process = p.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            int exitCode = -10;
            try {
                exitCode = process.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("\nExited with error code: " + exitCode);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
