import java.util.*;
import org.ejml.simple.*;

import java.lang.Math;

public class LossyRoundingTest {

    public static SimpleMatrix rot_mat;
    public static SimpleMatrix rot_inv;
    public static int size;
    public static int rot_angle;
    public static int num_errs;
    public static int test_num;

    public static void main(String[] args) {
        rot_angle = Integer.parseInt(args[0]);
        size = Integer.parseInt(args[1]);
        System.out.println("Angle of rotation: " + rot_angle);
        create_rot_mat();
        Random rand = new Random();
        num_errs = 0;
        test_num = 0;
        for(int i = 0; i < 100; i++) {
            test_num++;
            SimpleMatrix test_vec = create_test_vec(rand);
            SimpleMatrix encrypted = transform_vec(test_vec, false);
            SimpleMatrix decrypted = transform_vec(encrypted, true);
            compare(test_vec, decrypted);
        }
        System.out.println("Number of rounding errors: " + num_errs);

    }

    public static void create_rot_mat() {
        double[][] transformation = new double[size][size];
        transformation[0][0] = Math.cos(Math.toRadians(rot_angle));
        transformation[1][0] = Math.sin(Math.toRadians(rot_angle));
        transformation[0][1] = -Math.sin(Math.toRadians(rot_angle));
        transformation[1][1] = Math.cos(Math.toRadians(rot_angle));
        rot_mat = new SimpleMatrix(transformation);
        rot_mat.print();
        System.out.println();
        rot_inv = rot_mat.transpose();
    }

    public static SimpleMatrix create_test_vec(Random rand) {
        double[][] test_vec = new double[size][1];
        for(int i = 0; i < size; i++) {
            test_vec[i][0] = rand.nextInt(127);
        }
        SimpleMatrix vec = new SimpleMatrix(test_vec);
        return vec;
    }

    public static SimpleMatrix transform_vec(SimpleMatrix input_vec, boolean inv) {
        SimpleMatrix output_vec;
        if(inv) {
            output_vec = rot_inv.mult(input_vec);
        } else {
            output_vec = rot_mat.mult(input_vec);
        }
        //output_vec.print();
        for(int i = 0; i < size; i++) {
            output_vec.set(i,0, Math.round(output_vec.get(i,0)));
        }
        return output_vec;
    }

    public static void compare(SimpleMatrix test_vec, SimpleMatrix decrypted_vec) {
        for(int i = 0; i < size; i++) {
            if(!(test_vec.get(i, 0) == decrypted_vec.get(i, 0))) {
                num_errs++;
                System.out.println("Rounding Error on test number " + test_num + ".");
                test_vec.print();
                decrypted_vec.print();

            }
        }
    }
}
