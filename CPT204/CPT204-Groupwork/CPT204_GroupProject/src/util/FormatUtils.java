package util;

// Shared Utility - Formats values displayed in Task A and Task B results.
public final class FormatUtils {
    private FormatUtils() {
    }

    public static String formatCost(double cost) {
        if (Math.rint(cost) == cost) {
            return String.format("%.0f", cost);
        }
        return String.format("%.1f", cost);
    }
}
