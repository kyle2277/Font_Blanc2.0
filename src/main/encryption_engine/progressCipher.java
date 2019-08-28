package main.encryption_engine;

import main.swing_gui.Font_Blanc2_App;
import java.io.*;

/*
A subclass of Cipher that updates progress of the encryption and supports multithreading
 */
public class progressCipher extends Cipher implements Runnable {

    private Font_Blanc2_App app;

    public progressCipher(Globals g, String fileName, String fileInPath, String fileOutPath, char[] encryptKey, boolean encrypt, Font_Blanc2_App app) {
        super(g, fileName, fileInPath, fileOutPath, encryptKey, encrypt);
        this.app = app;
    }

    @Override
    public void run() {
        long startTime = System.currentTimeMillis();
        try {
            app.setRunning(true);
            app.setProgressLabel("Working");
            app.setProgressBar(0);
            distributor();
            app.setProgressBar(100);
            app.setProgressLabel("Done");
            System.out.println("Done");
            long endTime = System.currentTimeMillis();
            System.out.println("Program execution time: " + ((double)endTime - (double)startTime)/1000 + "s");
        } catch (Exception e) {
            System.out.println("Thread interrupted");
            System.exit(1);
        }
        app.cleanCurFile();
        app.setRunning(false);
    }

    @Override
    protected void permutCipher(int dimension, FileInputStream in, FileOutputStream out) throws IOException {
        super.permutCipher(dimension, in, out);
        app.setProgressBar((int)(((double)getBytesProcessed()/(double)getFileLength())*100));
    }

}