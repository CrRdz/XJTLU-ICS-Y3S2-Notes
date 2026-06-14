package cpt204.graphs;

import java.util.Arrays;

public class DijkstraDemo {
    public static int[] shortestPaths(int[][] weight, int source) {
        int n = weight.length;
        int[] distance = new int[n];
        boolean[] visited = new boolean[n];
        Arrays.fill(distance, Integer.MAX_VALUE / 2);
        distance[source] = 0;

        for (int i = 0; i < n; i++) {
            int u = -1;
            for (int j = 0; j < n; j++) {
                if (!visited[j] && (u == -1 || distance[j] < distance[u])) {
                    u = j;
                }
            }
            visited[u] = true;
            for (int v = 0; v < n; v++) {
                if (weight[u][v] > 0 && distance[u] + weight[u][v] < distance[v]) {
                    distance[v] = distance[u] + weight[u][v];
                }
            }
        }
        return distance;
    }
}
