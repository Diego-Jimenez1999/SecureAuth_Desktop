package secureauth.ui.components.dashboard;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class DashboardCardConfig {
    private static final Path CONFIG_PATH = Path.of("dashboard_cards.config.txt");

    public static boolean isVisible(String cardId, boolean defaultValue) {
        if (!Files.exists(CONFIG_PATH)) {
            return defaultValue;
        }
        Properties props = new Properties();
        try (var reader = Files.newBufferedReader(CONFIG_PATH, StandardCharsets.UTF_8)) {
            props.load(reader);
            String val = props.getProperty("card." + cardId + ".visible");
            if (val != null) {
                return Boolean.parseBoolean(val.trim());
            }
        } catch (IOException ignored) {}
        return defaultValue;
    }

    public static String getTitle(String cardId, String defaultValue) {
        if (!Files.exists(CONFIG_PATH)) {
            return defaultValue;
        }
        Properties props = new Properties();
        try (var reader = Files.newBufferedReader(CONFIG_PATH, StandardCharsets.UTF_8)) {
            props.load(reader);
            String val = props.getProperty("card." + cardId + ".title");
            if (val != null && !val.trim().isEmpty()) {
                return val.trim();
            }
        } catch (IOException ignored) {}
        return defaultValue;
    }

    public static void saveConfig(String cardId, boolean visible, String title) {
        Properties props = new Properties();
        if (Files.exists(CONFIG_PATH)) {
            try (var reader = Files.newBufferedReader(CONFIG_PATH, StandardCharsets.UTF_8)) {
                props.load(reader);
            } catch (IOException ignored) {}
        }
        props.setProperty("card." + cardId + ".visible", String.valueOf(visible));
        props.setProperty("card." + cardId + ".title", title);
        try (var writer = Files.newBufferedWriter(CONFIG_PATH, StandardCharsets.UTF_8)) {
            props.store(writer, "Dashboard Cards Configuration");
        } catch (IOException ignored) {}
    }
}
