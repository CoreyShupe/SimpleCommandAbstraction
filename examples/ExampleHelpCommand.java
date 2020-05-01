import com.github.coreyshupe.sca.AbstractCommand;
import com.github.coreyshupe.sca.CommandInfo;
import com.github.coreyshupe.sca.MessagesConfiguration;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.logging.Level;

public class ExampleHelpCommand extends AbstractCommand {
    public ExampleHelpCommand(String helpKey, JavaPlugin plugin, MessagesConfiguration messagesConfiguration) {
        super(CommandInfo.builder()
                .plugin(plugin)
                .command("help")
                .aliases(Arrays.asList("?", "h"))
                .consoleUsageAllowed(false)
                .messagesImplementation(messagesConfiguration)
                .usageConfigKey(helpKey)
                .build());
    }

    @Override public void executeBoth(@NotNull CommandSender sender, String[] args) {
        getPlugin().getLogger().log(Level.INFO, "User " + sender.getName() + " ran example command.");
        String key = getUsageConfigKey();
        assert key != null;
        sendConfigMessage(sender, key, "Malformed help usage key.");
    }

    @Override public void executeConsole(@NotNull CommandSender sender, String[] args) {
        getPlugin().getLogger().log(Level.INFO, "Console execution for example command.");
    }

    @Override public void executePlayer(@NotNull Player player, String[] args) {
        getPlugin().getLogger().log(Level.INFO, "Player execution for example command.");
    }

    @Override public @NotNull String getNoPermissionMessage() {
        return getConfigMessage("no-permission", "&4You do not have permission for this command.");
    }

    @Override public @NotNull String getRefuseConsoleMessage() {
        return getConfigMessage("no-console", "&4Console does not have permission for this command.");
    }
}
