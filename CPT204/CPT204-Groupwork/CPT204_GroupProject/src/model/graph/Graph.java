package model.graph;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Task B - Stores location vertices and weighted paths.
public class Graph {
    // Fast key-based lookup is sufficient; insertion order is not required.
    private final Map<String, Vertex> vertices = new HashMap<>(); // id-vertex
    private final Map<Vertex, List<Edge>> adjacencyList = new HashMap<>(); // vertex-edges

    public Vertex addVertex(String id) {
        Vertex vertex = vertices.computeIfAbsent(id, Vertex::new);
        adjacencyList.computeIfAbsent(vertex, key -> new ArrayList<>());
        return vertex;
    }

    // Adds one directed weighted edge.
    public void addEdge(String fromId, String toId, int weight) {
        Vertex from = addVertex(fromId);
        Vertex to = addVertex(toId);
        adjacencyList.get(from).add(new Edge(from, to, weight));
    }

    // Adds two opposite directed edges to represent an undirected path.
    public void addUndirectedEdge(String firstId, String secondId, int weight) {
        addEdge(firstId, secondId, weight);
        addEdge(secondId, firstId, weight);
    }

    // Returns the vertex for a location ID, or null if it is missing.
    public Vertex getVertex(String id) {
        return vertices.get(id);
    }

    // Returns all outgoing edges for a vertex.
    public List<Edge> getEdgesFrom(Vertex vertex) {
        return adjacencyList.getOrDefault(vertex, Collections.emptyList());
    }

    // Exposes all vertices without allowing external modification.
    public Collection<Vertex> getVertices() {
        return Collections.unmodifiableCollection(vertices.values());
    }

    // Exposes the adjacency list as a read-only map.
    public Map<Vertex, List<Edge>> getAdjacencyList() {
        return Collections.unmodifiableMap(adjacencyList);
    }
}
