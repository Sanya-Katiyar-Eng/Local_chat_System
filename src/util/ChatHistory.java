
package util;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ChatHistory {

    private static final Path HISTORY_DIR =
            Paths.get("chat_history");

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static synchronized void saveMessage(
            String roomId, String username, String message) {

        if (roomId == null || !roomId.matches("[A-Z0-9]{8}")) {
            return;
        }

        try {
            Files.createDirectories(HISTORY_DIR);

            Path file = HISTORY_DIR.resolve(roomId + ".txt");

            String safeUsername = username
                    .replace("\n", " ")
                    .replace("\r", " ");

            String safeMessage = message
                    .replace("\n", " ")
                    .replace("\r", " ");

            try (BufferedWriter writer = Files.newBufferedWriter(
                    file,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND)) {

                writer.write(
                        "[" + LocalDateTime.now().format(FORMATTER)
                        + "] " + safeUsername + ": " + safeMessage);
                writer.newLine();
            }

        } catch (IOException e) {
            System.err.println("Could not save chat history: "
                    + e.getMessage());
        }
    }

    public static synchronized List<String> loadHistory(String roomId) {

        if (roomId == null || !roomId.matches("[A-Z0-9]{8}")) {
            return List.of();
        }

        Path file = HISTORY_DIR.resolve(roomId + ".txt");

        try {
            if (Files.exists(file)) {
                return Files.readAllLines(file, StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            System.err.println("Could not load chat history: "
                    + e.getMessage());
        }

        return List.of();
    }
}
