# CPT204 Lecture Code Examples

This folder reorganizes lecture code into Java packages.

Structure:

- `cpt204.arrays`: arrays, pass-by-value, searching, basic sorting
- `cpt204.oop`: classes, objects, encapsulation, strings, simple models
- `cpt204.inheritance`: inheritance, overriding, overloading, polymorphism
- `cpt204.interfaces`: abstract classes, interfaces, comparable, cloneable
- `cpt204.generics`: generic stack, generic methods, wildcards
- `cpt204.collections`: list, stack, queue, priority queue, set, map, comparator
- `cpt204.algorithms`: Big O style loops, recursion, sieve, string matching
- `cpt204.sorting`: bubble, merge, quick, heap sort
- `cpt204.graphs`: graph, DFS, BFS, Dijkstra
- `cpt204.trees`: BST, AVL, hashing demos

Compile all:

```bash
javac -d out $(find src -name '*.java')
```

Run one example:

```bash
java -cp out cpt204.arrays.PassByValueDemo
```
