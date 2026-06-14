package cpt204.sorting;

import java.util.Arrays;

public class HeapSort {
    public static <E extends Comparable<E>> void heapSort(E[] list) {
        Heap<E> heap = new Heap<>();
        for (E value : list) {
            heap.add(value);
        }
        for (int i = list.length - 1; i >= 0; i--) {
            list[i] = heap.remove();
        }
    }

    public static void main(String[] args) {
        Integer[] list = {2, 3, 2, 5, 6, 1, -2, 3, 14, 12};
        heapSort(list);
        System.out.println(Arrays.toString(list));
    }
}
