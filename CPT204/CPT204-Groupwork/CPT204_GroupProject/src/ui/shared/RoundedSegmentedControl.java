package ui.shared;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;

// Paints an Apple-style segmented control behind transparent toggle buttons.
// Shared UI - Provides segmented navigation controls for the main window.
public class RoundedSegmentedControl extends JPanel {
    private static final Color CONTROL_BACKGROUND = new Color(242, 243, 245);
    private static final Color CONTROL_BORDER = new Color(211, 215, 222);
    private static final Color SELECTED_BACKGROUND = Color.WHITE;
    private static final Color SELECTED_BORDER = new Color(196, 201, 210);
    private static final Color SEPARATOR = new Color(218, 222, 229);
    private static final Color SELECTED_TEXT = new Color(28, 35, 46);
    private static final Color TEXT = new Color(88, 96, 108);

    private final JToggleButton[] buttons;
    private final int cornerRadius;

    public RoundedSegmentedControl(int cornerRadius, Dimension buttonSize, JToggleButton... buttons) {
        super(new GridLayout(1, buttons.length, 0, 0));
        this.buttons = buttons;
        this.cornerRadius = cornerRadius;

        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

        for (JToggleButton button : buttons) {
            configureButton(button, buttonSize);
            button.addItemListener(event -> {
                button.setForeground(event.getStateChange() == ItemEvent.SELECTED ? SELECTED_TEXT : TEXT);
                repaint();
            });
            add(button);
        }
    }

    public static void select(JToggleButton selectedButton, JToggleButton otherButton) {
        selectedButton.setSelected(true);
        otherButton.setSelected(false);
    }

    private void configureButton(JToggleButton button, Dimension buttonSize) {
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setForeground(button.isSelected() ? SELECTED_TEXT : TEXT);
        button.setFont(button.getFont().deriveFont(Font.BOLD, 13.5f));
        button.setMargin(new Insets(0, 6, 0, 6));
        button.setPreferredSize(buttonSize);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g2 = (Graphics2D) graphics.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();
        g2.setColor(CONTROL_BACKGROUND);
        g2.fillRoundRect(0, 0, width - 1, height - 1, cornerRadius, cornerRadius);
        g2.setColor(CONTROL_BORDER);
        g2.drawRoundRect(0, 0, width - 1, height - 1, cornerRadius, cornerRadius);

        paintSelectedSegment(g2);
        paintSeparators(g2);
        g2.dispose();

        super.paintComponent(graphics);
    }

    private void paintSelectedSegment(Graphics2D g2) {
        for (JToggleButton button : buttons) {
            if (!button.isSelected()) {
                continue;
            }

            Rectangle bounds = button.getBounds();
            int arc = Math.max(10, cornerRadius - 5);
            g2.setColor(new Color(0, 0, 0, 18));
            g2.fillRoundRect(bounds.x, bounds.y + 1, bounds.width - 1, bounds.height - 1, arc, arc);
            g2.setColor(SELECTED_BACKGROUND);
            g2.fillRoundRect(bounds.x, bounds.y, bounds.width - 1, bounds.height - 1, arc, arc);
            g2.setColor(SELECTED_BORDER);
            g2.drawRoundRect(bounds.x, bounds.y, bounds.width - 1, bounds.height - 1, arc, arc);
            return;
        }
    }

    private void paintSeparators(Graphics2D g2) {
        g2.setColor(SEPARATOR);
        for (int index = 0; index < buttons.length - 1; index++) {
            if (buttons[index].isSelected() || buttons[index + 1].isSelected()) {
                continue;
            }

            Rectangle bounds = buttons[index].getBounds();
            int x = bounds.x + bounds.width;
            g2.drawLine(x, bounds.y + 7, x, bounds.y + bounds.height - 7);
        }
    }
}
