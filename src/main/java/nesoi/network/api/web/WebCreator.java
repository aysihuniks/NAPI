package nesoi.network.api.web;

import fi.iki.elonen.NanoHTTPD;
import nesoi.network.api.model.Setting;
import nesoi.network.api.enumeration.SettingType;
import org.bukkit.plugin.Plugin;
import org.json.JSONObject;
import org.nandayo.DAPI.Util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class WebCreator extends NanoHTTPD {

    private final Plugin plugin;

    public WebCreator(Plugin plugin) throws IOException {
        super(8080);
        this.plugin = plugin;
        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
    }

    @Override
    public Response serve(IHTTPSession session) {
        String uri = session.getUri();
        Method method = session.getMethod();

        if (method == Method.GET && uri.equals("/settings")) {
            String htmlContent = loadHTMLTemplate("settings.html");
            StringBuilder cards = new StringBuilder();
            Setting.getSettings().forEach(setting -> {
                cards.append("<div class=\"card\">")
                        .append("<div class=\"card-prefix\">").append(setting.title).append("</div>")
                        .append("<div class=\"card-content\">")
                        .append("<p>").append(setting.description).append("</p>");

                if (setting.type == SettingType.INPUT) {
                    cards.append("<input type=\"text\" class=\"form-control\" id=\"")
                            .append(setting.title.replace(" ", "_")).append("Input\" placeholder=\"")
                            .append(setting.inputPlaceholder != null ? setting.inputPlaceholder : "").append("\"")
                            .append(setting.inputPlaceholder != null ? " value=\"" + setting.inputPlaceholder + "\"" : "").append(">");
                } else if (setting.type == SettingType.CHECKBOX) {
                    cards.append("<div class=\"form-check form-switch\">")
                            .append("<input class=\"form-check-input toggle-input\" type=\"checkbox\" id=\"")
                            .append(setting.title.replace(" ", "_")).append("\"")
                            .append(setting.checkboxValue != null && setting.checkboxValue ? " checked" : "").append(">")
                            .append("<label class=\"form-check-label\" for=\"")
                            .append(setting.title.replace(" ", "_")).append("\"></label>")
                            .append("</div>");
                } else if (setting.type == SettingType.COMBOBOX) {
                    cards.append("<select class=\"custom-select\" id=\"")
                            .append(setting.title.replace(" ", "_")).append("Input\">");
                    if (setting.comboData != null) {
                        for (int i = 0; i < setting.comboData.size(); i++) {
                            cards.append("<option value=\"").append(setting.comboData.get(i)).append("\"")
                                    .append(i == setting.comboIndex ? " selected" : "").append(">")
                                    .append(setting.comboData.get(i)).append("</option>");
                        }
                    }
                    cards.append("</select>");
                } else if (setting.type == SettingType.NUMERIC) {
                    cards.append("<div class=\"numeric-wrapper\" id=\"")
                            .append(setting.title.replace(" ", "_")).append("Wrapper\">")
                            .append("<input type=\"number\" class=\"numeric-input\" id=\"")
                            .append(setting.title.replace(" ", "_")).append("Input\" value=\"")
                            .append(setting.numericValue).append("\" min=\"")
                            .append(setting.minNumeric).append("\" max=\"")
                            .append(setting.maxNumeric).append("\" readonly>")
                            .append("<div class=\"numeric-buttons\">")
                            .append("<span class=\"numeric-up\"></span>")
                            .append("<span class=\"numeric-down\"></span>")
                            .append("</div></div>");
                }

                cards.append("</div>").append("</div>");
            });

            htmlContent = htmlContent.replace("<!--SETTINGS_CARDS-->", cards.toString());
            return newFixedLengthResponse(htmlContent);
        } else if (method == Method.POST && uri.equals("/update-config")) {
            try {
                String body = new String(session.getInputStream().readNBytes(session.getInputStream().available()), StandardCharsets.UTF_8);
                JSONObject json = new JSONObject(body);

                boolean hasChanges = false;
                for (Setting setting : Setting.getSettings()) {
                    String key = setting.title;
                    if (json.has(key)) {
                        Object newValue = json.get(key);
                        boolean changed = false;
                        String oldValueStr = "";

                        switch (setting.type) {
                            case INPUT:
                                String newPlaceholder = newValue.toString();
                                oldValueStr = setting.inputPlaceholder != null ? setting.inputPlaceholder : "";
                                if (!newPlaceholder.equals(oldValueStr)) {
                                    setting.inputPlaceholder = newPlaceholder;
                                    if (setting.saveHandler != null) setting.saveHandler.accept(setting.inputPlaceholder);
                                    changed = true;
                                }
                                break;
                            case CHECKBOX:
                                boolean newBool = Boolean.parseBoolean(newValue.toString());
                                oldValueStr = String.valueOf(setting.checkboxValue != null ? setting.checkboxValue : false);
                                if (newBool != (setting.checkboxValue != null && setting.checkboxValue)) {
                                    setting.checkboxValue = newBool;
                                    if (setting.saveHandler != null) setting.saveHandler.accept(setting.checkboxValue);
                                    else Util.log("No save handler for " + key);
                                    changed = true;
                                }
                                break;
                            case COMBOBOX:
                                int newIndex = setting.comboData.indexOf(newValue.toString());
                                oldValueStr = setting.comboData.get(setting.comboIndex);
                                if (newIndex != -1 && newIndex != setting.comboIndex) {
                                    setting.comboIndex = newIndex;
                                    if (setting.saveHandler != null) setting.saveHandler.accept(setting.comboData.get(newIndex));
                                    else Util.log("No save handler for " + key);
                                    changed = true;
                                }
                                break;
                            case NUMERIC:
                                int newNumeric = Integer.parseInt(newValue.toString());
                                oldValueStr = String.valueOf(setting.numericValue != null ? setting.numericValue : 0);
                                if (newNumeric != (setting.numericValue != null ? setting.numericValue : 0)) {
                                    setting.numericValue = newNumeric;
                                    if (setting.saveHandler != null) setting.saveHandler.accept(setting.numericValue);
                                    else Util.log("No save handler for " + key);
                                    changed = true;
                                }
                                break;
                        }

                        if (changed) {
                            hasChanges = true;
                            Util.log(key.replace(" ", "_") + ": " + oldValueStr + " -> " + newValue);
                        }
                    }
                }

                if (hasChanges) {
                    return newFixedLengthResponse("Settings updated successfully!");
                } else {
                    return newFixedLengthResponse("No changes detected.");
                }
            } catch (Exception e) {
                Util.log("&cError updating settings: " + e.getMessage());
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", "Error: " + e.getMessage());
            }
        }

        String notFoundPage = loadHTMLTemplate("404.html");
        return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/html", notFoundPage);
    }

    private String loadHTMLTemplate(String fileName) {
        try (InputStream is = plugin.getResource(fileName)) {
            if (is == null) {
                Util.log("&cError: " + fileName + " not found in resources!");
                return "<h1>Error: " + fileName + " not found</h1>";
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            Util.log("&cError loading " + fileName + ": " + e.getMessage());
            return "<h1>Error loading " + fileName + "</h1>";
        }
    }
}