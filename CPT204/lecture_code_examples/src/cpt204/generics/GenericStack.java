package cpt204.generics;

import java.util.ArrayList;

public class GenericStack<E> {
    private ArrayList<E> list = new ArrayList<>();

    public int getSize() {
        return list.size();
    }

    public E peek() {
        return list.get(getSize() - 1);
    }

    public void push(E value) {
        list.add(value);
    }

    public E pop() {
        return list.remove(getSize() - 1);
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }
}
