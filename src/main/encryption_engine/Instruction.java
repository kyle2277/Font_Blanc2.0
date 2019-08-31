package main.encryption_engine;

import java.util.Arrays;

public class Instruction {

    private int fixed;
    private int dimension;
    private char[] encryptKey;

    public Instruction(int fixed, int dimension, char[] encryptKey) {
        this.fixed = fixed;
        this.dimension = dimension;
        this.encryptKey = encryptKey;
    }

    public int getFixed() {
        return fixed;
    }

    public int getDimension() {
        return dimension;
    }

    public char[] getEncryptKey() {
        return encryptKey;
    }

    public void setDimension(int dimension) {
        this.dimension = dimension;
    }

    public String toString() {
        String readout = "Fixed: ";
        readout += getFixed() == 1 ? "Yes, Dimension: " + getDimension() + ", " : "No, ";
        readout += "Encrypt Key: " + Arrays.toString(getEncryptKey());
        return readout;
    }

}
