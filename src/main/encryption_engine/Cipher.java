package main.encryption_engine;

import java.io.*;
import java.util.*;

import org.ejml.data.*;
import org.ejml.sparse.csc.CommonOps_DSCC;

/*
The Font_Blanc2.0 core encryption/decryption device
*/
public class Cipher extends Thread{

    private String justPath;
    private String fileName;
    private String fileOutPath;
    private char[] encrypt_key;
    private int encrypt_key_val;
    public long fileLength;
    public long bytes_processed;
    public long bytes_remaining;
    private boolean encrypt;
    private Globals g;
    private HashMap<Integer, DMatrixSparseCSC> permut_map;

    public Cipher(Globals g, String fileName, String fileInPath, String fileOutPath, char[] encryptKey, boolean encrypt) {
        this.g = g;
        this.encrypt = encrypt;
        encrypt_key = encryptKey;
        justPath = fileInPath;
        this.fileName = fileName;
        if(fileOutPath != null) {
            this.fileOutPath = fileOutPath;
        } else {
            this.fileOutPath = fileInPath;
        }
        System.out.println("Encrypt key: " + Arrays.toString(encrypt_key));
        encrypt_key_val = getEncryptKeyVal();
        bytes_processed = 0;
        fileLength = fileLength(g, encrypt);
        bytes_remaining = fileLength;
        permut_map = new HashMap<>();
    }

    @Override
    public void run() {
        try {
            distributor();
        } catch (Exception e) {
            System.out.println("Exception.");
            System.exit(1);
        }
    }

    /*
    Takes the path of the input file
    Checks if the input file exists and returns the length of the file in bytes
     */
    private long fileLength(Globals g, boolean encrypt) {
        File f;
        String fullPath;
        if(encrypt) {
            fullPath = justPath + fileName;
            f = new File(fullPath);
        } else { //EorD = "decrypt"
            fullPath = justPath + g.encryptTag + fileName + g.encryptExt;
            f = new File(fullPath);
        }
        if(f.exists()) {
            return f.length();
        } else {
            g.fatal("File " + fileName + " does not exist");
            return 0;
        }
    }

    private int getEncryptKeyVal() {
        int sum = 0;
        for(char ch: encrypt_key) {
            sum += ch;
        }
        return sum;
    }

    /*
    Takes objects for reading and writing to file and the coefficient which tells whether to fetch the normal or
    inverted permutation matrix
    Breaks the file into chunks to be separately encrypted/decrypted
    */
    public void distributor() throws IOException {
        FileInputStream in = null;
        FileOutputStream out = null;
        int coeff = 0;
        try {
            if(encrypt) {
                in = new FileInputStream(justPath + fileName);
                out = new FileOutputStream(justPath + g.encryptTag + fileName + g.encryptExt);
                coeff = 1;
                System.out.println("Encrypting...");
            } else { //decrypt
                in = new FileInputStream(justPath + g.encryptTag + fileName + g.encryptExt);
                out = new FileOutputStream(justPath + g.decryptTag + fileName);
                coeff = -1;
                System.out.println("Decrypting...");
            }
            String encryptMap = genLogBaseStr(Math.exp(1));
            int encryptMapLen = encryptMap.length();
            int mapItr = 0;
            while(bytes_remaining >= 1024) {
                if(mapItr == encryptMapLen) {
                    mapItr = 0;
                }
                int permutDimension = Character.getNumericValue(encryptMap.charAt(mapItr)) + 1;
                permutCipher(coeff*(1024/permutDimension), in, out);
                mapItr++;
            }
            permutCipher(coeff*(int) bytes_remaining, in, out);
        } catch(IOException e) {
            g.fatal("Output path not found.");
        } finally {
            if (in != null) { in.close(); }
            if (out != null) { out.close(); }
        }
    }

    /*
    Takes the matrix dimension and objects for reading and writing to file
    Facilitates the matrix transformation using n-dimensional permutation matrix
    Writes resulting vector of bytes to file
    */
    private void permutCipher(int dimension, FileInputStream in, FileOutputStream out) throws IOException {
        DMatrixSparseCSC permutMat;
        if(permut_map.containsKey(dimension)) {
            permutMat = permut_map.get(dimension);
        } else {
            permutMat = gen_permut_mat(Math.abs(dimension), (dimension < 0));
            permut_map.put(dimension, permutMat);
        }
        //dimension no longer needs to be negative to signify inverse operation
        dimension = Math.abs(dimension);
        byte[] fileBytes = new byte[dimension];
        in.read(fileBytes, 0, dimension);
        //System.out.println(Arrays.toString(unencryptedBytes));
        DMatrixSparseCSC resultantVec = transformVec(dimension, fileBytes, permutMat);
        for(int i = 0; i < dimension; i++) {
            byte write_byte = (byte) ((Math.round(resultantVec.get(i, 0))) & 0xff);
            //System.out.println(write_byte);
            out.write(write_byte);
        }
        bytes_processed += dimension;
        bytes_remaining -= dimension;
    }

    /*
    Takes the matrix dimension, a list of bytes from the file and relevant permutation matrix
    Performs the linear transformation operation on the byte vector and returns the resulting vector
    */
    private DMatrixSparseCSC transformVec(int dimension, byte[] fileBytes, DMatrixSparseCSC permutMat) {
        DMatrixSparseCSC vec = new DMatrixSparseCSC(dimension, 1, dimension);
        for(int i = 0; i < dimension; i++) {
            vec.set(i, 0, fileBytes[i]);
        }
        //convert to CSC matrix
        //allocate memory for the matrix operation
        IGrowArray workA = new IGrowArray(permutMat.numRows);
        DGrowArray workB = new DGrowArray(permutMat.numRows);
        DMatrixSparseCSC resultantVec = new DMatrixSparseCSC(dimension, 1, dimension);
        CommonOps_DSCC.mult(permutMat, vec, resultantVec, workA, workB);
        //encryptedVec.print();
        return resultantVec;
    }

    /*
    Generates unique, pseudo-random string of numbers using the encryption key
    */
    private String genLogBaseStr(double logBase) {
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
    private String extend(String numStr) {
        int length = numStr.length();
        if (length < 16) {
            StringBuilder str = new StringBuilder(numStr);
            for (int i = 0; i < (16 - length); i++) {
                str.append("0");
            }
            numStr = str.toString();
        }
        return numStr;
    }

    /*
    Takes the dimension of the matrix to create and whether to invert it
    Generates unique n-dimensional permutation matrices from the encryption key
    */
    private DMatrixSparseCSC gen_permut_mat(int dimension, boolean inverse) {
        //System.out.println(encrypt_key_val);
        int num_matrices = 1;
        if(2*dimension > 16) {
            num_matrices = (((2*dimension) - ((2*dimension)%16))/16) + 1;
        }
        StringBuilder strTotal = new StringBuilder();
        for(int i = 0; i < num_matrices; i++) {
            int logBaseStr = i + dimension;
            String logStr = genLogBaseStr((double) logBaseStr);
            strTotal.append(logStr);
        }
        //System.out.println(strTotal);
        //build permutation matrix
        ArrayList<Integer> rows = new ArrayList<Integer>();
        ArrayList<Integer> cols = new ArrayList<Integer>();
        DMatrixSparseCSC permut_matrix = new DMatrixSparseCSC(dimension, dimension, dimension);
        //expensive computation
        //creating array to build permutation matrix
        for(int i = 0; i < dimension; rows.add(i), cols.add(i), i++);
        for(int i = 0; i < 2*dimension; i++) {
            int row = Character.getNumericValue(strTotal.charAt(i));
            row = row % rows.size();
            int rowIndex = rows.remove(row);
            i++;
            int column = Character.getNumericValue(strTotal.charAt(i));
            column = column % cols.size();
            int columnIndex = cols.remove(column);
            permut_matrix.set(rowIndex, columnIndex, 1);
            //System.out.println(rows);
            //System.out.println(cols);
        }
        if(inverse) {
            //convert to CSC matrix
            //allocate memory for the matrix operation
            IGrowArray workA = new IGrowArray(permut_matrix.numRows);
            DMatrixSparseCSC mat_transpose_CSC = new DMatrixSparseCSC(dimension, dimension, dimension);
            return CommonOps_DSCC.transpose(permut_matrix, mat_transpose_CSC, workA);
        } else {
            return permut_matrix;
        }
    }
}
