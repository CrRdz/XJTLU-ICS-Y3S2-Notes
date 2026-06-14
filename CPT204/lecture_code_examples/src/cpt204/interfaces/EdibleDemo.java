package cpt204.interfaces;

interface Edible {
    String howToEat();
}

abstract class Animal {
}

class Chicken extends Animal implements Edible {
    @Override
    public String howToEat() {
        return "Fry it";
    }
}

abstract class Fruit implements Edible {
}

class Apple extends Fruit {
    @Override
    public String howToEat() {
        return "Make apple cider";
    }
}

public class EdibleDemo {
    public static void main(String[] args) {
        Edible[] items = {new Chicken(), new Apple()};
        for (Edible item : items) {
            System.out.println(item.howToEat());
        }
    }
}
