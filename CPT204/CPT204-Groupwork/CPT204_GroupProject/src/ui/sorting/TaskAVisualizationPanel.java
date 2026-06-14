package ui.sorting;

import model.result.sorting.DatasetBenchmarkResult;
import model.result.sorting.RuntimeStats;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;
import java.util.Map;

// Draws report-friendly Task A charts directly inside the Swing application.
// Task A UI - Draws benchmark charts for sorting algorithms.
public class TaskAVisualizationPanel extends JPanel {
    private static final Color BACKGROUND = Color.WHITE;
    private static final Color CARD_BACKGROUND = Color.WHITE;
    private static final Color AXIS_COLOR = new Color(93, 106, 126);
    private static final Color TEXT_COLOR = new Color(35, 44, 59);
    private static final Color MUTED_TEXT_COLOR = new Color(94, 108, 128);
    private static final Color BEST_COLOR = new Color(36, 133, 91);
    private static final Color[] RUNTIME_COLORS = {
            new Color(224, 93, 80),
            new Color(232, 164, 42),
            new Color(66, 145, 108),
            new Color(115, 99, 184)
    };

    private DatasetBenchmarkResult result;

    public TaskAVisualizationPanel() {
        setBackground(BACKGROUND);
        setPreferredSize(new Dimension(560, 520));
    }

    public void setResult(DatasetBenchmarkResult result) {
        this.result = result;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g2 = (Graphics2D) graphics.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (result == null) {
            drawEmptyState(g2);
            g2.dispose();
            return;
        }

        int gap = 16;
        int cardX = 12;
        int cardWidth = getWidth() - 24;
        int availableHeight = getHeight() - (gap * 3);
        int chartHeight = Math.max(190, (int) Math.round(availableHeight * 0.42));
        int insightHeight = availableHeight - chartHeight;

        drawRuntimeChart(g2, cardX, gap, cardWidth, chartHeight);
        drawInsightPanel(g2, cardX, gap + chartHeight + gap, cardWidth, insightHeight);

        g2.dispose();
    }

    private void drawEmptyState(Graphics2D g2) {
        drawCard(g2, 12, 16, getWidth() - 24, getHeight() - 32);
        g2.setColor(TEXT_COLOR);
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 16f));
        drawCenteredString(g2, "Run Task A to generate report-ready charts.", getWidth() / 2, getHeight() / 2);
    }

    private void drawRuntimeChart(Graphics2D g2, int x, int y, int width, int height) {
        drawCard(g2, x, y, width, height);
        drawTitle(g2, "Average Runtime by Sorting Algorithm - " + result.datasetName(), x + 18, y + 26);

        List<Map.Entry<String, RuntimeStats>> entries = new ArrayList<>(result.runtimes().entrySet());
        if (entries.isEmpty()) {
            return;
        }

        double maxRuntime = entries.stream().mapToDouble(entry -> entry.getValue().averageMs()).max().orElse(1.0);
        int plotX = x + 64;
        int plotY = y + 48;
        int plotWidth = width - 112;
        int plotHeight = height - 106;
        int barGap = 28;
        int barWidth = Math.max(34, (plotWidth - (barGap * (entries.size() - 1))) / entries.size());
        FontMetrics metrics = g2.getFontMetrics();

        drawAxes(g2, plotX, plotY, plotWidth, plotHeight);

        for (int index = 0; index < entries.size(); index++) {
            Map.Entry<String, RuntimeStats> entry = entries.get(index);
            RuntimeStats stats = entry.getValue();
            int barHeight = (int) Math.round((stats.averageMs() / maxRuntime) * (plotHeight - 8));
            int barX = plotX + index * (barWidth + barGap);
            int barY = plotY + plotHeight - barHeight;

            g2.setColor(RUNTIME_COLORS[index % RUNTIME_COLORS.length]);
            g2.fillRoundRect(barX, barY, barWidth, barHeight, 6, 6);
            g2.setColor(TEXT_COLOR);
            String value = String.format("%.3f ms", stats.averageMs());
            g2.drawString(value, barX + (barWidth - metrics.stringWidth(value)) / 2, barY - 5);

            String label = entry.getKey().replace(" Sort", "");
            g2.drawString(label, barX + (barWidth - metrics.stringWidth(label)) / 2, plotY + plotHeight + 20);

            String stability = "sd " + String.format("%.3f", stats.standardDeviationMs());
            g2.setColor(AXIS_COLOR);
            g2.drawString(stability, barX + (barWidth - metrics.stringWidth(stability)) / 2, plotY + plotHeight + 36);
        }

    }

    private void drawInsightPanel(Graphics2D g2, int x, int y, int width, int height) {
        drawCard(g2, x, y, width, height);
        drawTitle(g2, "Benchmark Interpretation", x + 18, y + 26);

        List<Map.Entry<String, RuntimeStats>> entries = new ArrayList<>(result.runtimes().entrySet());
        if (entries.isEmpty()) {
            return;
        }

        Map.Entry<String, RuntimeStats> fastest = entries.stream()
                .min(Comparator.comparingDouble(entry -> entry.getValue().averageMs()))
                .orElse(entries.get(0));
        Map.Entry<String, RuntimeStats> mostStable = entries.stream()
                .min(Comparator.comparingDouble(entry -> entry.getValue().standardDeviationMs()))
                .orElse(entries.get(0));
        Map.Entry<String, RuntimeStats> slowest = entries.stream()
                .max(Comparator.comparingDouble(entry -> entry.getValue().averageMs()))
                .orElse(entries.get(0));

        int boxY = y + 52;
        int boxHeight = 56;
        int gap = 14;
        int boxWidth = (width - 36 - (gap * 2)) / 3;

        drawMetricBox(g2, x + 18, boxY, boxWidth, boxHeight, "Fastest average",
                fastest.getKey(), formatMs(fastest.getValue().averageMs()), BEST_COLOR);
        drawMetricBox(g2, x + 18 + boxWidth + gap, boxY, boxWidth, boxHeight, "Most stable",
                mostStable.getKey(), "sd " + formatMs(mostStable.getValue().standardDeviationMs()), new Color(47, 118, 186));
        drawMetricBox(g2, x + 18 + (boxWidth + gap) * 2, boxY, boxWidth, boxHeight, "Slowest average",
                slowest.getKey(), formatMs(slowest.getValue().averageMs()), new Color(190, 80, 76));

        int tableY = boxY + boxHeight + 24;
        drawComparisonTable(g2, entries, x + 20, tableY, width - 40);
    }

    private void drawMetricBox(
            Graphics2D g2,
            int x,
            int y,
            int width,
            int height,
            String label,
            String algorithm,
            String value,
            Color accent
    ) {
        g2.setColor(accent);
        g2.fillRoundRect(x, y, 6, height, 9, 9);

        g2.setColor(MUTED_TEXT_COLOR);
        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 11f));
        g2.drawString(label, x + 16, y + 19);
        g2.setColor(TEXT_COLOR);
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 14f));
        g2.drawString(algorithm, x + 16, y + 39);
        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 12f));
        g2.drawString(value, x + 16, y + 56);
    }

    private void drawComparisonTable(
            Graphics2D g2,
            List<Map.Entry<String, RuntimeStats>> entries,
            int x,
            int y,
            int width
    ) {
        int rowHeight = 28;
        int nameX = x;
        int averageX = x + width / 3;
        int medianX = x + width / 2;
        int deviationX = x + (width * 2 / 3);
        int rangeX = x + (width * 5 / 6);

        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 12f));
        g2.setColor(TEXT_COLOR);
        g2.drawString("Algorithm", nameX, y);
        g2.drawString("Avg", averageX, y);
        g2.drawString("Median", medianX, y);
        g2.drawString("Std dev", deviationX, y);
        g2.drawString("Range", rangeX, y);

        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 12f));
        for (int index = 0; index < entries.size(); index++) {
            Map.Entry<String, RuntimeStats> entry = entries.get(index);
            RuntimeStats stats = entry.getValue();
            int rowY = y + 22 + index * rowHeight;
            g2.setColor(TEXT_COLOR);
            g2.drawString(entry.getKey(), nameX, rowY);
            g2.drawString(formatMs(stats.averageMs()), averageX, rowY);
            g2.drawString(formatMs(stats.medianMs()), medianX, rowY);
            g2.drawString(formatMs(stats.standardDeviationMs()), deviationX, rowY);
            g2.drawString(formatMs(stats.rangeMs()), rangeX, rowY);
        }
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
}
