package cpt204.inheritance;

public class PolymorphismDemo {
    public static void display(GeometricObject object) {
        System.out.println(object);
        System.out.println("Area = " + object.getArea());
    }

    public static void main(String[] args) {
        display(new Circle(2));
        display(new Rectangle(3, 4));

        GeometricObject object = new Circle(5);
        if (object instanceof Circle) {
            Circle circle = (Circle) object;
            System.out.println(circle.getRadius());
        }
    }
}
