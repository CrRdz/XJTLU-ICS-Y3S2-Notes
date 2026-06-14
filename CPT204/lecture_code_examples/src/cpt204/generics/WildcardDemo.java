package cpt204.generics;

public class WildcardDemo {
    public static double sum(GenericStack<? extends Number> stack) {
        double total = 0;
        while (!stack.isEmpty()) {
            total += stack.pop().doubleValue();
        }
        return total;
    }

    public static void print(GenericStack<?> stack) {
        while (!stack.isEmpty()) {
            System.out.println(stack.pop());
        }
    }

    public static void main(String[] args) {
        GenericStack<Integer> stack = new GenericStack<>();
        stack.push(1);
        stack.push(2);
        System.out.println(sum(stack));
    }
}
