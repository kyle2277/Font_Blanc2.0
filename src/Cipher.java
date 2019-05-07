import java.io.*;
import java.util.*;

import org.ejml.data.*;
import org.ejml.ops.ConvertDMatrixStruct;
import org.ejml.sparse.csc.CommonOps_DSCC;

/*
The Font_Blanc2.0 core encryption/decryption device
*/
public class Cipher {

    private static String encrypt_key;
    private static int encrypt_key_val;
    public static long bytes_processed;
    public static long bytes_remaining;
    private static HashMap<Integer, DMatrixSparseTriplet> permut_map;

    public Cipher(String encryptKey, long fileLength) {
        encrypt_key = encryptKey;
        System.out.println("Encrypt key: " + encrypt_key);
        encrypt_key_val = getEncryptKeyVal();
        bytes_processed = 0;
        bytes_remaining = fileLength;
        permut_map = new HashMap<>();
    }

    public int getEncryptKeyVal() {
        int sum = 0;
        char[] chars = encrypt_key.toCharArray();
        for(char ch: chars) {
            sum += ch;
        }
        return sum;
    }

    /*
    Takes objects for reading and writing to file and the coefficient which tells whether to fetch the normal or
    inverted permutation matrix
    Breaks the file into chunks to be separately encrypted/decrypted
    */
    public void distributor(FileInputStream in, FileOutputStream out, int coeff) throws IOException {
        //String encryptMap = genLogBaseStr(Math.exp(1));
        //permutEncrypt(Character.getNumericValue(encryptMap.charAt(0)));
        while(bytes_remaining >= 1024) {
            permutCipher(coeff*1024, in, out);
        }
        permutCipher(coeff*(int) bytes_remaining, in, out);
    }

    /*
    Takes the matrix dimension and objects for reading and writing to file
    Facilitates the matrix transformation using n-dimensional permutation matrix
    Writes resulting vector of bytes to file
    */
    public void permutCipher(int dimension, FileInputStream en_in, FileOutputStream en_out) throws IOException {
        DMatrixSparseTriplet permutMat;
        if(permut_map.containsKey(dimension)) {
            permutMat = permut_map.get(dimension);
        } else {
            boolean inverse = (dimension < 0);
            permutMat = gen_permut_mat(Math.abs(dimension), inverse);
            permut_map.put(dimension, permutMat);
        }
        dimension = Math.abs(dimension);
        byte[] fileBytes = new byte[dimension];
        en_in.read(fileBytes, 0, dimension);
        //System.out.println(Arrays.toString(unencryptedBytes));
        DMatrixSparseCSC resultantVec = transformVec(dimension, fileBytes, permutMat);
        for(int i = 0; i < dimension; i++) {
            double write_dbl = resultantVec.get(i, 0);
            byte write_byte = (byte) ((Math.round(write_dbl)) & 0xff);
            //System.out.println(write_byte);
            en_out.write(write_byte);
        }
        bytes_remaining -= dimension;
    }

    /*
    Takes the matrix dimension, a list of bytes from the file and relevant permutation matrix
    Performs the linear transformation operation on the byte vector and returns the resulting vector
    */
    public DMatrixSparseCSC transformVec(int dimension, byte[] fileBytes, DMatrixSparseTriplet permutMat) {
        DMatrixSparseCSC vec = new DMatrixSparseCSC(dimension, 1, dimension);
        for(int i = 0; i < dimension; i++) {
            vec.set(i, 0, fileBytes[i]);
        }
        //convert to CSC matrix
        DMatrixSparseCSC transformMat_CSC = ConvertDMatrixStruct.convert(permutMat, (DMatrixSparseCSC)null);
        //allocate memory for the matrix operation
        IGrowArray workA = new IGrowArray(transformMat_CSC.numRows);
        DGrowArray workB = new DGrowArray(transformMat_CSC.numRows);
        DMatrixSparseCSC resultantVec = new DMatrixSparseCSC(dimension, 1, dimension);
        CommonOps_DSCC.mult(transformMat_CSC, vec, resultantVec, workA, workB);
        //encryptedVec.print();
        return resultantVec;
    }

    /*
    Generates unique, pseudo-random string of numbers using the encryption key
    */
    public String genLogBaseStr(double logBase) {
        double sum_log = Math.log(encrypt_key_val)/Math.log(logBase);
        String sum_str = sum_log + "";
        sum_str = sum_str.replaceAll("[.]", "");
        sum_str = extend(sum_str);
        return sum_str;
    }

    /*
	extends numbers with zeros in case they are too short to pair all values evenly
	for creation of change of basis matrix
	*/
    public String extend(String numStr) {
        int length = numStr.length();
        if (length < 16) {
            for (int i = 0; i < (16 - length); i++) {
                numStr = numStr.concat("0");
            }
        }
        return numStr;
    }

    /*
    Takes the dimension of the matrix to create and whether to invert it
    Generates unique n-dimensional permutation matrices from the encryption key
    */
    public DMatrixSparseTriplet gen_permut_mat(int dimension, boolean inverse) {
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
        if(inverse) {
            //convert to CSC matrix
            DMatrixSparseCSC mat_CSC = ConvertDMatrixStruct.convert(permut_matrix, (DMatrixSparseCSC)null);
            //allocate memory for the matrix operation
            IGrowArray workA = new IGrowArray(mat_CSC.numRows);
            DMatrixSparseCSC mat_transpose_CSC = new DMatrixSparseCSC(dimension, dimension, dimension);
            CommonOps_DSCC.transpose(mat_CSC, mat_transpose_CSC, workA);
            return ConvertDMatrixStruct.convert(mat_transpose_CSC, (DMatrixSparseTriplet)null);
        } else {
            return permut_matrix;
        }
    }
}
