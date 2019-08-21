package main.encryption_engine;

import org.ejml.data.*;
import org.ejml.sparse.csc.CommonOps_DSCC;


public class SinglePermutation extends Thread {

    private long id;
    private byte[] fileBytes;
    private int dimension;
    private DMatrixSparseCSC permutMat;
    private DMatrixSparseCSC resultantMat;

    public SinglePermutation(int id, int dimension, byte[] fileBytes, DMatrixSparseCSC permutMat) {
        this.id = id;
        this.dimension = dimension;
        this.fileBytes = fileBytes;
        this.permutMat = permutMat;
        resultantMat = null;
    }

    @Override
    public void run() {
        //System.out.println("Started thead " + id + ".");
        resultantMat = transformVec();
    }

    /*
    Takes the matrix dimension, a list of bytes from the file and relevant permutation matrix
    Performs the linear transformation operation on the byte vector and returns the resulting vector
    */
    private DMatrixSparseCSC transformVec() {
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

    public long getId() {
        return id;
    }

    public DMatrixSparseCSC getResMat() {
        return resultantMat;
    }

    public int getDimension() {
        return dimension;
    }

}
