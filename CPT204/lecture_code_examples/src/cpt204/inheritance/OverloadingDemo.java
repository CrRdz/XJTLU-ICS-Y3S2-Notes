package cpt204.inheritance;

public class OverloadingDemo {
    public static int max(int a, int b) {
        return Math.max(a, b);
    }

    public static double max(double a, double b) {
        return Math.max(a, b);
    }

    public static void main(String[] args) {
        System.out.println(max(1, 2));
        System.out.println(max(1.5, 2.5));
    }
}
