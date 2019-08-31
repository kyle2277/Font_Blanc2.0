package main.encryption_engine;

import main.swing_gui.FilePreferences;

import java.io.*;
import java.util.*;

public class FontBlanc2Main {

    public static String log_path = "log.txt";
    public static final String encrypt_tag = "e_";
    public static final String decrypt_tag = "d_";
    public static final String encrypt_extension = ".txt";

    //merged
    // takes path to the file, encryption key, encrypt or decrypt
    public static void main(String[] args) throws IOException {
        Globals g = new Globals(encrypt_tag, decrypt_tag, encrypt_extension, log_path);
        long startTime = System.currentTimeMillis();
        String filePath = args[0];
        char[] encryptKey = args[1].toCharArray();
        String EorD = args[2];
        boolean encrypt = EorD.equalsIgnoreCase("encrypt");
        if(!encrypt && !EorD.equalsIgnoreCase("decrypt")) {
            g.fatal("Invalid action.");
        }
        FilePreferences fp = new FilePreferences(filePath, encrypt);
        Deque<Instruction> instructions = new LinkedList<>();
        Instruction i = new Instruction( 0, encryptKey);
        instructions.add(i);
        fp.setInstructions(instructions);
        Cipher c = new Cipher(g, fp.getFileName(), fp.getInPath(), fp.getOutPath(), encrypt, fp.getInstructions());
        c.execute();
        long endTime = System.currentTimeMillis();
        System.out.println("Program execution time: " + ((double)endTime - (double)startTime)/1000 + "s");
    }
}
