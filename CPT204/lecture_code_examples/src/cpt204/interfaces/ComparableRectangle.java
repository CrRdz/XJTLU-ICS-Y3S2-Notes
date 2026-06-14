package cpt204.interfaces;

public class ComparableRectangle implements Comparable<ComparableRectangle> {
    private double width;
    private double height;

    public ComparableRectangle(double width, double height) {
        this.width = width;
        this.height = height;
    }

    public double getArea() {
        return width * height;
    }

    @Override
    public int compareTo(ComparableRectangle other) {
        return Double.compare(getArea(), other.getArea());
    }

    public static void main(String[] args) {
        ComparableRectangle r1 = new ComparableRectangle(3, 4);
        ComparableRectangle r2 = new ComparableRectangle(5, 6);
        System.out.println(r1.compareTo(r2));
    }
}
