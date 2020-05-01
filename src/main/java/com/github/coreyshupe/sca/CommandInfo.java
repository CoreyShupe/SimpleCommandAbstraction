package com.github.coreyshupe.sca;

import lombok.Builder;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Builder @Getter public class CommandInfo {
    @NotNull private final JavaPlugin plugin;
    @NotNull private final String command;
    private final boolean consoleUsageAllowed;
    @Nullable @Builder.Default private final String permission = null;
    @Nullable @Builder.Default private final String usageConfigKey = null;
    @NotNull @Builder.Default private final List<String> aliases = new ArrayList<>();
    @Nullable @Builder.Default private final MessagesConfiguration messagesImplementation = null;
}