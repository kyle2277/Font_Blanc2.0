import java.util.*;
import org.ejml.simple.*;
import java.lang.Math;

public class PermutMatrixTest {

    public static String encrypt_key;
    public static int encrypt_key_val;
    public static SimpleMatrix encryption_mat;
    public static HashMap<Integer, SimpleMatrix> permut_map;

    public static void main(String[] args) {
        encrypt_key = args[0];
        encrypt_key_val = getEncryptKeyVal();
        System.out.println("Encrypt key: " + encrypt_key);
        //generate permutation matrices
        permut_map = new HashMap<Integer, SimpleMatrix>();
        SimpleMatrix permut_4 = gen_permut_mat(9);

    }

    public static SimpleMatrix gen_permut_mat(int dimension) {
        System.out.println(encrypt_key_val);
        int num_matrices = 1;
        if(2*dimension > 16) {
            num_matrices = (((2*dimension) - ((2*dimension)%16))/16) + 1;
        }
        Queue<String> nums = new LinkedList<String>();
        for(int i = 0; i < num_matrices; i++) {
            String logBaseStr = "" + i + dimension;
            int logBase = Integer.parseInt(logBaseStr);
            double sum_log = Math.log(encrypt_key_val)/Math.log(logBase);
            String sum_str = sum_log + "";
            sum_str = sum_str.replaceAll("[.]", "");
            sum_str = extend(sum_str);
            System.out.println(sum_str);
            nums.add(sum_str);
        }
        StringBuilder strTotal = new StringBuilder();
        for(String str: nums) {
            strTotal.append(str);
        }
        System.out.println(strTotal);

        //build permutation matrix
        ArrayList<Integer> rows = new ArrayList<Integer>();
        ArrayList<Integer> cols = new ArrayList<Integer>();
        double[][] permut_array = new double[dimension][dimension];
        for(int i = 0; i < dimension; rows.add(i), cols.add(i), i++);

        for(int i = 0; i < 2*dimension; i++) {
            int row = Character.getNumericValue(strTotal.charAt(i));
            row = row % rows.size();
            int rowIndex = rows.remove(row);
            i++;
            int column = Character.getNumericValue(strTotal.charAt(i));
            column = column % cols.size();
            int columnIndex = cols.remove(column);
            permut_array[rowIndex][columnIndex] = 1;
            System.out.println(rows);
            System.out.println(cols);
        }
        SimpleMatrix permut_mat = new SimpleMatrix(permut_array);
        permut_mat.print();
        return permut_mat;
    }

    public static int getEncryptKeyVal() {
        int sum = 0;
        char[] chars = encrypt_key.toCharArray();
        for(char ch: chars) {
            sum += ch;
        }
        return sum;
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
}
