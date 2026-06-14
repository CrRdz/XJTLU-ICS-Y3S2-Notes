package ui.path;

import model.result.path.PathComparisonResult;
import model.result.path.PathRunResult;
import util.FormatUtils;

import javax.swing.*;
import java.awt.*;
import java.util.List;

// Task B UI - Draws comparison charts for weighted shortest-path algorithms.
public class PathComparisonVisualizationPanel extends JPanel {
    private static final Color BACKGROUND = Color.WHITE;
    private static final Color CARD_BACKGROUND = Color.WHITE;
    private static final Color AXIS_COLOR = new Color(93, 106, 126);
    private static final Color TEXT_COLOR = new Color(35, 44, 59);
    private static final Color MUTED_TEXT_COLOR = new Color(94, 108, 128);
    private static final Color[] ALGORITHM_COLORS = {
            new Color(47, 118, 186),
            new Color(224, 93, 80),
            new Color(66, 145, 108)
    };

    private PathComparisonResult comparison;

    public PathComparisonVisualizationPanel() {
        setBackground(BACKGROUND);
        setPreferredSize(new Dimension(360, 520));
    }

    public void setComparison(PathComparisonResult comparison) {
        this.comparison = comparison;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g2 = (Graphics2D) graphics.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (comparison == null) {
            drawEmptyState(g2);
            g2.dispose();
            return;
        }

        int gap = 16;
        int x = 12;
        int width = getWidth() - 24;
        int availableHeight = getHeight() - gap * 3;
        int chartHeight = Math.max(230, (int) Math.round(availableHeight * 0.54));
        int insightHeight = availableHeight - chartHeight;

        drawRuntimeChart(g2, x, gap, width, chartHeight);
        drawInsightPanel(g2, x, gap + chartHeight + gap, width, insightHeight);
        g2.dispose();
    }

    private void drawEmptyState(Graphics2D g2) {
        drawCard(g2, 12, 16, getWidth() - 24, getHeight() - 32);
        g2.setColor(TEXT_COLOR);
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 15f));
        drawCenteredString(g2, "Run a case to compare weighted shortest-path algorithms.",
                getWidth() / 2, getHeight() / 2);
    }

    private void drawRuntimeChart(Graphics2D g2, int x, int y, int width, int height) {
        drawCard(g2, x, y, width, height);
        drawTitle(g2, "Runtime by Shortest-Path Algorithm - " + comparison.caseName(), x + 18, y + 26);

        List<AlgorithmRun> runs = algorithmRuns();
        double maxRuntime = runs.stream()
                .mapToDouble(run -> run.result.runtimeMs())
                .max()
                .orElse(1.0);
        maxRuntime = Math.max(0.001, maxRuntime);

        int plotX = x + 72;
        int plotY = y + 56;
        int plotWidth = width - 132;
        int plotHeight = height - 130;
        drawAxes(g2, plotX, plotY, plotWidth, plotHeight);

        int barGap = 26;
        int barWidth = Math.max(38, (plotWidth - barGap * (runs.size() - 1)) / runs.size());
        for (int index = 0; index < runs.size(); index++) {
            AlgorithmRun run = runs.get(index);
            int barX = plotX + index * (barWidth + barGap);
            drawRuntimeBar(g2, run.name, run.result.runtimeMs(), maxRuntime, barX, plotY,
                    plotHeight, barWidth, ALGORITHM_COLORS[index % ALGORITHM_COLORS.length]);
        }

        g2.setColor(MUTED_TEXT_COLOR);
        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 11f));
        g2.drawString("Runtime in milliseconds", plotX, y + height - 28);
    }

    private void drawRuntimeBar(
            Graphics2D g2,
            String label,
            double value,
            double maxValue,
            int x,
            int plotY,
            int plotHeight,
            int width,
            Color color
    ) {
        int barHeight = (int) Math.round((value / maxValue) * (plotHeight - 8));
        int barY = plotY + plotHeight - barHeight;
        FontMetrics metrics = g2.getFontMetrics();

        g2.setColor(color);
        g2.fillRoundRect(x, barY, width, barHeight, 6, 6);

        String valueText = formatMs(value);
        g2.setColor(TEXT_COLOR);
        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 12f));
        g2.drawString(valueText, x + (width - metrics.stringWidth(valueText)) / 2, barY - 6);
        g2.drawString(label, x + (width - metrics.stringWidth(label)) / 2, plotY + plotHeight + 20);
    }

    private void drawInsightPanel(Graphics2D g2, int x, int y, int width, int height) {
        drawCard(g2, x, y, width, height);
        drawTitle(g2, "Algorithm Comparison", x + 18, y + 26);

        List<AlgorithmRun> runs = algorithmRuns();
        int boxY = y + 52;
        int boxHeight = 58;
        int gap = 10;
        int boxWidth = (width - 36 - gap * (runs.size() - 1)) / runs.size();

        for (int index = 0; index < runs.size(); index++) {
            AlgorithmRun run = runs.get(index);
            drawMetricBox(g2, x + 18 + index * (boxWidth + gap), boxY, boxWidth, boxHeight,
                    run.name, formatMs(run.result.runtimeMs()),
                    "distance " + FormatUtils.formatCost(run.result.pathResult().getDistance()),
                    ALGORITHM_COLORS[index % ALGORITHM_COLORS.length]);
        }

        int tableY = boxY + boxHeight + 28;
        drawComparisonTable(g2, x + 20, tableY, width - 40);
    }

    private void drawMetricBox(
            Graphics2D g2,
            int x,
            int y,
            int width,
            int height,
            String label,
            String value,
            String detail,
            Color accent
    ) {
        g2.setColor(accent);
        g2.fillRoundRect(x, y, 6, height, 9, 9);

        g2.setColor(MUTED_TEXT_COLOR);
        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 11f));
        g2.drawString(label, x + 16, y + 19);
        g2.setColor(TEXT_COLOR);
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 14f));
        g2.drawString(value, x + 16, y + 39);
        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 12f));
        g2.drawString(detail, x + 16, y + 56);
    }

    private void drawComparisonTable(Graphics2D g2, int x, int y, int width) {
        int distanceX = x + width / 3;
        int nodesX = x + width * 2 / 3;
        int runtimeX = x + width * 5 / 6;

        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 12f));
        g2.setColor(TEXT_COLOR);
        g2.drawString("Algorithm", x, y);
        g2.drawString("Distance", distanceX, y);
        g2.drawString("Nodes", nodesX, y);
        g2.drawString("Runtime", runtimeX, y);

        List<AlgorithmRun> runs = algorithmRuns();
        for (int index = 0; index < runs.size(); index++) {
            AlgorithmRun run = runs.get(index);
            drawTableRow(g2, run.name, run.result, x, distanceX, nodesX, runtimeX, y + 26 + index * 28);
        }
    }

    private void drawTableRow(
            Graphics2D g2,
            String algorithm,
            PathRunResult result,
            int nameX,
            int distanceX,
            int nodesX,
            int runtimeX,
            int y
    ) {
        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 12f));
        g2.setColor(TEXT_COLOR);
        g2.drawString(algorithm, nameX, y);
        g2.drawString(FormatUtils.formatCost(result.pathResult().getDistance()), distanceX, y);
        g2.drawString(String.valueOf(result.pathResult().getPath().size()), nodesX, y);
        g2.drawString(formatMs(result.runtimeMs()), runtimeX, y);
    }

    private void drawCard(Graphics2D g2, int x, int y, int width, int height) {
        g2.setColor(CARD_BACKGROUND);
        g2.fillRect(x, y, width, height);
    }

    private void drawTitle(Graphics2D g2, String title, int x, int y) {
        g2.setColor(TEXT_COLOR);
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 15f));
        g2.drawString(title, x, y);
        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 12f));
    }

    private void drawAxes(Graphics2D g2, int x, int y, int width, int height) {
        g2.setColor(AXIS_COLOR);
        g2.drawLine(x, y, x, y + height);
        g2.drawLine(x, y + height, x + width, y + height);
    }

    private void drawCenteredString(Graphics2D g2, String text, int centerX, int centerY) {
        FontMetrics metrics = g2.getFontMetrics();
        g2.drawString(text, centerX - metrics.stringWidth(text) / 2, centerY);
    }

    private String formatMs(double value) {
        return String.format("%.3f ms", value);
    }

    private List<AlgorithmRun> algorithmRuns() {
        return List.of(
                new AlgorithmRun("Dijkstra", comparison.dijkstraResult()),
                new AlgorithmRun("Bellman-Ford", comparison.bellmanFordResult()),
                new AlgorithmRun("Floyd-Warshall", comparison.floydWarshallResult())
        );
    }

    private static class AlgorithmRun {
        private final String name;
        private final PathRunResult result;

        private AlgorithmRun(String name, PathRunResult result) {
            this.name = name;
            this.result = result;
        }
    }
}
