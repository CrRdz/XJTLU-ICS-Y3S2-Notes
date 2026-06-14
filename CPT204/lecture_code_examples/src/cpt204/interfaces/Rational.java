package cpt204.interfaces;

public class Rational extends Number implements Comparable<Rational> {
    private long numerator;
    private long denominator;

    public Rational(long numerator, long denominator) {
        long gcd = gcd(Math.abs(numerator), Math.abs(denominator));
        this.numerator = denominator > 0 ? numerator / gcd : -numerator / gcd;
        this.denominator = Math.abs(denominator) / gcd;
    }

    private static long gcd(long n, long d) {
        long n1 = n;
        long n2 = d;
        while (n2 != 0) {
            long temp = n2;
            n2 = n1 % n2;
            n1 = temp;
        }
        return n1;
    }

    @Override
    public int compareTo(Rational other) {
        return Double.compare(doubleValue(), other.doubleValue());
    }

    @Override
    public int intValue() {
        return (int) doubleValue();
    }

    @Override
    public long longValue() {
        return (long) doubleValue();
    }

    @Override
    public float floatValue() {
        return (float) doubleValue();
    }

    @Override
    public double doubleValue() {
        return numerator * 1.0 / denominator;
    }
}
