import java.util.*;
import java.io.*;

import org.ejml.data.*;
import org.ejml.ops.*;
import org.ejml.sparse.csc.CommonOps_DSCC;

import java.lang.Math;
import java.sql.Timestamp;

public class PermutMatrixTest {

    public static String encrypt_key;
    public static int encrypt_key_val;
    public static HashMap<Integer, DMatrixSparseTriplet> permut_map;
    public static long bytes_processed;
    public static long bytes_remaining;
    public static long file_length;

    public static final String log_path = "./log.txt";
    public static final String encrypt_tag = "encrypted_";
    public static final String decrypt_tag = "decrypted_";
    public static final String encrypt_extension = ".txt";

    public static void main(String[] args) throws IOException {
        String filePath = args[0];
        encrypt_key = args[1];
        encrypt_key_val = getEncryptKeyVal();
        System.out.println("Encrypt key: " + encrypt_key);
        bytes_processed = 0;
        String EorD = args[2];
        permut_map = new HashMap<Integer, DMatrixSparseTriplet>();
        file_length = check_file(filePath);
        bytes_remaining = file_length;
        FileInputStream in = null;
        FileOutputStream out = null;
        try {
            if(EorD.equalsIgnoreCase("encrypt")) {
                in = new FileInputStream(filePath);
                out = new FileOutputStream(encrypt_tag + filePath + encrypt_extension);
                int encryptCoeff = 1;
                distributor(in, out, encryptCoeff);

            } else if(EorD.equalsIgnoreCase("decrypt")) {
                in = new FileInputStream(encrypt_tag + filePath + encrypt_extension);
                out = new FileOutputStream(decrypt_tag + filePath);
                int decryptCoeff = -1;
                distributor(in, out, decryptCoeff);
            } else {
                fatal("Invalid action");
            }
        } catch(IOException e) {
            fatal("Output path not found");
        } finally {
            if (in != null) { in.close(); }
            if (out != null) { out.close(); }
        }
    }

    public static long check_file(String filePath) throws IOException {
        File f = new File(filePath);
        if(f.exists()) {
            return f.length();
        } else {
            System.out.println("File " + filePath + " does not exist.");
            fatal("Input file path does not exist");
        }
        return -1;
    }

    public static int getEncryptKeyVal() {
        int sum = 0;
        char[] chars = encrypt_key.toCharArray();
        for(char ch: chars) {
            sum += ch;
        }
        return sum;
    }

    public static void distributor(FileInputStream in, FileOutputStream out, int coeff) throws IOException {
        //encryption
        //String encryptMap = genLogBaseStr(Math.exp(1));
        //permutEncrypt(Character.getNumericValue(encryptMap.charAt(0)));
        while(bytes_remaining >= 1024) {
            permutCipher(coeff*1024, in, out);
        }
        permutCipher(coeff*(int) bytes_remaining, in, out);

    }

    public static void permutCipher(int dimension, FileInputStream en_in, FileOutputStream en_out) throws IOException {
        DMatrixSparseTriplet encryptMat;
        if(permut_map.containsKey(dimension)) {
            encryptMat = permut_map.get(dimension);
        } else {
            boolean inverse = (dimension < 0);
            encryptMat = gen_permut_mat(Math.abs(dimension), inverse);
            permut_map.put(dimension, encryptMat);
        }
        dimension = Math.abs(dimension);
        byte[] unencryptedBytes = new byte[dimension];
        en_in.read(unencryptedBytes, 0, dimension);
        //System.out.println(Arrays.toString(unencryptedBytes));
        DMatrixSparseCSC encryptedVec = encryptVec(dimension, unencryptedBytes, encryptMat);
        for(int i = 0; i < dimension; i++) {
            double write_dbl = encryptedVec.get(i, 0);
            byte write_byte = (byte) ((Math.round(write_dbl)) & 0xff);
            //System.out.println(write_byte);
            en_out.write(write_byte);
        }
        bytes_remaining -= dimension;

    }

    public static DMatrixSparseCSC encryptVec(int dimension, byte[] unencryptedBytes, DMatrixSparseTriplet encryptMat) {
        DMatrixSparseCSC vec = new DMatrixSparseCSC(dimension, 1, dimension);
        for(int i = 0; i < dimension; i++) {
            vec.set(i, 0, unencryptedBytes[i]);
        }
        DMatrixSparseCSC encryptMat_CSC = ConvertDMatrixStruct.convert(encryptMat, (DMatrixSparseCSC)null);
        IGrowArray workA = new IGrowArray(encryptMat_CSC.numRows);
        DGrowArray workB = new DGrowArray(encryptMat_CSC.numRows);
        DMatrixSparseCSC encryptedVec = new DMatrixSparseCSC(dimension, 1, dimension);
        CommonOps_DSCC.mult(encryptMat_CSC, vec, encryptedVec, workA, workB);
        //encryptedVec.print();
        return encryptedVec;
    }

    public static DMatrixSparseTriplet gen_permut_mat(int dimension, boolean inverse) {
        System.out.println(encrypt_key_val);
        int num_matrices = 1;
        if(2*dimension > 16) {
            num_matrices = (((2*dimension) - ((2*dimension)%16))/16) + 1;
        }
        Queue<String> nums = new LinkedList<String>();
        for(int i = 0; i < num_matrices; i++) {
            String logBaseStr = "" + i + dimension;
            double logBase = Double.parseDouble(logBaseStr);
            String logStr = genLogBaseStr(logBase);
            nums.add(logStr);
        }
        StringBuilder strTotal = new StringBuilder();
        for(String str: nums) {
            strTotal.append(str);
        }
        //System.out.println(strTotal);

        //build permutation matrix
        ArrayList<Integer> rows = new ArrayList<Integer>();
        ArrayList<Integer> cols = new ArrayList<Integer>();
        DMatrixSparseTriplet permut_matrix = new DMatrixSparseTriplet(dimension, dimension, dimension);
        for(int i = 0; i < dimension; rows.add(i), cols.add(i), i++);

        for(int i = 0; i < 2*dimension; i++) {
            int row = Character.getNumericValue(strTotal.charAt(i));
            row = row % rows.size();
            int rowIndex = rows.remove(row);
            i++;
            int column = Character.getNumericValue(strTotal.charAt(i));
            column = column % cols.size();
            int columnIndex = cols.remove(column);
            permut_matrix.addItem(rowIndex, columnIndex, 1);
            //System.out.println(rows);
            //System.out.println(cols);
        }
        //SimpleMatrix permut_mat = new SimpleMatrix(permut_array);
        //permut_mat.print();
        if(inverse) {
            //convert to csc matrix
            DMatrixSparseCSC mat_CSC = ConvertDMatrixStruct.convert(permut_matrix, (DMatrixSparseCSC)null);
            //memory for the matrix operation
            IGrowArray workA = new IGrowArray(mat_CSC.numRows);
            DMatrixSparseCSC mat_transpose_CSC = new DMatrixSparseCSC(dimension, dimension, dimension);
            CommonOps_DSCC.transpose(mat_CSC, mat_transpose_CSC, workA);
            return ConvertDMatrixStruct.convert(mat_transpose_CSC, (DMatrixSparseTriplet)null);
        } else {
            return permut_matrix;
        }
    }

    public static String genLogBaseStr(double logBase) {
        double sum_log = Math.log(encrypt_key_val)/Math.log(logBase);
        String sum_str = sum_log + "";
        sum_str = sum_str.replaceAll("[.]", "");
        sum_str = extend(sum_str);
        //System.out.println(sum_str);
        return sum_str;
    }

    /*
	extends numbers with zeros in case they are too short to pair all values evenly
	for creation of change of basis matrix
	*/
    public static String extend(String numStr) {
        int length = numStr.length();
        if (length < 16) {
            for (int i = 0; i < (16 - length); i++) {
                numStr = numStr.concat("0");
            }
        }
        //System.out.println(numStr);
        return numStr;
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
