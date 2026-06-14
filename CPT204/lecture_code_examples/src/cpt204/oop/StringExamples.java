package cpt204.oop;

public class StringExamples {
    public static void main(String[] args) {
        String s1 = "Hello";
        String s2 = new String("Hello");

        System.out.println(s1 == s2);
        System.out.println(s1.equals(s2));
        System.out.println(s1.compareTo(s2));
        System.out.println(s1.substring(1, 4));
        System.out.println(s1.indexOf('l'));

        StringBuilder builder = new StringBuilder("Java");
        builder.append(" OOP");
        builder.reverse();
        System.out.println(builder);
    }
}
