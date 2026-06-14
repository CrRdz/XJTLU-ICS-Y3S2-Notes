package util;
import model.sorting.Candidate;
import model.graph.Graph;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// Shared Utility - Loads Task A candidate data and Task B graph data from CSV files.
public class FileLoader {
    // Reads all lines from csv file return List<lines>
    public List<String> readAllLines(String filePath) throws IOException {
        List<String> lines = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }

        return lines;
    }

    // Converts a candidate CSV file into Candidate objects.
    public List<Candidate> loadCandidates(String filePath) throws IOException {
        List<String> lines = readAllLines(filePath);
        List<Candidate> candidates = new ArrayList<>();

        // Skip the CSV header row from i=1, loop for lines
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) {
                continue;
            }

            String[] parts = line.split(","); // split by ,

            String locationId = parts[0].trim();
            double priorityScore = Double.parseDouble(parts[1].trim());

            candidates.add(new Candidate(locationId, priorityScore));
        }

        return candidates;
    }

    // Loads candidate dataset A.
    public List<Candidate> loadCandidatesA() throws IOException {
        return loadCandidates("data/candidates_A.csv");
    }

    // Loads candidate dataset B.
    public List<Candidate> loadCandidatesB() throws IOException {
        return loadCandidates("data/candidates_B.csv");
    }

    // Loads candidate dataset C.
    public List<Candidate> loadCandidatesC() throws IOException {
        return loadCandidates("data/candidates_C.csv");
    }

    // Builds an undirected weighted graph from paths.csv return graph
    public Graph loadGraph() throws IOException {
        List<String> lines = readAllLines("data/paths.csv");
        Graph graph = new Graph();

        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) {
                continue;
            }

            String[] parts = line.split(",");
            String fromLocation = parts[0].trim();
            String toLocation = parts[1].trim();
            int weight = Integer.parseInt(parts[2].trim());

            // Each path can be travelled in both directions.
            graph.addUndirectedEdge(fromLocation, toLocation, weight);
        }

        return graph;
    }
}
