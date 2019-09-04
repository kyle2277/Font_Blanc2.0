package main.encryption_engine;

import java.io.*;
import java.util.*;

import org.ejml.data.*;
import org.ejml.sparse.csc.CommonOps_DSCC;

/*
The Font_Blanc2.0 core encryption/decryption device
*/
public class Cipher {

    private String justPath;
    private String fileName;
    private String fileOutPath;
    private char[] encrypt_key;
    private byte[] fileBytes;
    private int encrypt_key_val;
    private long fileLength;
    private long bytes_processed;
    private long bytes_remaining;
    private boolean encrypt;
    private Globals g;
    private  Deque<Instruction> instructions;
    private HashMap<Integer, Mat> permut_map;
    public static final int MAX_DIMENSION = 1024;

    public Cipher(Globals g, String fileName, String fileInPath, String fileOutPath,
                  boolean encrypt, Deque<Instruction> instructions) {
        this.g = g;
        this.encrypt = encrypt;
        justPath = fileInPath;
        this.fileName = fileName;
        if(fileOutPath != null) {
            this.fileOutPath = fileOutPath;
        } else {
            this.fileOutPath = fileInPath;
        }
        bytes_processed = 0;
        fileLength = fileLength(g);
        System.out.println("File length: " + fileLength);
        bytes_remaining = fileLength;
        this.instructions = instructions;
    }

    /*
    Takes the path of the input file
    Checks if the input file exists and returns the length of the file in bytes
     */
    private long fileLength(Globals g) {
        File f;
        String fullPath;
        fullPath = getJustPath() + getFileName();
        f = new File(fullPath);
        if(f.exists()) {
            return f.length();
        } else {
            g.fatal("File " + getFileName() + " does not exist at given path.");
            return 0;
        }
    }

    private int charSum(char[] s) {
        int sum = 0;
        for(char ch: s) {
            sum += ch;
        }
        return sum;
    }

    protected int execute() throws IOException{
        byte[] fileBytes = readInput();
        setFileBytes(fileBytes);
        readInstructions();
        writeOutput();
        return 1;
    }

    /*
    Reads and executes instructions in the correct order
    Instruction list length must be at least one
     */
    private void readInstructions() {
        int coeff = isEncrypt() ? 1 : -1;
        Deque<Instruction> instructions = getInstructions();
        Instruction current = isEncrypt() ? instructions.pollFirst() : instructions.pollLast();
        if(current != null) {
            while(current != null) {
                setBytesRemaining(getFileLength());
                setBytesProcessed(0);
                //todo zero out old map matrices
                setPermutMap(new HashMap<>());
                int dimension = current.getDimension();
                char[] encryptKey = current.getEncryptKey();
                setEncryptKey(encryptKey);
                System.out.println("Encrypt key: " + Arrays.toString(getEncryptKey()));
                setEncryptKeyVal(charSum(encryptKey));
                if(dimension > 0) { //fixed dimension
                    fixedDistributor(dimension, coeff);
                } else { //flexible dimension
                    randDistributor(coeff);
                }
                System.out.println("Done.");
                current = isEncrypt() ? instructions.pollFirst() : instructions.pollLast();
                purgeKey();
            }
        } else {
            g.fatal("Process instructions empty.");
        }
    }

    private void purgeKey() {
        Arrays.fill(getEncryptKey(), '0');
        setEncryptKeyVal(0);
    }

    public void close() {
        Arrays.fill(getFileBytes(), (byte)0);
    }

    private byte[] readInput() throws IOException {
        //todo file size limited to ~2GB by cast to int
        byte[] fileBytes = new byte[(int)getFileLength()];
        String inPath = getJustPath() + getFileName();
        FileInputStream in = new FileInputStream(inPath);
        in.read(fileBytes, 0, (int)getFileLength());
        in.close();
        return fileBytes;
    }

    private void writeOutput() throws IOException {
        FileOutputStream out = null;
        try {
            String outName = getFileName();
            if(isEncrypt()) {
                outName = g.encryptTag + outName;
                if(!outName.contains(g.encryptExt)) {
                    outName += g.encryptExt;
                }
            } else if(!isEncrypt()) {
                outName = g.decryptTag + outName;
                if(outName.contains(g.encryptExt)) {
                    outName = outName.substring(0, outName.length() - g.encryptExt.length());
                }
            }
            out = new FileOutputStream(fileOutPath + outName);
            out.write(getFileBytes(), 0, (int)getFileLength());
        } catch (IOException e) {
            g.fatal("Output path not found.");
        } finally {
            if(out != null) { out.close(); }
        }
    }

    /*
    Takes objects for reading and writing to file and the coefficient which tells whether to fetch the normal or
    inverted permutation matrix
    Breaks the file into chunks to be separately encrypted/decrypted
    */
    public void randDistributor(int coeff) {
//            String encryptMap = genLogBaseStr(Math.exp(1));
        String encryptMap = genStringVals((int)bytes_remaining/MAX_DIMENSION);
        int encryptMapLen = encryptMap.length();
        for(int mapItr = 0; bytes_remaining >= MAX_DIMENSION; mapItr++) {
            //generate permutation dimension
            int permutDimension = Character.getNumericValue(encryptMap.charAt(mapItr % encryptMapLen));
            int dimension = permutDimension > 1 ? (MAX_DIMENSION - (MAX_DIMENSION / permutDimension)) : MAX_DIMENSION;
            permutCipher(coeff*dimension);
        }
        int b = (int) getBytesRemaining();
        if(b > 0) {
            permutCipher(coeff*b);
        }
    }

    /*
    Run process with a fixed matrix dimension size
    Takes dimension to use and whether to encrypt or decrypt
     */
    public void fixedDistributor(int dimension, int coeff) {
        while(getBytesRemaining() >= dimension) {
            permutCipher(coeff*dimension);
        }
        int b = (int) getBytesRemaining();
        if(b > 0) {
            permutCipher(coeff*b);
        }
    }

    /*
    Takes the matrix dimension and objects for reading and writing to file
    Facilitates the matrix transformation using n-dimensional permutation matrix
    Writes resulting vector of bytes to file
    */
    protected void permutCipher(int dimension) {
        int ref = (int)getBytesProcessed();
        Mat m;
        if(permut_map.containsKey(dimension)) {
            m = permut_map.get(dimension);
        } else {
            m = gen_permut_mat(Math.abs(dimension), (dimension < 0));
            permut_map.put(dimension, m);
        }
        //dimension no longer needs to be negative to signify inverse operation
        dimension = Math.abs(dimension);
        byte[] fileBytes;
        fileBytes = Arrays.copyOfRange(getFileBytes(), ref, ref+dimension);
        //System.out.println(Arrays.toString(unencryptedBytes));
        DMatrixSparseCSC vec = bArrToCSCMat(fileBytes, dimension);
        DMatrixSparseCSC resultantVec = transformVec(dimension, vec, m.getMat());
        //dot check
        int dotBef = dotProduct(vec, m.getCheckBef(), dimension);
        int dotAft = dotProduct(resultantVec, m.getCheckAft(), dimension);
        //System.out.println(dotBef + "\n" + dotAft);
        if(dotBef != dotAft) {
            String message = "Data corruption detected.\nUnencrypted bytes remaining: " + getBytesRemaining();
            g.fatal(message);
            System.exit(1);
        }
        for(int i = ref; i < ref+dimension; i++) {
            byte write_byte = (byte) ((Math.round(resultantVec.get(i-ref, 0))) & 0xff);
            setFileByte(write_byte, i);
            //System.out.println(write_byte);
        }
        bytes_processed += dimension;
        bytes_remaining -= dimension;
    }

    /*
    Takes an array of bytes and the size of the array. Converts the array into a matrix object and returns it
     */
    private DMatrixSparseCSC bArrToCSCMat(byte[] bytes, int dimension) {
        DMatrixSparseCSC vec = new DMatrixSparseCSC(dimension, 1, dimension);
        //convert to CSC matrix
        for(int i = 0; i < dimension; vec.set(i, 0, bytes[i]), i++);
        return vec;
    }

    /*
    Takes the matrix dimension, a list of bytes from the file and relevant permutation matrix
    Performs the linear transformation operation on the byte vector and returns the resulting vector
    */
    private DMatrixSparseCSC transformVec(int dimension, DMatrixSparseCSC fileBytes, DMatrixSparseCSC permutMat) {
        //allocate memory for the matrix operation
        IGrowArray workA = new IGrowArray(permutMat.numRows);
        DGrowArray workB = new DGrowArray(permutMat.numRows);
        DMatrixSparseCSC resultantVec = new DMatrixSparseCSC(dimension, 1, dimension);
        CommonOps_DSCC.mult(permutMat, fileBytes, resultantVec, workA, workB);
        //encryptedVec.print();
        return resultantVec;
    }

    private int dotProduct(DMatrixSparseCSC a, DMatrixSparseCSC b, int dimension) {
        int result = 0;
        for(int i = 0; i < dimension; i++) {
            result += (int)a.get(i, 0) * (int)b.get(i, 0);
        }
        return result;
    }

    /*
    Generates unique, pseudo-random string of numbers using the encryption key
    */
    private String genLogBaseStr(double logBase) {
        double sum_log = Math.log(getEncryptKeyVal())/Math.log(logBase);
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
    private Mat gen_permut_mat(int dimension, boolean inverse) {
        //System.out.println(encrypt_key_val);
        String nums = genStringVals(2*dimension);
        //System.out.println(nums);
        //build permutation matrix
        ArrayList<Integer> rows = new ArrayList<Integer>();
        ArrayList<Integer> cols = new ArrayList<Integer>();
        DMatrixSparseCSC permut_matrix = new DMatrixSparseCSC(dimension, dimension, dimension);
        Mat m = new Mat();
        //expensive computation
        //creating array to build permutation matrix
        DMatrixSparseCSC checkBef = new DMatrixSparseCSC(dimension, 1, dimension);
        for(int i = 0; i < dimension; rows.add(i), cols.add(i), checkBef.set(i, 0, i), i++);
        m.setCheckBef(checkBef);
        for(int i = 0; i < 2*dimension; i++) {
            int row = Character.getNumericValue(nums.charAt(i));
            row = row % rows.size();
            int rowIndex = rows.remove(row);
            i++;
            int column = Character.getNumericValue(nums.charAt(i));
            column = column % cols.size();
            int columnIndex = cols.remove(column);
            permut_matrix.set(rowIndex, columnIndex, 1);
            //System.out.println(rows);
            //System.out.println(cols);
        }
        DMatrixSparseCSC pm;
        if(inverse) {
            //convert to CSC matrix
            //allocate memory for the matrix operation
            IGrowArray workA = new IGrowArray(permut_matrix.numRows);
            DMatrixSparseCSC mat_transpose_CSC = new DMatrixSparseCSC(dimension, dimension, dimension);
            pm = CommonOps_DSCC.transpose(permut_matrix, mat_transpose_CSC, workA);
        } else {
            pm = permut_matrix;
        }
        m.setMat(pm);
        DMatrixSparseCSC checkAft = transformVec(dimension, m.getCheckBef(), pm);
        m.setCheckAft(checkAft);
        return m;
    }

    private String genStringVals(int approx) {
        int num_matrices = 1;
        if(approx > 16) {
            num_matrices = ((approx - (approx%16))/16) + 1;
        }
        StringBuilder strTotal = new StringBuilder();
        for(int i = 0; i < num_matrices; i++) {
            int logBaseStr = i + approx;
            String logStr = genLogBaseStr((double) logBaseStr);
            strTotal.append(logStr);
        }
        return strTotal.toString();
    }

    public long getFileLength() {
        return fileLength;
    }

    public long getBytesProcessed() {
        return bytes_processed;
    }

    public long getBytesRemaining() {
        return bytes_remaining;
    }

    public void setBytesProcessed(long bytes) {
        bytes_processed = bytes;
    }

    public void setBytesRemaining(long bytes) {
        bytes_remaining = bytes;
    }

    public void putPermutMap(int k, Mat v) {
        permut_map.put(k, v);
    }

    public Mat getPermutMap(int k) {
        return permut_map.get(k);
    }

    public void setPermutMap(HashMap<Integer, Mat> permut_map) {
        this.permut_map = permut_map;
    }

    public boolean permutMapContainsKey(int k) {
        return permut_map.containsKey(k);
    }

    public boolean isEncrypt() {
        return encrypt;
    }

    public String getJustPath() {
        return justPath;
    }

    public String getFileName() {
        return fileName;
    }

    public Globals getGlobals() {
        return g;
    }

    public void setFileBytes(byte[] fileBytes) {
        this.fileBytes = fileBytes;
    }

    public byte[] getFileBytes() {
        return fileBytes;
    }

    public void setFileByte(byte b, int index) {
        getFileBytes()[index] = b;
    }

    public Deque<Instruction> getInstructions() {
        return instructions;
    }

    private void setEncryptKey(char[] encrypt_key) {
        this.encrypt_key = encrypt_key;
    }

    private char[] getEncryptKey() {
        return encrypt_key;
    }

    private void setEncryptKeyVal(int encrypt_key_val) {
        this.encrypt_key_val = encrypt_key_val;
    }

    private int getEncryptKeyVal() {
        return encrypt_key_val;
    }
}
