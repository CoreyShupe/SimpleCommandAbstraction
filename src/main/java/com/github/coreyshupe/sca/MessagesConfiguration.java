package com.github.coreyshupe.sca;

import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.Objects;
import java.util.logging.Level;

public class MessagesConfiguration {
    @Nullable @Getter private FileConfiguration fileConfiguration;

    public MessagesConfiguration(@NotNull JavaPlugin plugin) {
        this(plugin, false);
    }

    public MessagesConfiguration(@NotNull JavaPlugin plugin, boolean loadLater) {
        if (!loadLater) this.fileConfiguration = retrieveResourceSafely(plugin);
    }

    @NotNull public String getMessage(@NotNull String key) {
        return getMessage(key, "Failed to find " + key + ".");
    }

    @NotNull public String getMessage(@NotNull String key, @NotNull String def) {
        return ChatColor.translateAlternateColorCodes('&', getMessageRaw(key, def));
    }

    @NotNull public String getMessageRaw(@NotNull String key) {
        return getMessageRaw(key, "Failed to find " + key + ".");
    }

    @NotNull public String getMessageRaw(@NotNull String key, @NotNull String def) {
        if (fileConfiguration == null) return "Unloaded call to " + key + ".";
        return Objects.requireNonNull(fileConfiguration.getString(key, def));
    }

    public void reload(JavaPlugin plugin) {
        fileConfiguration = retrieveResourceSafely(plugin);
    }

    @NotNull
    private FileConfiguration retrieveResourceSafely(@NotNull JavaPlugin plugin) {
        String resourcePath = "messages.yml";

        resourcePath = resourcePath.replace('\\', '/');
        InputStream in = plugin.getResource(resourcePath);
        File outFile = new File(plugin.getDataFolder(), resourcePath);
        if (in == null) {
            if (outFile.exists()) return YamlConfiguration.loadConfiguration(outFile);
            else return new YamlConfiguration();
        }

        File outDir = plugin.getDataFolder();

        if (!outDir.exists() && !outDir.mkdirs()) {
            throw new IllegalStateException("Failed to create data folder.");
        }

        try {
            if (!outFile.exists()) {
                OutputStream out = new FileOutputStream(outFile);
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                out.close();
                in.close();
            }
            return YamlConfiguration.loadConfiguration(outFile);
        } catch (IOException ex) {
            plugin.getLogger().log(Level.SEVERE, "Could not save " + outFile.getName() + " to " + outFile, ex);
            throw new IllegalStateException("Failed to write yaml configuration.", ex);
        }
    }
}
