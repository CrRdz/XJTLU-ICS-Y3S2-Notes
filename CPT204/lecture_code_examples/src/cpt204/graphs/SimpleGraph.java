package cpt204.graphs;

import java.util.*;

public class SimpleGraph {
    private List<List<Integer>> neighbors = new ArrayList<>();

    public SimpleGraph(int numberOfVertices) {
        for (int i = 0; i < numberOfVertices; i++) {
            neighbors.add(new ArrayList<>());
        }
    }

    public void addEdge(int u, int v) {
        neighbors.get(u).add(v);
        neighbors.get(v).add(u);
    }

    public List<Integer> bfs(int start) {
        ArrayList<Integer> order = new ArrayList<>();
        boolean[] visited = new boolean[neighbors.size()];
        Queue<Integer> queue = new LinkedList<>();
        queue.offer(start);
        visited[start] = true;
        while (!queue.isEmpty()) {
            int u = queue.poll();
            order.add(u);
            for (int v : neighbors.get(u)) {
                if (!visited[v]) {
                    queue.offer(v);
                    visited[v] = true;
                }
            }
        }
        return order;
    }

    public List<Integer> dfs(int start) {
        ArrayList<Integer> order = new ArrayList<>();
        boolean[] visited = new boolean[neighbors.size()];
        dfs(start, visited, order);
        return order;
    }

    private void dfs(int u, boolean[] visited, List<Integer> order) {
        visited[u] = true;
        order.add(u);
        for (int v : neighbors.get(u)) {
            if (!visited[v]) {
                dfs(v, visited, order);
            }
        }
    }

    public static void main(String[] args) {
        SimpleGraph graph = new SimpleGraph(5);
        graph.addEdge(0, 1);
        graph.addEdge(0, 2);
        graph.addEdge(1, 3);
        graph.addEdge(2, 4);
        System.out.println(graph.dfs(0));
        System.out.println(graph.bfs(0));
    }
}
