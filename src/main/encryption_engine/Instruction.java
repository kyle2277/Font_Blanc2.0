package main.encryption_engine;

import java.util.Arrays;

public class Instruction {

    private int dimension;
    private char[] encryptKey;

    public Instruction(int dimension, char[] encryptKey) {
        this.dimension = dimension;
        this.encryptKey = encryptKey;
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
        readout += getDimension() > 0 ? "Yes, Dimension: " + getDimension() + ", " : "No, ";
        readout += "Encrypt Key: " + Arrays.toString(getEncryptKey());
        return readout;
    }

}
