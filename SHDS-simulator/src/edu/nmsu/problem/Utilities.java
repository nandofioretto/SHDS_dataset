package edu.nmsu.problem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by nandofioretto on 11/4/16.
 */
public class Utilities {

    private static Random rand = new Random();

    public static String readFile(String path)
            throws IOException
    {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded);
    }

    public static double getMax (double[] array) {
        assert (array.length > 0);

        double max = array[0];
        for (int i=1; i<array.length; i++)
            if (max < array[i])
                max = array[i];
        return max;
    }

    public static int getMax (int[] array) {
        assert (array.length > 0);

        int max = array[0];
        for (int i=1; i<array.length; i++)
            if (max < array[i])
                max = array[i];
        return max;
    }

    public static Integer getMax (List<Integer> array) {
        assert (array.size() > 0);

        int max = array.get(0);
        for (int i=1; i<array.size(); i++)
            if (max < array.get(i))
                max = array.get(i);
        return max;
    }


    public static double getMin (double[] array) {
        assert (array.length > 0);

        double min = array[0];
        for (int i=1; i<array.length; i++)
            if (array[i] < min)
                min = array[i];
        return min;
    }

    public static int getMin (int[] array) {
        assert (array.length > 0);

        int min = array[0];
        for (int i=1; i<array.length; i++)
            if (array[i] < min)
                min = array[i];
        return min;
    }

    public static Integer getMin (List<Integer> array) {
        assert (array.size() > 0);

        int min = array.get(0);
        for (int i=1; i<array.size(); i++)
            if (min< array.get(i))
                min = array.get(i);
        return min;
    }


    public static double[] sum(double[] a, double[] b) {
        assert (a.length == b.length);
        for (int i=0; i<a.length; i++) {
            a[i] += b[i];
        }
        return a;
    }

    public static double[] sum(double[] a, Double[] b) {
        assert (a.length == b.length);
        for (int i=0; i<a.length; i++) {
            a[i] += b[i];
        }
        return a;
    }

    public static double sum(Double[] array) {
        double sum = 0;
        for (int i = 0; i < array.length; i++)
            sum += array[i];
        return sum;
    }

    public static double sum(double[] array) {
        double sum = 0;
        for (int i = 0; i < array.length; i++)
            sum += array[i];
        return sum;
    }

    public static int genRand(int a, int b) {
        return rand.nextInt(b - a + 1) + a;
    }

    public static String genRand(String[] array) {
        int idx = rand.nextInt(array.length);
        return array[idx];
    }

    public static String genRand(ArrayList<String> array) {
        int idx = rand.nextInt(array.size());
        return array.get(idx);
    }

    public static int genRand(int[] array) {
        int idx = rand.nextInt(array.length);
        return array[idx];
    }

}
