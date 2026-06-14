package cpt204.collections;

import cpt204.oop.Loan;
import java.util.*;

public class ComparatorDemos {
    public static void main(String[] args) {
        ArrayList<String> words = new ArrayList<>(Arrays.asList("Java", "OOP", "PriorityQueue"));
        words.sort(Comparator.comparing(String::length));
        System.out.println(words);

        ArrayList<Loan> loans = new ArrayList<>();
        loans.add(new Loan(5, 10, 10000));
        loans.add(new Loan(5, 10, 5000));
        loans.sort(Comparator.comparing(Loan::getLoanAmount).reversed());
        System.out.println(loans.get(0).getLoanAmount());
    }
}
