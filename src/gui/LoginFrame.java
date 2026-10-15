
package gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

public class LoginFrame extends JFrame {

    private JTextField usernameField;
    private JLabel statusLabel;

    public LoginFrame() {
        setTitle("Linkora | Connect Locally. Chat Instantly.");
        setSize(1000, 650);
        setMinimumSize(new Dimension(800, 550));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(true);

        JPanel root = new JPanel(new GridLayout(1, 2));
        root.setBackground(Theme.BACKGROUND);

        root.add(createBrandPanel());
        root.add(createLoginPanel());

        setContentPane(root);
    }

    private JPanel createBrandPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Theme.SIDEBAR);
        panel.setBorder(new EmptyBorder(65, 45, 50, 35));

        JLabel logo = new JLabel("L");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 38));
        logo.setForeground(Color.WHITE);
        logo.setHorizontalAlignment(SwingConstants.CENTER);
        logo.setOpaque(true);
        logo.setBackground(Theme.PURPLE);
        logo.setPreferredSize(new Dimension(70, 70));
        logo.setMaximumSize(new Dimension(70, 70));
        logo.setBorder(new EmptyBorder(8, 20, 8, 20));
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel brand = Theme.label(
                "Linkora", new Font("Segoe UI", Font.BOLD, 36),
                Theme.TEXT);
        brand.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel tagline = Theme.label(
                "Connect Locally. Chat Instantly.",
                Theme.NORMAL_FONT, Theme.MUTED);
        tagline.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(logo);
        panel.add(Box.createVerticalStrut(32));
        panel.add(brand);
        panel.add(Box.createVerticalStrut(10));
        panel.add(tagline);
        panel.add(Box.createVerticalStrut(55));

        JLabel heading = Theme.label(
                "Your conversations,",
                new Font("Segoe UI", Font.BOLD, 27), Theme.TEXT);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel heading2 = Theme.label(
                "closer than ever.",
                new Font("Segoe UI", Font.BOLD, 27), Theme.PURPLE);
        heading2.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel description = Theme.label(
                "<html>Connect with friends on your local network.<br>"
                + "Enjoy private conversations and group chats<br>"
                + "in one simple desktop application.</html>",
                Theme.NORMAL_FONT, Theme.MUTED);
        description.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(heading);
        panel.add(Box.createVerticalStrut(7));
        panel.add(heading2);
        panel.add(Box.createVerticalStrut(20));
        panel.add(description);
        panel.add(Box.createVerticalGlue());

        JLabel footer = Theme.label(
                "PRIVATE  •  SIMPLE  •  CONNECTED",
                Theme.SMALL_FONT, Theme.MUTED);
        footer.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(footer);

        return panel;
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Theme.BACKGROUND);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Theme.BACKGROUND);
        card.setBorder(new EmptyBorder(30, 35, 30, 35));
        card.setPreferredSize(new Dimension(390, 390));

        JLabel welcome = Theme.label(
                "Welcome to Linkora",
                Theme.TITLE_FONT, Theme.TEXT);

        JLabel subtitle = Theme.label(
                "Enter a username to get started.",
                Theme.NORMAL_FONT, Theme.MUTED);

        JLabel usernameLabel = Theme.label(
                "USERNAME", new Font("Segoe UI", Font.BOLD, 12),
                Theme.MUTED);

        usernameField = new JTextField();
        usernameField.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 48));
        Theme.styleTextField(usernameField);

        JButton joinButton = new JButton("Join Linkora  →");
        joinButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        joinButton.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 48));
        Theme.styleButton(joinButton);

        statusLabel = Theme.label(
                "Use 3–20 letters, numbers or underscores.",
                Theme.SMALL_FONT, Theme.MUTED);

        card.add(welcome);
        card.add(Box.createVerticalStrut(10));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(38));
        card.add(usernameLabel);
        card.add(Box.createVerticalStrut(10));
        card.add(usernameField);
        card.add(Box.createVerticalStrut(22));
        card.add(joinButton);
        card.add(Box.createVerticalStrut(14));
        card.add(statusLabel);

        panel.add(card);

        joinButton.addActionListener(e -> joinChat());
        usernameField.addActionListener(e -> joinChat());

        return panel;
    }

    private void joinChat() {
        String username = usernameField.getText().trim();

        if (!username.matches("[A-Za-z0-9_]{3,20}")) {
            statusLabel.setForeground(new Color(255, 105, 120));
            statusLabel.setText(
                    "Enter 3–20 letters, numbers or underscores.");
            return;
        }

        statusLabel.setForeground(Theme.GREEN);
        statusLabel.setText("Opening Linkora...");

        // The chat window will be implemented in the next file.
        ChatFrame chatFrame = new ChatFrame(username);
        chatFrame.setVisible(true);
        dispose();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(
                        UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // Use the default Swing look and feel.
            }

            new LoginFrame().setVisible(true);
        });
    }
}
