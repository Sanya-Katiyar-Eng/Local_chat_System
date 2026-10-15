
package gui;

import client.ChatClient;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import javax.swing.*;
import javax.swing.border.*;

public class ChatFrame extends JFrame {

    private final String username;

    private JPanel messagePanel;
    private JScrollPane messageScroll;
    private JTextField messageField;

    private JLabel conversationTitle;
    private JLabel conversationSubtitle;
    private JLabel connectionStatus;
    private JLabel typingLabel;

    private Timer typingTimer;

    private ChatClient client;
    private String currentRoomId = null;

    private final Map<String, JPanel> messageRows = new HashMap<>();

    private JPanel conversationPanel;
    private JPanel homePanel;

    public ChatFrame(String username) {
        this.username = username;

        setTitle("Linkora | " + username);
        setSize(1200, 760);
        setMinimumSize(new Dimension(900, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.BACKGROUND);

        root.add(createSidebar(), BorderLayout.WEST);
        root.add(createConversationPanel(), BorderLayout.CENTER);

        setContentPane(root);

        connectToServer();
    }

    // ================= SIDEBAR =================

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(270, 0));
        sidebar.setBackground(Theme.SIDEBAR);
        sidebar.setBorder(new MatteBorder(0, 0, 0, 1, Theme.BORDER));

        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setBackground(Theme.SIDEBAR);
        top.setBorder(new EmptyBorder(24, 20, 18, 20));

        JLabel brand = Theme.label(
                "◆  Linkora",
                new Font("Segoe UI", Font.BOLD, 25),
                Theme.TEXT);

        JLabel subtitle = Theme.label(
                "Private conversations",
                Theme.SMALL_FONT,
                Theme.MUTED);

        top.add(brand);
        top.add(Box.createVerticalStrut(5));
        top.add(subtitle);
        top.add(Box.createVerticalStrut(30));

        JLabel chatsLabel = Theme.label(
                "  PRIVATE CHATS",
                new Font("Segoe UI", Font.BOLD, 11),
                Theme.MUTED);

        chatsLabel.setBorder(new EmptyBorder(0, 0, 12, 0));
        top.add(chatsLabel);

        JButton createChat = sidebarButton("+   Create New Chat");
        JButton joinChat = sidebarButton("   Join Existing Chat");
        JButton leaveChat = sidebarButton("   Leave Current Chat");

        createChat.addActionListener(e -> createRoomDialog());
        joinChat.addActionListener(e -> joinRoomDialog());
        leaveChat.addActionListener(e -> leaveCurrentRoom());

        top.add(createChat);
        top.add(Box.createVerticalStrut(8));
        top.add(joinChat);
        top.add(Box.createVerticalStrut(8));
        top.add(leaveChat);

        sidebar.add(top, BorderLayout.NORTH);

        JPanel profile = new JPanel(new BorderLayout(10, 0));
        profile.setBackground(Theme.PANEL);
        profile.setBorder(new EmptyBorder(15, 16, 15, 12));

        String initial = username.isEmpty()
                ? "?"
                : username.substring(0, 1).toUpperCase();

        JLabel avatar = new JLabel(initial, SwingConstants.CENTER);
        avatar.setFont(new Font("Segoe UI", Font.BOLD, 16));
        avatar.setForeground(Color.WHITE);
        avatar.setOpaque(true);
        avatar.setBackground(Theme.PURPLE);
        avatar.setPreferredSize(new Dimension(42, 42));

        JPanel details = new JPanel();
        details.setOpaque(false);
        details.setLayout(new BoxLayout(details, BoxLayout.Y_AXIS));

        JLabel name = Theme.label(username, Theme.NORMAL_FONT, Theme.TEXT);
        JLabel status = Theme.label(
                "Linkora user", Theme.SMALL_FONT, Theme.MUTED);

        details.add(name);
        details.add(Box.createVerticalStrut(5));
        details.add(status);

        JButton logout = new JButton("Logout");
        logout.setForeground(Theme.TEXT);
        logout.setBackground(Theme.INPUT);
        logout.setFocusPainted(false);
        logout.setBorder(new EmptyBorder(9, 10, 9, 10));
        logout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        logout.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(
                    this,
                    "Do you want to log out of Linkora?",
                    "Confirm Logout",
                    JOptionPane.YES_NO_OPTION);

            if (choice == JOptionPane.YES_OPTION) {
                if (client != null) {
                    client.disconnect();
                }

                dispose();
                new LoginFrame().setVisible(true);
            }
        });

        profile.add(avatar, BorderLayout.WEST);
        profile.add(details, BorderLayout.CENTER);
        profile.add(logout, BorderLayout.EAST);

        sidebar.add(profile, BorderLayout.SOUTH);

        return sidebar;
    }

    private JButton sidebarButton(String text) {
        JButton button = new JButton(text);

        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setFont(Theme.NORMAL_FONT);
        button.setForeground(Theme.TEXT);
        button.setBackground(Theme.SIDEBAR);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(false);
        button.setBorder(new EmptyBorder(12, 8, 12, 8));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(Theme.PANEL);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(Theme.SIDEBAR);
            }
        });

        return button;
    }

    // ================= CONVERSATION PANEL =================

    private JPanel createConversationPanel() {
        conversationPanel = new JPanel(new BorderLayout());
        conversationPanel.setBackground(Theme.BACKGROUND);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.BACKGROUND);
        header.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, Theme.BORDER),
                new EmptyBorder(20, 25, 20, 25)));

        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));

        conversationTitle = Theme.label(
                "Welcome to Linkora",
                Theme.HEADING_FONT,
                Theme.TEXT);

        conversationSubtitle = Theme.label(
                "Create a room or join one using its credentials.",
                Theme.SMALL_FONT,
                Theme.MUTED);

        typingLabel = Theme.label("", Theme.SMALL_FONT, Theme.PURPLE);

        heading.add(conversationTitle);
        heading.add(Box.createVerticalStrut(6));
        heading.add(conversationSubtitle);
        heading.add(Box.createVerticalStrut(4));
        heading.add(typingLabel);

        connectionStatus = Theme.label(
                "Connecting...",
                Theme.SMALL_FONT,
                Theme.MUTED);

        header.add(heading, BorderLayout.WEST);
        header.add(connectionStatus, BorderLayout.EAST);

        // Message panel
        messagePanel = new JPanel();
        messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.Y_AXIS));
        messagePanel.setBackground(Theme.BACKGROUND);
        messagePanel.setBorder(new EmptyBorder(20, 24, 20, 24));

        messageScroll = new JScrollPane(messagePanel);
        messageScroll.setBorder(null);
        messageScroll.getViewport().setBackground(Theme.BACKGROUND);
        messageScroll.getVerticalScrollBar().setUnitIncrement(16);

        JLabel empty = Theme.label(
                "Welcome, " + username
                        + "! Create a new private chat to begin.",
                Theme.NORMAL_FONT,
                Theme.MUTED);

        empty.setBorder(new EmptyBorder(15, 5, 15, 5));
        empty.setAlignmentX(Component.LEFT_ALIGNMENT);
        messagePanel.add(empty);

        // Composer
        JPanel composer = new JPanel(new BorderLayout(8, 0));
        composer.setBackground(Theme.BACKGROUND);
        composer.setBorder(new EmptyBorder(15, 22, 22, 22));

        // Message input field
        messageField = new JTextField();
        messageField.setFont(Theme.NORMAL_FONT);
        messageField.setForeground(Theme.TEXT);
        messageField.setBackground(Theme.INPUT);
        messageField.setCaretColor(Theme.TEXT);
        messageField.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER, 1, true),
                new EmptyBorder(14, 15, 14, 15)));

        messageField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (client != null && currentRoomId != null) {
                    client.sendTypingStatus();
                }
            }
        });

        messageField.setEnabled(false);
        messageField.setToolTipText(
                "Create or join a room before sending messages");

        // Emoji picker button
        JButton emojiButton = new JButton("☺");
        emojiButton.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
        emojiButton.setForeground(Theme.TEXT);
        emojiButton.setBackground(Theme.INPUT);
        emojiButton.setFocusPainted(false);
        emojiButton.setBorder(new EmptyBorder(8, 12, 8, 12));
        emojiButton.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        emojiButton.setEnabled(false);
        emojiButton.setToolTipText("Choose an emoji");

        // Send button
        JButton send = new JButton("Send  >");
        Theme.styleButton(send);
        send.setEnabled(false);

        // Button actions
        emojiButton.addActionListener(e -> showEmojiPicker());
        send.addActionListener(e -> sendMessage());
        messageField.addActionListener(e -> sendMessage());

        // Save button references
        messageField.putClientProperty("sendButton", send);
        messageField.putClientProperty("emojiButton", emojiButton);

        // Input field
        JPanel inputArea = new JPanel(new BorderLayout(8, 0));
        inputArea.setOpaque(false);
        inputArea.add(messageField, BorderLayout.CENTER);

        // Emoji and Send buttons
        JPanel rightButtons = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightButtons.setOpaque(false);
        rightButtons.add(emojiButton);
        rightButtons.add(send);

        composer.add(inputArea, BorderLayout.CENTER);
        composer.add(rightButtons, BorderLayout.EAST);

        // Home screen
        homePanel = createHomePanel();

        conversationPanel.add(header, BorderLayout.NORTH);
        conversationPanel.add(homePanel, BorderLayout.CENTER);
        conversationPanel.add(composer, BorderLayout.SOUTH);

        return conversationPanel;
    }


















    // ================= EMOJI PICKER =================

    
    private void showEmojiPicker() {
        if (messageField == null || !messageField.isEnabled()) {
            return;
    }

        JPopupMenu popup = new JPopupMenu();
        popup.setBorder(new LineBorder(Theme.BORDER, 1, true));

        JPanel emojiPanel = new JPanel(new GridLayout(0, 6, 4, 4));
        emojiPanel.setBackground(Theme.PANEL);
        emojiPanel.setBorder(new EmptyBorder(8, 8, 8, 8));

        String[] emojis = {
        "\u263A", "\u2665", "\u2605", "\u2600", "\u2602", "\u2601",
        "\u263A", "\u266B", "\u2713", "\u2714", "\u2191", "\u2193",
        "\uD83D\uDE00", "\uD83D\uDE02", "\uD83D\uDE0A",
        "\uD83D\uDE0D", "\uD83D\uDE22", "\uD83D\uDE21",
        "\uD83D\uDC4D", "\uD83D\uDC4E", "\uD83D\uDC4F",
        "\uD83D\uDE4F", "\u2764", "\uD83D\uDD25",
        "\uD83C\uDF89", "\uD83C\uDF82", "\uD83C\uDF39",
        "\uD83C\uDF38", "\uD83D\uDC36", "\uD83D\uDC31"
    };

        Font emojiFont = new Font("Segoe UI Emoji", Font.PLAIN, 22);

        for (String emoji : emojis) {
            JButton button = new JButton(emoji);
            button.setFont(emojiFont);
            button.setPreferredSize(new Dimension(42, 40));
            button.setForeground(Theme.TEXT);
            button.setBackground(Theme.PANEL);
            button.setFocusPainted(false);
            button.setBorderPainted(false);
            button.setContentAreaFilled(true);
            button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            button.addActionListener(e -> {
                int position = messageField.getCaretPosition();
                String oldText = messageField.getText();

                String newText = oldText.substring(0, position)
                    + emoji
                    + oldText.substring(position);

                messageField.setText(newText);
                messageField.setCaretPosition(
                    Math.min(position + emoji.length(), newText.length()));
                messageField.requestFocusInWindow();

                popup.setVisible(false);
        }); 

            emojiPanel.add(button);
    }

        popup.add(emojiPanel);
        popup.pack();

        Object emojiObject = messageField.getClientProperty("emojiButton");

        if (emojiObject instanceof JButton) {
            JButton anchor = (JButton) emojiObject;
            popup.show(anchor, 0, -popup.getPreferredSize().height - 5);
    }
}























    // ================= HOME SCREEN =================

    private JPanel createHomePanel() {
        JPanel home = new JPanel() {
            private float glow = 0;

            {
                setLayout(new GridBagLayout());
                setOpaque(false);

                Timer animation = new Timer(45, e -> {
                    glow += 0.025f;

                    if (glow > 1) {
                        glow = 0;
                    }

                    repaint();
                });

                animation.start();
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                Graphics2D g2 = (Graphics2D) g.create();

                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                g2.setPaint(new GradientPaint(
                        0, 0, new Color(18, 22, 48),
                        w, h, new Color(35, 22, 70)));

                g2.fillRect(0, 0, w, h);

                int size = 220;
                int x = (int) (
                        w * 0.68 + Math.sin(glow * 6.28) * 35);
                int y = (int) (
                        h * 0.24 + Math.cos(glow * 6.28) * 25);

                g2.setColor(new Color(100, 70, 220, 28));
                g2.fillOval(
                        x - size / 2,
                        y - size / 2,
                        size,
                        size);

                g2.setColor(new Color(40, 150, 230, 20));
                g2.fillOval(25, h - 190, 180, 180);

                g2.dispose();
            }
        };

        home.setBorder(new EmptyBorder(25, 25, 25, 25));

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JLabel logo = new JLabel("L", SwingConstants.CENTER);
        logo.setFont(new Font("Segoe UI", Font.BOLD, 48));
        logo.setForeground(Color.WHITE);
        logo.setOpaque(true);
        logo.setBackground(Theme.PURPLE);
        logo.setPreferredSize(new Dimension(100, 100));
        logo.setMaximumSize(new Dimension(100, 100));
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        logo.setBorder(
                new LineBorder(new Color(150, 130, 255), 2, true));

        JLabel title = new JLabel("Linkora");
        title.setFont(new Font("Segoe UI", Font.BOLD, 38));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel tagline = new JLabel(
                "Your Space. Your People. Your Conversations.");
        tagline.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tagline.setForeground(new Color(195, 195, 230));
        tagline.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel status = new JLabel("●  Ready to connect");
        status.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        status.setForeground(new Color(100, 230, 175));
        status.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton createButton = new JButton("+   Create a Chat");
        JButton joinButton = new JButton("↗   Join a Chat");

        styleHomeButton(createButton, true);
        styleHomeButton(joinButton, false);

        createButton.addActionListener(e -> createRoomDialog());
        joinButton.addActionListener(e -> joinRoomDialog());

        content.add(logo);
        content.add(Box.createVerticalStrut(18));
        content.add(title);
        content.add(Box.createVerticalStrut(8));
        content.add(tagline);
        content.add(Box.createVerticalStrut(18));
        content.add(status);
        content.add(Box.createVerticalStrut(32));
        content.add(createButton);
        content.add(Box.createVerticalStrut(12));
        content.add(joinButton);
        content.add(Box.createVerticalStrut(20));

        JLabel footer = new JLabel("PRIVATE  •  SIMPLE  •  CONNECTED");
        footer.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        footer.setForeground(new Color(150, 155, 190));
        footer.setAlignmentX(Component.CENTER_ALIGNMENT);

        content.add(footer);
        home.add(content);

        return home;
    }

    private void styleHomeButton(JButton button, boolean primary) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 15));
        button.setForeground(Color.WHITE);

        button.setBackground(primary
                ? new Color(100, 75, 220)
                : new Color(37, 43, 72));

        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(15, 25, 15, 25));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(290, 52));
        button.setPreferredSize(new Dimension(290, 52));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(primary
                        ? new Color(125, 100, 245)
                        : new Color(55, 65, 100));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(primary
                        ? new Color(100, 75, 220)
                        : new Color(37, 43, 72));
            }
        });
    }

    private void showHomeScreen() {
        conversationPanel.remove(messageScroll);

        if (homePanel == null) {
            homePanel = createHomePanel();
        }

        conversationPanel.add(homePanel, BorderLayout.CENTER);
        conversationPanel.revalidate();
        conversationPanel.repaint();

        messageField.setText("");
        typingLabel.setText("");
    }

    private void showConversationScreen() {
        if (homePanel != null) {
            conversationPanel.remove(homePanel);
        }

        conversationPanel.add(messageScroll, BorderLayout.CENTER);
        conversationPanel.revalidate();
        conversationPanel.repaint();
    }

    // ================= SERVER CONNECTION =================

    private void connectToServer() {
        client = new ChatClient();

        try {
            client.connect(
                    username,
                    response -> SwingUtilities.invokeLater(
                            () -> handleServerMessage(response)));

            connectionStatus.setText("Connected");
            connectionStatus.setForeground(Theme.GREEN);

        } catch (IOException e) {
            connectionStatus.setText("Server offline");
            connectionStatus.setForeground(
                    new Color(255, 105, 120));

            JOptionPane.showMessageDialog(
                    this,
                    "Could not connect to Linkora server.\n"
                            + "Please start the server first.\n\n"
                            + e.getMessage(),
                    "Connection Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // ================= CREATE ROOM =================

    private void createRoomDialog() {
        if (client == null) {
            return;
        }

        client.createRoom();

        conversationTitle.setText("Creating private chat...");
        conversationSubtitle.setText(
                "Generating your Room ID and password.");
    }

    // ================= JOIN ROOM =================

    private void joinRoomDialog() {
        if (client == null) {
            return;
        }

        JTextField roomIdField = new JTextField();
        JPasswordField passwordField = new JPasswordField();

        JPanel form = new JPanel(new GridLayout(0, 1, 6, 6));
        form.add(new JLabel("Room ID:"));
        form.add(roomIdField);
        form.add(new JLabel("Room Password:"));
        form.add(passwordField);

        int result = JOptionPane.showConfirmDialog(
                this,
                form,
                "Join Existing Chat",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String roomId = roomIdField.getText().trim();
            String password = new String(passwordField.getPassword());

            if (roomId.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(
                        this,
                        "Please enter both Room ID and password.");
                return;
            }

            client.joinRoom(roomId, password);
            conversationSubtitle.setText(
                    "Checking Room ID and password...");
        }
    }

    // ================= LEAVE ROOM =================

    private void leaveCurrentRoom() {
        if (client == null || currentRoomId == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "You are not in a chat room.");
            return;
        }

        client.leaveRoom();
        currentRoomId = null;

        setComposerEnabled(false);

        conversationTitle.setText("Welcome to Linkora");
        conversationSubtitle.setText(
                "Create a room or join an existing room.");

        clearMessages();
        showHomeScreen();
    }

    // ================= SERVER MESSAGES =================

    private void handleServerMessage(String response) {

        if (response.startsWith("ROOM_CREATED|")) {
            String[] parts = response.split("\\|", 3);

            if (parts.length == 3) {
                currentRoomId = parts[1];

                showConversationScreen();

                conversationTitle.setText(
                        "Room " + currentRoomId);

                conversationSubtitle.setText(
                        "Private room • Share credentials with your friend");

                setComposerEnabled(true);
                clearMessages();

                addMessageBubble(
                        "Linkora",
                        "Your private room has been created.",
                        false);

                showRoomDetails(parts[1], parts[2]);
            }

        } else if (response.startsWith("ROOM_JOINED|")) {
            String[] parts = response.split("\\|", 2);

            if (parts.length == 2) {
                currentRoomId = parts[1];

                showConversationScreen();

                conversationTitle.setText(
                        "Room " + currentRoomId);

                conversationSubtitle.setText(
                        "You are connected to this private room.");

                setComposerEnabled(true);
                clearMessages();

                addMessageBubble(
                        "Linkora",
                        "You joined the private room successfully.",
                        false);
            }

        } else if (response.startsWith("TYPING|")) {
            String[] parts = response.split("\\|", 2);

            if (parts.length == 2 && !parts[1].equals(username)) {
                typingLabel.setText(parts[1] + " is typing...");

                if (typingTimer != null) {
                    typingTimer.stop();
                }

                typingTimer = new Timer(
                        2000,
                        e -> typingLabel.setText(""));

                typingTimer.setRepeats(false);
                typingTimer.start();
            }

        } else if (response.startsWith("ROOM_MESSAGE|")) {
            String[] parts = response.split("\\|", 5);

            if (parts.length == 5) {
                addMessageBubble(
                        parts[2],
                        parts[4],
                        parts[2].equals(username),
                        parts[1]);
            }

        } else if (response.startsWith("MESSAGE_DELETED|")) {
            String[] parts = response.split("\\|", 2);

            if (parts.length == 2) {
                removeMessageBubble(parts[1]);
            }

        } else if (response.startsWith("SYSTEM|")) {
            String text = response.substring("SYSTEM|".length());
            addMessageBubble("Linkora", text, false);

        } else if (response.startsWith("ERROR|")) {
            JOptionPane.showMessageDialog(
                    this,
                    response.substring("ERROR|".length()),
                    "Linkora Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // ================= ROOM DETAILS =================

    private void showRoomDetails(String roomId, String password) {
        String details = "Linkora Private Chat\n\n"
                + "Room ID: " + roomId + "\n"
                + "Password: " + password + "\n\n"
                + "Share these details with your friend.\n"
                + "Keep the password private.";

        JTextArea textArea = new JTextArea(details, 9, 35);
        textArea.setEditable(false);
        textArea.setFont(new Font("Consolas", Font.PLAIN, 14));
        textArea.setBackground(Theme.BACKGROUND);
        textArea.setForeground(Theme.TEXT);
        textArea.setBorder(new EmptyBorder(10, 10, 10, 10));

        int choice = JOptionPane.showOptionDialog(
                this,
                new JScrollPane(textArea),
                "Your Private Chat Is Ready",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                new String[]{"Copy Details", "Done"},
                "Copy Details");

        if (choice == 0) {
            try {
                StringSelection selection =
                        new StringSelection(details);

                Toolkit.getDefaultToolkit()
                        .getSystemClipboard()
                        .setContents(selection, null);

                JOptionPane.showMessageDialog(
                        this,
                        "Room details copied. You can share them privately.");

            } catch (Exception e) {
                JOptionPane.showMessageDialog(
                        this,
                        "Please copy the Room ID and password manually.");
            }
        }
    }

    // ================= ENABLE / DISABLE COMPOSER =================

    private void setComposerEnabled(boolean enabled) {
        messageField.setEnabled(enabled);

        Object buttonObject =
                messageField.getClientProperty("sendButton");

        if (buttonObject instanceof JButton) {
            ((JButton) buttonObject).setEnabled(enabled);
        }

        Object emojiObject =
                messageField.getClientProperty("emojiButton");

        if (emojiObject instanceof JButton) {
            ((JButton) emojiObject).setEnabled(enabled);
        }
    }

    // ================= CLEAR MESSAGES =================

    private void clearMessages() {
        messagePanel.removeAll();
        messageRows.clear();

        messagePanel.revalidate();
        messagePanel.repaint();
    }

    // ================= SEND MESSAGE =================

    private void sendMessage() {
        String text = messageField.getText().trim();

        if (text.isEmpty() || client == null) {
            return;
        }

        if (currentRoomId == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please create or join a room first.");
            return;
        }

        client.sendRoomMessage(text);

        messageField.setText("");
        messageField.requestFocusInWindow();
    }

    // ================= MESSAGE BUBBLES =================

    private void addMessageBubble(
            String sender,
            String text,
            boolean ownMessage) {

        addMessageBubble(sender, text, ownMessage, null);
    }

    private void addMessageBubble(
            String sender,
            String text,
            boolean ownMessage,
            String messageId) {

        JPanel row = new JPanel(new FlowLayout(
                ownMessage ? FlowLayout.RIGHT : FlowLayout.LEFT,
                10,
                5));

        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2000));

        JPanel bubble = new JPanel();
        bubble.setLayout(new BoxLayout(bubble, BoxLayout.Y_AXIS));
        bubble.setBackground(
                ownMessage ? Theme.PURPLE : Theme.PANEL);

        bubble.setBorder(new EmptyBorder(10, 14, 9, 14));

        JLabel senderLabel = Theme.label(
                sender,
                Theme.SMALL_FONT,
                ownMessage ? Color.WHITE : Theme.TEXT);

        senderLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextArea body = new JTextArea(text);
        body.setFont(Theme.NORMAL_FONT);
        body.setForeground(Color.WHITE);
        body.setBackground(
                ownMessage ? Theme.PURPLE : Theme.PANEL);

        body.setLineWrap(true);
        body.setWrapStyleWord(true);
        body.setEditable(false);
        body.setFocusable(false);
        body.setColumns(
                Math.min(32, Math.max(12, text.length())));

        body.setBorder(new EmptyBorder(6, 0, 6, 0));
        body.setAlignmentX(Component.LEFT_ALIGNMENT);

        String time = LocalTime.now().format(
                DateTimeFormatter.ofPattern("HH:mm"));

        JLabel timeLabel = Theme.label(
                time,
                Theme.SMALL_FONT,
                new Color(220, 218, 242));

        timeLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);

        bubble.add(senderLabel);
        bubble.add(body);
        bubble.add(timeLabel);

        // Delete button for your own messages
        if (ownMessage && messageId != null) {
            JButton deleteButton = new JButton("Delete");

            deleteButton.setFont(Theme.SMALL_FONT);
            deleteButton.setFocusPainted(false);
            deleteButton.setCursor(
                    Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            deleteButton.addActionListener(e -> {
                int choice = JOptionPane.showConfirmDialog(
                        this,
                        "Delete this message for everyone in this room?",
                        "Delete Message",
                        JOptionPane.YES_NO_OPTION);

                if (choice == JOptionPane.YES_OPTION
                        && client != null) {
                    client.deleteRoomMessage(messageId);
                }
            });

            deleteButton.setAlignmentX(Component.RIGHT_ALIGNMENT);

            bubble.add(Box.createVerticalStrut(5));
            bubble.add(deleteButton);
        }

        row.add(bubble);
        messagePanel.add(row);

        if (messageId != null) {
            messageRows.put(messageId, row);
        }

        messagePanel.revalidate();
        messagePanel.repaint();

        SwingUtilities.invokeLater(() ->
                messageScroll.getVerticalScrollBar().setValue(
                        messageScroll.getVerticalScrollBar().getMaximum()));
    }

    // ================= DELETE MESSAGE FROM UI =================

    private void removeMessageBubble(String messageId) {
        JPanel row = messageRows.remove(messageId);

        if (row != null) {
            messagePanel.remove(row);
            messagePanel.revalidate();
            messagePanel.repaint();
        }
    }
}
