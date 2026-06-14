package cpt204.arrays;

import java.util.Arrays;

public class PassByValueDemo {
    public static void update(int num, int[] array) {
        num = 20;
        array[0] = 50;
    }

    public static void main(String[] args) {
        int x = 10;
        int[] arr = new int[1];
        update(x, arr);
        System.out.println(x);
        System.out.println(Arrays.toString(arr));
    }
}
