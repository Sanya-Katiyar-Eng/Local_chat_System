
package gui;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

public final class Theme {

    private Theme() {}

    public static final Color BACKGROUND = new Color(15, 18, 33);
    public static final Color SIDEBAR = new Color(21, 25, 44);
    public static final Color PANEL = new Color(27, 32, 54);
    public static final Color INPUT = new Color(35, 41, 66);

    public static final Color PURPLE = new Color(132, 94, 247);
    public static final Color PURPLE_HOVER = new Color(151, 119, 255);

    public static final Color TEXT = new Color(242, 244, 255);
    public static final Color MUTED = new Color(153, 163, 190);
    public static final Color GREEN = new Color(65, 210, 145);
    public static final Color BORDER = new Color(49, 56, 82);

    public static final Font TITLE_FONT =
            new Font("Segoe UI", Font.BOLD, 26);
    public static final Font HEADING_FONT =
            new Font("Segoe UI", Font.BOLD, 17);
    public static final Font NORMAL_FONT =
            new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font SMALL_FONT =
            new Font("Segoe UI", Font.PLAIN, 12);

    public static void styleButton(JButton button) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(PURPLE);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(12, 18, 12, 18));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);
        button.setBorderPainted(false);

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(PURPLE_HOVER);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(PURPLE);
            }
        });
    }

    public static void styleTextField(JTextField field) {
        field.setFont(NORMAL_FONT);
        field.setForeground(TEXT);
        field.setBackground(INPUT);
        field.setCaretColor(TEXT);
        field.setBorder(new CompoundBorder(
                new LineBorder(BORDER, 1, true),
                new EmptyBorder(12, 14, 12, 14)
        ));
    }

    public static JLabel label(String text, Font font, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        label.setForeground(color);
        return label;
    }
}
