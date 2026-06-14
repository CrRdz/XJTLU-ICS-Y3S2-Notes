package cpt204.arrays;

import java.util.Arrays;

public class ArrayBasics {
    public static void main(String[] args) {
        int[] numbers = new int[5];
        boolean[] flags = new boolean[3];
        double[] values = {1.9, 2.9, 3.4, 3.5};

        numbers[0] = 10;
        numbers[1] = 20;

        System.out.println("numbers length = " + numbers.length);
        System.out.println("default int = " + numbers[2]);
        System.out.println("default boolean = " + flags[0]);
        System.out.println("values = " + Arrays.toString(values));

        int sum = 0;
        for (int value : numbers) {
            sum += value;
        }
        System.out.println("sum = " + sum);
    }
}
