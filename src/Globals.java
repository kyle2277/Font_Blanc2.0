public class Globals {

    public String file_path;
    public String file_name;
    public String log_path;
    public final String log_name = "../JNI/Font_Blanc2.0/log.txt";
    public final String encrypt_tag = "encrypted_";
    public final String decrypt_tag = "decrypted_";
    public final String encrypt_extension = ".txt";

    public Globals(String filePath, String cwd) {
        log_path = cwd + "/" + log_name;
        splitPath(filePath);
    }

    /*
    Splits the input file path into two components: path to the file name and the file name itself
    */
    public void splitPath(String file_input) {
        String[] split = file_input.split("/", 0);
        int splitLen = split.length;
        StringBuilder path = new StringBuilder();
        file_path = "";
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
        file_path = path.toString();
        //File name var
        file_name = split[splitLen - 1];
    }

}
