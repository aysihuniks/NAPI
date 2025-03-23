package nesoi.network.api.web;

import fi.iki.elonen.NanoHTTPD;
import nesoi.network.api.enumeration.SettingType;
import nesoi.network.api.model.Setting;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.nandayo.DAPI.Util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class WebCreator extends NanoHTTPD {
    private final Plugin plugin;

    public WebCreator(Plugin plugin) throws IOException {
        super(8080);
        this.plugin = plugin;
        start(SOCKET_READ_TIMEOUT, false);
    }

    @Override
    public Response serve(IHTTPSession session) {
        String uri = session.getUri();

        if (uri.equals("/api/players")) {
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
        } else if (uri.equals("/settings")) {
            String htmlTemplate;
            try (InputStream is = plugin.getResource("index.html")) {
                if (is == null) {
                    Util.log("index.html not found!");
                    return newFixedLengthResponse("500 - index.html not found");
                }
                htmlTemplate = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                Util.log("HTML not readable: " + e.getMessage());
                return newFixedLengthResponse("500 - HTML not readable");
            }

            StringBuilder cards = new StringBuilder();
            Setting.getSettings().forEach(setting -> {
                cards.append("<div class=\"card\">")
                        .append("<div class=\"card-prefix\">").append(setting.title).append("</div>")
                        .append("<div class=\"card-content\">")
                        .append("<p>").append(setting.description).append("</p>");

                if (setting.type == SettingType.INPUT) {
                    cards.append("<input type=\"text\" class=\"form-control\" id=\"")
                            .append(setting.title.replace(" ", "_")).append("Input\" placeholder=\"")
                            .append(setting.placeholder != null ? setting.placeholder : "").append("\">");
                } else if (setting.type == SettingType.CHECKBOX) {
                    cards.append("<div class=\"form-check form-switch\">")
                            .append("<input class=\"form-check-input toggle-input\" type=\"checkbox\" id=\"")
                            .append(setting.title.replace(" ", "_")).append("\"")
                            .append(setting.value != null && setting.value ? " checked" : "").append(">")
                            .append("<label class=\"form-check-label\" for=\"")
                            .append(setting.title.replace(" ", "_")).append("\"></label>")
                            .append("</div>");
                }

                cards.append("</div>").append("</div>");
            });

            String finalHtml = htmlTemplate.replace("<!--SETTINGS_CARDS-->", cards.toString());
            return newFixedLengthResponse(Response.Status.OK, "text/html", finalHtml);
        }

        return newFixedLengthResponse("404 - Undefined");
    }

}