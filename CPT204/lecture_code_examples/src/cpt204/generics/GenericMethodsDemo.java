package cpt204.generics;

public class GenericMethodsDemo {
    public static <E> void print(E[] list) {
        for (E item : list) {
            System.out.print(item + " ");
        }
        System.out.println();
    }

    public static <E extends Comparable<E>> E max(E a, E b) {
        return a.compareTo(b) >= 0 ? a : b;
    }

    public static void main(String[] args) {
        print(new Integer[]{1, 2, 3});
        print(new String[]{"London", "Paris"});
        System.out.println(max("Suzhou", "London"));
    }
}
