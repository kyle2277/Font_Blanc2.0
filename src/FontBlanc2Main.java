import java.io.*;
import java.sql.Timestamp;

public class FontBlanc2Main {

    public static String file_path;
    public static String file_name;
    public static final String log_path = "./log.txt";
    public static final String encrypt_tag = "encrypted_";
    public static final String decrypt_tag = "decrypted_";
    public static final String encrypt_extension = ".txt";

    public static int main(String filePath, String encryptKey, String EorD) throws IOException {
        long startTime = System.currentTimeMillis();
        splitPath(filePath);
        long fileLength = checkThisFile();
        Cipher c = new Cipher(encryptKey, fileLength);
        FileInputStream in = null;
        FileOutputStream out = null;
        int coeff = 0;
        try {
            if(EorD.equalsIgnoreCase("encrypt")) {
                in = new FileInputStream(file_path + file_name);
                out = new FileOutputStream(encrypt_tag + file_name + encrypt_extension);
                coeff = 1;
            } else if(EorD.equalsIgnoreCase("decrypt")) {
                in = new FileInputStream(encrypt_tag + file_name + encrypt_extension);
                out = new FileOutputStream(decrypt_tag + file_name);
                coeff = -1;
            } else {
                return fatal("Invalid action");
            }
            c.distributor(in, out, coeff);
        } catch(IOException e) {
            return fatal("Output path not found");
        } finally {
            if (in != null) { in.close(); }
            if (out != null) { out.close(); }
        }
        long endTime = System.currentTimeMillis();
        System.out.println("Program execution time: " + (endTime - startTime));
        return 1;
    }

    /*
    Splits the input file path into two components: path to the file name and the file name itself
    */
    public static void splitPath(String file_input) {
        String[] split = file_input.split("/", 0);
        int splitLen = split.length;
        StringBuilder path = new StringBuilder();
        file_path = "";
        if(splitLen > 1) {
            //set global file path var
            for(int i = 0; i < splitLen - 1; i++) {
                path.append("/");
                path.append(split[i]);
            }
            path.append("/");
        } else {
            path.append("./");
        }
        file_path = path.toString();
        //File name var
        file_name = split[splitLen - 1];
    }


    /*
    Takes the path of the input file
    Checks if the input file exists and returns the length of the file in bytes
     */
    public static long checkThisFile() throws IOException {
        File f = new File(file_path + file_name);
        if(f.exists()) {
            return f.length();
        } else {
            fatal("Input file " + file_path + file_name + " path does not exist");
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
        return -1;
    }
}
