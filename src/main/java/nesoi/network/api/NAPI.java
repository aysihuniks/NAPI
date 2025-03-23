package nesoi.network.api;

import nesoi.network.api.model.Setting;
import nesoi.network.api.enumeration.SettingType;
import nesoi.network.api.web.WebCreator;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.nandayo.DAPI.Util;

import java.io.IOException;

public final class NAPI {

    public final Plugin plugin;
    public static NAPI instance;

    private static WebCreator webCreator;

    public NAPI(Plugin plugin, Setting... settings) {
        instance = this;
        this.plugin = plugin;

        if (plugin != null) {
            Util.PREFIX = "&8[<#fa8443>NAPI&8]&r ";

            try {
                webCreator = new WebCreator(plugin);

                for (Setting setting : settings) {
                    Setting.add(setting.title, setting.description, setting.type,
                            setting.type == SettingType.INPUT ? setting.placeholder : setting.value);
                }

                Util.log("Loaded " + Setting.getSettings().size() + " settings: " +
                        String.join(", ", Setting.getSettings().stream().map(s -> s.title).toList()));
            } catch (IOException e) {
                Util.log("&cError occurred while starting NAPI: " + e.getMessage());
            }
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
