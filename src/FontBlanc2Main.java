import java.io.*;
import java.sql.Timestamp;

public class FontBlanc2Main {

    public static int main(String filePath, String encryptKey, String EorD, String cwd) throws IOException {
        long startTime = System.currentTimeMillis();
        Globals g = new Globals(filePath, cwd);
        if(!EorD.equalsIgnoreCase("encrypt") && !EorD.equalsIgnoreCase("decrypt")) {
            return fatal(g, "Invalid action");
        }
        long fileLength = checkThisFile(g, EorD);
        Cipher c = new Cipher(encryptKey, fileLength);
        FileInputStream in = null;
        FileOutputStream out = null;
        int coeff = 0;
        try {
            if(EorD.equalsIgnoreCase("encrypt")) {
                in = new FileInputStream(g.file_path + g.file_name);
                out = new FileOutputStream(g.file_path + g.encrypt_tag + g.file_name + g.encrypt_extension);
                coeff = 1;
            } else { //EorD = "decrypt"
                in = new FileInputStream(g.file_path + g.encrypt_tag + g.file_name + g.encrypt_extension);
                out = new FileOutputStream(g.file_path + g.decrypt_tag + g.file_name);
                coeff = -1;
            }
            c.distributor(in, out, coeff);
        } catch(IOException e) {
            return fatal(g, "Output path not found");
        } finally {
            if (in != null) { in.close(); }
            if (out != null) { out.close(); }
        }
        long endTime = System.currentTimeMillis();
        System.out.println("Program execution time: " + (endTime - startTime));
        return 1;
    }

    /*
    Takes the path of the input file
    Checks if the input file exists and returns the length of the file in bytes
     */
    public static long checkThisFile(Globals g, String EorD) {
        File f;
        String fileName;
        if(EorD.equalsIgnoreCase("encrypt")) {
            fileName = g.file_path + g.file_name;
            f = new File(fileName);
        } else { //EorD = "decrypt"
            fileName = g.file_path + g.encrypt_tag + g.file_name + g.encrypt_extension;
            f = new File(fileName);
        }
        if(f.exists()) {
            return f.length();
        } else {
            return fatal(g, "File " + fileName + " does not exist");
        }
    }

    /*
    Triggered if a fatal error occurs. Writes the error to the console and log file
    before program termination
    */
    public static int fatal(Globals g, String message) {
        try {
            File fatal = new File(g.log_path);
            FileWriter out = new FileWriter(fatal, true);
            Timestamp time = new Timestamp(System.currentTimeMillis());
            out.write(time + "\n");
            out.write("Fatal error:\n");
            out.write(message + "\n\n");
            out.close();
        } catch(IOException e) {
            System.out.println("No log file found at " + g.log_path);
        } finally {
            System.out.println("Fatal error:");
            System.out.println(message);
        }
        return 0;
    }
}
