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

    public NAPI(Plugin plugin) {
        instance = this;
        this.plugin = plugin;

        if (plugin != null) {
            Util.PREFIX = "&8[<#fa8443>NAPI&8]&r ";

            try {
                webCreator = new WebCreator(plugin);
                Util.log("Web Creator started on https://" + Bukkit.getIp() + ":8080/");
            } catch (IOException e) {
                Util.log("Error occurred while starting NAPI: " + e.getMessage());
            }
        }
    }

    public NAPI add(String title, String description, SettingType type, Object value) {
        if (type == SettingType.INPUT && value instanceof String) {
            Setting.add(title, description, SettingType.INPUT, value);
        } else if (type == SettingType.CHECKBOX && value instanceof Boolean) {
            Setting.add(title, description, SettingType.CHECKBOX, value);
        }
        return this;
    }

    public void disable() {
        if (webCreator != null && webCreator.isAlive()) {
            Util.log("Web Creator shutting down...");
            webCreator.stop();
            webCreator = null;
        }
    }
}
