package main.swing_gui;

import main.encryption_engine.*;

public class Progress extends Thread {

    private Cipher c;
    private Font_Blanc2_App app;

    public Progress(Font_Blanc2_App app, Cipher c) {
        this.app = app;
        this.c = c;
    }

    public void run() {
        long startTime = System.currentTimeMillis();
        try {
            app.setRunning(true);
            app.setProgressLabel("Working");
            c.start();
            //check bytes remaining to update progress bar
            while(c.getBytesRemaining() > 0) {
                app.setProgressBar((int)(((double)c.getBytesProcessed()/(double)c.getFileLength())*100));
                Thread.sleep(250);
            }
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

}
