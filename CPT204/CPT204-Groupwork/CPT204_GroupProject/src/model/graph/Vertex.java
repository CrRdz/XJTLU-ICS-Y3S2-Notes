package model.graph;

import java.util.Objects;

// Task B - Represents one location node in the graph.
public class Vertex {
    private final String id;

    public Vertex(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Vertex other)) {
            return false;
        }
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        // Print the location ID when the vertex is displayed.
        return id;
    }
}
