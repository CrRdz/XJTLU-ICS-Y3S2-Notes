package cpt204.trees;

import java.util.Arrays;

public class HashingDemo {
    public static void insertLinearProbing(Integer[] table, int key) {
        int index = key % table.length;
        while (table[index] != null) {
            index = (index + 1) % table.length;
        }
        table[index] = key;
    }

    public static void main(String[] args) {
        Integer[] table = new Integer[10];
        insertLinearProbing(table, 12);
        insertLinearProbing(table, 22);
        insertLinearProbing(table, 32);
        System.out.println(Arrays.toString(table));
    }
}
