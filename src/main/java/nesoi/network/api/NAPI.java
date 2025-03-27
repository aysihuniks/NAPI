package nesoi.network.api;

import nesoi.network.api.model.Setting;
import nesoi.network.api.utils.VersionChecker;
import nesoi.network.api.web.WebCreator;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.nandayo.DAPI.Util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.nandayo.DAPI.HexUtil.parse;

public final class NAPI {

    private static NAPI napi;
    public final Plugin plugin;
    public final String version = "1.1.1";
    public static NAPI instance;

    private static WebCreator webCreator;
    private final Map<String, String> playerCodes = new HashMap<>();
    private final Map<String, Long> playerActivity = new HashMap<>();

    public NAPI(@NotNull Plugin plugin, @NotNull Setting... settings) {
        instance = this;
        this.plugin = plugin;

        Util.PREFIX = "&8[<#fa8443>NAPI&8]&r ";
        Metrics metrics = new Metrics(plugin, 25222);
        metrics.addCustomChart(new SimplePie("napi_version", () -> version));

        String latestVersion = VersionChecker.getVersion();
        if (!version.equals(latestVersion)) {
            Util.log("&c[NAPI] Plugin version (" + version + ") does not match latest release (" + (latestVersion != null ? latestVersion : "unknown") + "). Disabling NAPI.");
            NAPI.napi = null;
            NAPI.instance = null;
            return;
        }

        Util.log("&a[NAPI] Plugin version (" + version + ") is up-to-date with latest release (" + latestVersion + ").");

        for (Setting setting : settings) {
            Setting.getSettings().add(setting);
            Util.log("New setting added: " + setting.title + " (" + setting.type.toString().toLowerCase() + ")");
        }
        Util.log("Loaded " + Setting.getSettings().size() + " settings: " +
                String.join(", ", Setting.getSettings().stream().map(s -> s.title).toList()));

        NAPI.napi = this;

        new BukkitRunnable() {
            @Override
            public void run() {
                checkInactivePlayers();
            }
        }.runTaskTimerAsynchronously(plugin, 20 * 60, 20 * 60);
    }

    public void start(@NotNull CommandSender sender) {
        if (webCreator != null && webCreator.isAlive()) {
            if (sender instanceof Player player) {
                String code = playerCodes.get(player.getName());
                if (code == null) {
                    code = UUID.randomUUID().toString().substring(0, 8);
                    playerCodes.put(player.getName(), code);
                }
                String ip = plugin.getServer().getIp().isEmpty() ? "localhost" : plugin.getServer().getIp();
                tell(player, "You can access the site by entering https://" + ip + ":8080/" + code);
            } else {
                Util.log("&cThis command is only available to players at the moment.");
            }
            return;
        }

        try {
            webCreator = new WebCreator(plugin, sender, playerCodes, playerActivity);
        } catch (IOException e) {
            Util.log(" Error starting Web Creator " + e.getMessage());
            if (sender instanceof Player) {
                tell(sender, Util.PREFIX + "A error occurred while trying to starting website.");
            }
        }
    }

    private void checkInactivePlayers() {
        long currentTime = System.currentTimeMillis();
        playerActivity.entrySet().removeIf(entry -> {
            long lastActivity = entry.getValue();
            String playerName = entry.getKey();
            if (currentTime - lastActivity > 600_000) {
                Util.log("&eAfter 10 minutes of no change," + playerCodes + " was deleted.");
                playerCodes.remove(playerName);
                if (playerCodes.isEmpty() && webCreator != null && webCreator.isAlive()) {
                    Util.log("&eWeb Creator shutting down...");
                    webCreator.stop();
                    webCreator = null;
                }
                return true;
            }
            return false;
        });
    }

    public static NAPI getInst() {
        return instance;
    }

    public static NAPI getAPI() {
        return napi;
    }

    public void disable() {
        if (webCreator != null && webCreator.isAlive()) {
            Util.log("&eWeb Creator shutting down...");
            webCreator.stop();
            webCreator = null;
        }
        instance = null;
    }

    public void tell(@NotNull CommandSender receiver, @NotNull String... message) {
        for (String m : message) {
            receiver.sendMessage(parse(Util.PREFIX + m));
        }
    }
}