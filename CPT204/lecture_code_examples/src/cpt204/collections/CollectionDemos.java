package cpt204.collections;

import java.util.*;

public class CollectionDemos {
    public static void main(String[] args) {
        ArrayList<String> list = new ArrayList<>(Arrays.asList("London", "Paris", "Suzhou"));
        LinkedList<String> linkedList = new LinkedList<>(list);

        Stack<String> stack = new Stack<>();
        stack.push("A");
        stack.push("B");

        Queue<String> queue = new LinkedList<>();
        queue.offer("A");
        queue.offer("B");

        PriorityQueue<String> pq = new PriorityQueue<>(Arrays.asList("Liverpool", "Suzhou", "London", "Beijing"));

        System.out.println(list.get(0));
        System.out.println(linkedList.removeFirst());
        System.out.println(stack.pop());
        System.out.println(queue.poll());
        while (!pq.isEmpty()) {
            System.out.print(pq.poll() + " ");
        }
    }
}
