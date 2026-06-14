package cpt204.algorithms;

public class AlgorithmExamples {
    public static int logLoopCount(int n) {
        int count = 0;
        for (int i = 1; i < n; i *= 2) {
            count++;
        }
        return count;
    }

    public static long fibRecursive(long index) {
        if (index == 0) return 0;
        if (index == 1) return 1;
        return fibRecursive(index - 1) + fibRecursive(index - 2);
    }

    public static long gcd(long m, long n) {
        while (n != 0) {
            long temp = n;
            n = m % n;
            m = temp;
        }
        return m;
    }

    public static int stringMatch(String text, String pattern) {
        for (int i = 0; i <= text.length() - pattern.length(); i++) {
            int k = 0;
            while (k < pattern.length() && text.charAt(i + k) == pattern.charAt(k)) {
                k++;
            }
            if (k == pattern.length()) {
                return i;
            }
        }
        return -1;
    }

    public static void main(String[] args) {
        System.out.println(logLoopCount(100));
        System.out.println(fibRecursive(8));
        System.out.println(gcd(24, 16));
        System.out.println(stringMatch("hello java", "java"));
    }
}
