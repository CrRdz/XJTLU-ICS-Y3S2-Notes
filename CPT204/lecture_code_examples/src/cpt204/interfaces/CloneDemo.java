package cpt204.interfaces;

import java.util.Date;

class House implements Cloneable, Comparable<House> {
    private int id;
    private double area;
    private Date whenBuilt = new Date();

    House(int id, double area) {
        this.id = id;
        this.area = area;
    }

    public Date getWhenBuilt() {
        return whenBuilt;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public int compareTo(House other) {
        return Double.compare(area, other.area);
    }
}

public class CloneDemo {
    public static void main(String[] args) throws CloneNotSupportedException {
        House h1 = new House(1, 1750.50);
        House h2 = (House) h1.clone();
        System.out.println(h1.getWhenBuilt() == h2.getWhenBuilt());
    }
}
