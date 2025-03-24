package nesoi.network.api;

import nesoi.network.api.model.Setting;
import nesoi.network.api.web.WebCreator;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.nandayo.DAPI.Util;

import java.io.IOException;

public final class NAPI {

    public final Plugin plugin;
    private final String version = "1.1.10";
    public static NAPI instance;

    private static WebCreator webCreator;

    public NAPI(@NotNull Plugin plugin, @NotNull Setting... settings) {
        instance = this;
        this.plugin = plugin;

        Util.PREFIX = "&8[<#fa8443>NAPI&8]&r ";
        Metrics metrics = new Metrics(plugin, 25222);
        metrics.addCustomChart(new SimplePie("NAPI Version", () -> version));

        try {
            webCreator = new WebCreator(plugin);

            for (Setting setting : settings) {
                Setting.getSettings().add(setting);
                Util.log("New setting added: " + setting.title + " (" + setting.type.toString().toLowerCase() + ")");
            }

            Util.log("Loaded " + Setting.getSettings().size() + " settings: " +
                    String.join(", ", Setting.getSettings().stream().map(s -> s.title).toList()));
        } catch (IOException e) {
            Util.log("&cError occurred while starting NAPI: " + e.getMessage());
        }


    }

    public void disable() {
        if (webCreator != null && webCreator.isAlive()) {
            Util.log("&6Web Creator shutting down...");
            webCreator.stop();
            webCreator = null;
        }
    }
}
