package nesoi.network.api;

import nesoi.network.api.web.Setting;
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
            if (webCreator != null && webCreator.isAlive()) {
                Util.log("Web Creator closing...");
                webCreator.stop();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Util.log("Port ");
                }
            }

            try {
                webCreator = new WebCreator(plugin);
                Util.log("Web Creator started on https://" + Bukkit.getIp() + ":8080/");
            } catch (IOException e) {
                Util.log("Web Creator failed to start.");
            }
        }
    }

    public NAPI add(String title, String description, String type, Object value) {
        Setting.add(title, description, type, value);
        return this;
    }


}
