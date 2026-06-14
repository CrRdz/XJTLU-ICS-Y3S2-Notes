# CPT204 Code Index

This index maps the organized Java files to lecture code topics.

## Arrays

- `cpt204.arrays.ArrayBasics`
  - array declaration
  - array creation
  - default values
  - enhanced for loop
- `cpt204.arrays.ArrayAlgorithms`
  - `printArray`
  - `reverseCopy`
  - `reverseInPlace`
  - `linearSearch`
  - `binarySearch`
  - `selectionSort`
  - `insertionSort`
- `cpt204.arrays.PassByValueDemo`
  - primitive parameter vs array parameter
  - Java pass-by-value

## Objects and Classes

- `cpt204.oop.Circle`
  - constructor
  - field encapsulation
  - getter/setter
  - instance method
- `cpt204.oop.Course`
  - object design
  - array as a field
  - defensive copy style getter
- `cpt204.oop.Loan`
  - class abstraction
  - loan formula example
- `cpt204.oop.StringExamples`
  - `String`
  - `==` vs `equals`
  - `compareTo`
  - `substring`
  - `indexOf`
  - `StringBuilder`
- `cpt204.oop.StackOfIntegers`
  - custom stack
  - static final constant
  - array-backed container

## Inheritance and Polymorphism

- `cpt204.inheritance.GeometricObject`
  - abstract superclass
  - protected constructor
  - abstract methods
- `cpt204.inheritance.Circle`
  - extends superclass
  - `super`
  - override methods
- `cpt204.inheritance.Rectangle`
  - extends superclass
  - implements abstract methods
- `cpt204.inheritance.PolymorphismDemo`
  - polymorphism
  - dynamic binding
  - `instanceof`
  - casting
- `cpt204.inheritance.OverloadingDemo`
  - method overloading

## Abstract Classes and Interfaces

- `cpt204.interfaces.EdibleDemo`
  - interface
  - implements
  - abstract class
- `cpt204.interfaces.ComparableRectangle`
  - `Comparable`
  - `compareTo`
- `cpt204.interfaces.CloneDemo`
  - `Cloneable`
  - shallow copy
- `cpt204.interfaces.Rational`
  - extends `Number`
  - implements `Comparable`
  - `BigInteger`/`BigDecimal` adjacent rational-number topic

## Generics

- `cpt204.generics.GenericStack`
  - generic class
  - generic container
- `cpt204.generics.GenericMethodsDemo`
  - generic method
  - bounded generic type
- `cpt204.generics.WildcardDemo`
  - wildcard
  - `?`
  - `? extends Number`

## Collections

- `cpt204.collections.CollectionDemos`
  - `ArrayList`
  - `LinkedList`
  - `Stack`
  - `Queue`
  - `PriorityQueue`
  - `poll`
- `cpt204.collections.ComparatorDemos`
  - `Comparator`
  - `Comparator.comparing`
  - `reversed`
- `cpt204.collections.SetMapDemos`
  - `HashSet`
  - `LinkedHashSet`
  - `TreeSet`
  - `HashMap`
  - `LinkedHashMap`
  - `TreeMap`
- `cpt204.collections.KeywordCounter`
  - `HashSet`
  - `HashMap`
  - keyword lookup/counting

## Algorithms

- `cpt204.algorithms.AlgorithmExamples`
  - `O(log n)` loop
  - Fibonacci recursion
  - Euclidean GCD
  - string matching
- `cpt204.algorithms.SieveOfEratosthenes`
  - prime-number sieve

## Sorting and Heaps

- `cpt204.sorting.SortingAlgorithms`
  - bubble sort
  - merge sort
  - quick sort
  - partition
- `cpt204.sorting.Heap`
  - binary max heap
  - add
  - remove root
- `cpt204.sorting.HeapSort`
  - heap sort

## Graphs

- `cpt204.graphs.SimpleGraph`
  - adjacency list
  - DFS
  - BFS
- `cpt204.graphs.DijkstraDemo`
  - single-source shortest path
  - weighted graph adjacency matrix

## Trees and Hashing

- `cpt204.trees.BST`
  - `TreeNode`
  - insert
  - search
  - inorder traversal
- `cpt204.trees.AVLTree`
  - AVL node height
  - balance factor
  - LL rotation
  - RR rotation
- `cpt204.trees.HashingDemo`
  - hash table
  - linear probing
