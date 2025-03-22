package nesoi.network.api.web;

import fi.iki.elonen.NanoHTTPD;
import org.bukkit.Bukkit;
import org.nandayo.DAPI.Util;

import java.io.IOException;

public class WebCreator extends NanoHTTPD {

    public WebCreator() throws IOException {
        super(8080);
        if (!isAlive()) {
            start(SOCKET_READ_TIMEOUT, false);
            Util.log("Config interface started on https:// " + Bukkit.getIp() + " :8080/");
        }


    }

    @Override
    public Response serve(IHTTPSession session) {
        StringBuilder json = new StringBuilder();
        json.append("{\"players\": [");
        Bukkit.getOnlinePlayers().forEach(player -> {
            json.append("\"").append(player.getDisplayName()).append("\",");
        });

        String response;
        if (json.length() > 13) {
            json.setLength(json.length() - 1);
            json.append("]}");
            response = json.toString();
        } else {
            response = "{\"players\": []}";
        }
        return newFixedLengthResponse(Response.Status.OK, "application/json", response);
    }
}
