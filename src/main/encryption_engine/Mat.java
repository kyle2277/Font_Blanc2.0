package main.encryption_engine;

import org.ejml.data.DMatrixSparseCSC;

public class Mat {
    private DMatrixSparseCSC m;
    private DMatrixSparseCSC checkBef;
    private DMatrixSparseCSC checkAft;

    public Mat() {
    }

    public void setMat(DMatrixSparseCSC m) {
        this.m = m;
    }

    public void setCheckBef(DMatrixSparseCSC check_bef) {
        this.checkBef = check_bef;
    }

    public void setCheckAft(DMatrixSparseCSC check_aft) {
        this.checkAft = check_aft;
    }

    public DMatrixSparseCSC getCheckBef() {
        return checkBef;
    }

    public DMatrixSparseCSC getCheckAft() {
        return checkAft;
    }

    public DMatrixSparseCSC getMat() {
        return m;
    }
}
