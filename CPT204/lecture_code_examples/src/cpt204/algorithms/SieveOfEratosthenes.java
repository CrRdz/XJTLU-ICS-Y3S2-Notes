package cpt204.algorithms;

import java.util.Arrays;

public class SieveOfEratosthenes {
    public static boolean[] sieve(int n) {
        boolean[] primes = new boolean[n + 1];
        Arrays.fill(primes, true);
        if (n >= 0) primes[0] = false;
        if (n >= 1) primes[1] = false;

        for (int k = 2; k * k <= n; k++) {
            if (primes[k]) {
                for (int i = k * k; i <= n; i += k) {
                    primes[i] = false;
                }
            }
        }
        return primes;
    }

    public static void main(String[] args) {
        boolean[] primes = sieve(30);
        for (int i = 0; i < primes.length; i++) {
            if (primes[i]) {
                System.out.print(i + " ");
            }
        }
    }
}
