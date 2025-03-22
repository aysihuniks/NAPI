package nesoi.network.api;

import nesoi.network.api.web.WebCreator;
import org.bukkit.plugin.Plugin;
import org.nandayo.DAPI.Util;

import java.io.IOException;

public final class Main {

    public final Plugin plugin;
    public static Main instance;

    public Main(Plugin plugin) throws IOException {
        instance = this;
        this.plugin = plugin;

        if (plugin != null) {
            Util.PREFIX = "&8[<#fa8443>NAPI&8]&r ";
            new WebCreator();
        }
    }
}
