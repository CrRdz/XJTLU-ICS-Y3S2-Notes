package cpt204.collections;

import java.util.*;

public class SetMapDemos {
    public static void main(String[] args) {
        Set<String> hashSet = new HashSet<>(Arrays.asList("London", "Paris", "London"));
        Set<String> linkedHashSet = new LinkedHashSet<>(Arrays.asList("London", "Paris", "London"));
        Set<String> treeSet = new TreeSet<>(Arrays.asList("London", "Paris", "Beijing"));

        Map<String, Integer> hashMap = new HashMap<>();
        hashMap.put("Apple", 1);
        hashMap.put("Banana", 2);

        LinkedHashMap<String, Integer> accessOrderMap = new LinkedHashMap<>(16, 0.75f, true);
        accessOrderMap.put("Apple", 1);
        accessOrderMap.put("Banana", 2);
        accessOrderMap.put("Cherry", 3);
        accessOrderMap.get("Banana");

        Map<String, Integer> treeMap = new TreeMap<>(hashMap);

        System.out.println(hashSet);
        System.out.println(linkedHashSet);
        System.out.println(treeSet);
        System.out.println(accessOrderMap);
        System.out.println(treeMap);
    }
}
