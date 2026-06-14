package model.sorting;
// Task A - Represents one candidate location with a priority score.
public class Candidate implements Comparable<Candidate> {
    private final String locationId;
    private final double priorityScore;

    public Candidate(String locationId, double priorityScore) {
        this.locationId = locationId;
        this.priorityScore = priorityScore;
    }

    public String getLocationId() {
        return locationId;
    }

    public double getPriorityScore() {
        return priorityScore;
    }

    @Override
    public int compareTo(Candidate other) {
        // Higher priority scores should appear first.
        int scoreComparison = Double.compare(other.priorityScore, this.priorityScore);
        if (scoreComparison != 0) {
            return scoreComparison;
        }

        // if scores are equal sort by location id
        return this.locationId.compareTo(other.locationId);
    }

    @Override
    public String toString() {
        return "Candidate{" +
                "locationId='" + locationId + '\'' +
                ", priorityScore=" + priorityScore +
                '}';
    }
}
