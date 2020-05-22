package com.github.coreyshupe.sca;

import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("unused") @Getter public abstract class AbstractCommand implements CommandExecutor, TabCompleter {
    @NotNull private final Map<String, AbstractCommand> children;
    @NotNull private final JavaPlugin plugin;
    @NotNull private final String command;
    private final boolean consoleUsageAllowed;
    @Nullable private final String permission;
    @Nullable private final String usageConfigKey;
    @NotNull private final Set<String> aliases;
    @Nullable private final MessagesConfiguration messagesConfiguration;

    public AbstractCommand(@NotNull CommandInfo info) {
        this.children = new HashMap<>();
        this.plugin = info.getPlugin();
        this.command = info.getCommand();
        this.consoleUsageAllowed = info.isConsoleUsageAllowed();
        this.permission = info.getPermission();
        this.usageConfigKey = info.getUsageConfigKey();
        this.aliases = info.getAliases().parallelStream().map(String::toLowerCase).collect(Collectors.toSet());
        this.messagesConfiguration = info.getMessagesImplementation();
    }

    @Nullable public abstract String getNoPermissionMessage();

    @Nullable public abstract String getRefuseConsoleMessage();

    @NotNull public String[] subArr(@NotNull String[] current) {
        String[] n = new String[current.length - 1];
        System.arraycopy(current, 1, n, 0, n.length);
        return n;
    }

    @NotNull private Optional<AbstractCommand> findNext(@NotNull String command) {
        String x = command.toLowerCase();
        AbstractCommand fromMap = children.get(x);
        if (fromMap == null) {
            return children.values().stream().filter(c -> c.aliases.contains(x)).findFirst();
        } else {
            return Optional.of(fromMap);
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        run(commandSender, strings);
        return true;
    }

    private void run(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player) && !consoleUsageAllowed) {
            String message = getRefuseConsoleMessage();
            if (message != null) sender.sendMessage(message);
        } else if (children.size() == 0 || args.length == 0) {
            run0(sender, args);
        } else {
            Optional<AbstractCommand> optionalChild = findNext(args[0]);
            if (optionalChild.isPresent()) {
                optionalChild.get().run(sender, subArr(args));
            } else {
                run0(sender, args);
            }
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return getTabCompletion0(sender, args);
    }

    private void run0(@NotNull CommandSender sender, @NotNull String[] args) {
        if (permission != null && !sender.hasPermission(permission)) {
            String message = getNoPermissionMessage();
            if (message != null) sender.sendMessage(message);
        } else {
            executeBoth(sender, args);
            if (sender instanceof Player) {
                executePlayer((Player) sender, args);
            } else {
                executeConsole(sender, args);
            }
        }
    }

    @NotNull private List<String> getTabCompletion0(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            return new ArrayList<>();
        } else if (children.size() == 0 || args.length <= 1) {
            return getTabCompletion(sender, args);
        } else {
            Optional<AbstractCommand> optionalChild = findNext(args[0]);
            return optionalChild.map(abstractCommand -> abstractCommand.getTabCompletion0(sender, subArr(args))).orElseGet(() -> getTabCompletion(sender, args));
        }
    }

    // util
    public void child(@NotNull AbstractCommand command) {
        children.put(command.getCommand(), command);
    }

    public void sendUsage(@NotNull CommandSender sender) {
        if (usageConfigKey != null) {
            sendConfigMessage(sender, usageConfigKey);
        }
    }

    public void sendConfigMessage(@NotNull CommandSender sender, @NotNull String key) {
        String message = getConfigMessage(key);
        if (message != null) sender.sendMessage(message);
    }

    public void sendConfigMessage(@NotNull CommandSender sender, @NotNull String key, @NotNull String def) {
        sender.sendMessage(getConfigMessage(key, def));
    }

    @Nullable public String getConfigMessage(@NotNull String key) {
        if (messagesConfiguration == null) return null;
        else return messagesConfiguration.getMessage(key);
    }

    @NotNull public String getConfigMessage(@NotNull String key, @NotNull String def) {
        if (messagesConfiguration == null) return ChatColor.translateAlternateColorCodes('&', def);
        else return messagesConfiguration.getMessage(key, def);
    }

    // Leave empty
    public void executePlayer(@NotNull Player player, String[] args) {
    }

    public void executeConsole(@NotNull CommandSender sender, String[] args) {
    }

    public void executeBoth(@NotNull CommandSender sender, String[] args) {
    }

    public List<String> getTabCompletion(@NotNull CommandSender sender, String[] args) {
        if (args.length != 1) return new ArrayList<>();
        return getChildren()
                .entrySet()
                .stream()
                .filter(e -> (e.getValue().getPermission() == null || sender.hasPermission(e.getValue().getPermission())) &&
                        e.getKey().startsWith(args[0].toLowerCase()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}
