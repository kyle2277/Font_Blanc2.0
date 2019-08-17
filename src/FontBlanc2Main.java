import java.io.*;
import java.sql.Timestamp;

public class FontBlanc2Main {

    public static String log_path = "log.txt";
    public static final String encrypt_tag = "e_";
    public static final String decrypt_tag = "d_";
    public static final String encrypt_extension = ".txt";

    // takes path to the file, encryption key, encrypt or decrypt
    public static void main(String[] args) throws IOException {
        Globals g = new Globals(encrypt_tag, decrypt_tag, encrypt_extension, log_path);
        long startTime = System.currentTimeMillis();
        String filePath = args[0];
        String encryptKey = args[1];
        String EorD = args[2];
        boolean encrypt = EorD.equalsIgnoreCase("encrypt");
        if(!encrypt && !EorD.equalsIgnoreCase("decrypt")) {
            g.fatal("Invalid action.");
        }
        Cipher c = new Cipher(g, filePath, encryptKey, encrypt);

        FileInputStream in = null;
        FileOutputStream out = null;
        int coeff = 0;
        try {
            if(EorD.equalsIgnoreCase("encrypt")) {
                in = new FileInputStream(c.justPath + c.fileName);
                out = new FileOutputStream(c.justPath + g.encryptTag + c.fileName + g.encryptExt);
                coeff = 1;
            } else { //EorD = "decrypt"
                in = new FileInputStream(c.justPath + g.encryptTag + c.fileName + g.encryptExt);
                out = new FileOutputStream(c.justPath + g.decryptTag + c.fileName);
                coeff = -1;
            }
            c.distributor(in, out, coeff);
        } catch(IOException e) {
            g.fatal("Output path not found.");
        } finally {
            if (in != null) { in.close(); }
            if (out != null) { out.close(); }
        }
        long endTime = System.currentTimeMillis();
        System.out.println("Program execution time: " + (endTime - startTime));
    }
}
