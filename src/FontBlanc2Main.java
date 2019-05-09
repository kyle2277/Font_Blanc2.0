import java.io.*;
import java.sql.Timestamp;

public class FontBlanc2Main {

    public static String file_path;
    public static String file_name;
    public static final String log_path = "./log.txt";
    public static final String encrypt_tag = "encrypted_";
    public static final String decrypt_tag = "decrypted_";
    public static final String encrypt_extension = ".txt";

    public static void main(String[] args) throws IOException {
        long startTime = System.currentTimeMillis();
        splitPath(args[0]);
        String encrypt_key = args[1];
        long fileLength = check_file(file_path + file_name);
        String EorD = args[2];
        Cipher c = new Cipher(encrypt_key, fileLength);
        FileInputStream in = null;
        FileOutputStream out = null;
        try {
            if(EorD.equalsIgnoreCase("encrypt")) {
                in = new FileInputStream(file_path + file_name);
                out = new FileOutputStream(encrypt_tag + file_name + encrypt_extension);
                int encryptCoeff = 1;
                c.distributor(in, out, encryptCoeff);
            } else if(EorD.equalsIgnoreCase("decrypt")) {
                in = new FileInputStream(encrypt_tag + file_name + encrypt_extension);
                out = new FileOutputStream(decrypt_tag + file_name);
                int decryptCoeff = -1;
                c.distributor(in, out, decryptCoeff);
            } else {
                fatal("Invalid action");
            }
        } catch(IOException e) {
            fatal("Output path not found");
        } finally {
            if (in != null) { in.close(); }
            if (out != null) { out.close(); }
        }
        long endTime = System.currentTimeMillis();
        System.out.println("Program execution time: " + (endTime - startTime));
    }

    /*
    Splits the input file path into two components: path to the file name and the file name itself
    */
    public static void splitPath(String file_input) {
        String[] split = file_input.split("/", 0);
        StringBuilder path = new StringBuilder();
        if(split.length > 1) {
            file_path = "";
            //set global file path var
            for(int i = 0; i < split.length - 1; i++) {
                path.append("/");
                path.append(split[i]);
            }
            path.append("/");
        } else {
            path.append("./");
        }
        file_path = path.toString();
        //File name var
        file_name = split[split.length-1];
    }


    /*
    Takes the path of the input file
    Checks if the input file exists and returns the length of the file in bytes
     */
    public static long check_file(String filePath) throws IOException {
        File f = new File(filePath);
        if(f.exists()) {
            return f.length();
        } else {
            System.out.println("File " + filePath + " does not exist.");
            fatal("Input file path does not exist");
            return -1;
        }
    }

    /*
    Triggered if a fatal error occurs. Writes the error to the console and log file
    before program termination
    */
    public static int fatal(String message) throws IOException {
        File fatal = new File(log_path);
        FileWriter out = new FileWriter(fatal, true);
        Timestamp time = new Timestamp(System.currentTimeMillis());
        out.write(time + "\n");
        out.write("Fatal error:\n");
        out.write(message + "\n\n");
        System.out.println("Fatal error:");
        System.out.println(message);
        out.close();
        System.exit(0);
        return 0;
    }
}
