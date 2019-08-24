package main.encryption_engine;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;

public class Globals {

    public String encryptTag;
    public String decryptTag;
    public String encryptExt;
    public String logPath;

    public Globals(String encryptTag, String decryptTag, String encryptExt, String logPath) {
        this.encryptTag = encryptTag;
        this.decryptTag = decryptTag;
        this.encryptExt = encryptExt;
        this.logPath = logPath;
    }

    /*
    Triggered if a fatal error occurs. Writes the error to the console and log file
    before program termination
    */
    public void fatal(String message) {
        try {
            File fatal = new File(logPath + "log.txt");
            FileWriter out = new FileWriter(fatal, true);
            Timestamp time = new Timestamp(System.currentTimeMillis());
            out.write(time + "\n");
            out.write("Fatal error:\n");
            out.write(message + "\n\n");
            out.close();
        } catch(IOException e) {
            System.out.println("No log file found.");
        } finally {
            System.out.println("Fatal error:");
            System.out.println(message);
            System.out.println("Aborting.");
        }
        System.exit(1);
    }

}
