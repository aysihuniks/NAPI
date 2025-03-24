package nesoi.network.api;

import nesoi.network.api.model.Setting;
import nesoi.network.api.utils.VersionChecker;
import nesoi.network.api.web.WebCreator;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.nandayo.DAPI.Util;

import java.io.IOException;

public final class NAPI {

    private static NAPI napi;
    public final Plugin plugin;
    public final String version = "1.0.11";
    public static NAPI instance;

    private static WebCreator webCreator;

    public NAPI(@NotNull Plugin plugin, @NotNull Setting... settings) {
        this.plugin = plugin;

        // BSTATS & DAPI
        Util.PREFIX = "&8[<#fa8443>NAPI&8]&r ";
        Metrics metrics = new Metrics(plugin, 25222);
        metrics.addCustomChart(new SimplePie("napi_version", () -> version));

        // Version Checker
        String latestVersion = VersionChecker.getVersion();
        if (!version.equals(latestVersion)) {
            Util.log("&cVersion (" + version + ") does not match latest release (" + (latestVersion != null ? latestVersion : "Unknown") + "). Disabling NAPI.", "&2https://github.com/aysihuniks/NAPI/releases");
            NAPI.napi = null;
            NAPI.instance = null;
            return;
        }

        try {
            webCreator = new WebCreator(plugin);

            for (Setting setting : settings) {
                Setting.getSettings().add(setting);
                Util.log("&aNew setting added: " + setting.title + " (" + setting.type.toString().toLowerCase() + ")");
            }

            Util.log("&aLoaded " + Setting.getSettings().size() + " settings: " +
                    String.join(", ", Setting.getSettings().stream().map(s -> s.title).toList()));
        } catch (IOException e) {
            Util.log("&cError occurred while starting NAPI: " + e.getMessage());
        }

        NAPI.napi = this;
        NAPI.instance = this;
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
    }
}
