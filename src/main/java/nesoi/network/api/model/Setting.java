package nesoi.network.api.model;

import nesoi.network.api.enumeration.SettingType;
import org.nandayo.DAPI.Util;

import java.util.ArrayList;
import java.util.List;

public class Setting {

    private static final List<Setting> settings = new ArrayList<>();

    public String title;
    public String description;
    public SettingType type;

    public Boolean value;
    public String placeholder;

    public List<String> data;
    public int index;

    public Setting(String title, String description, String placeholder) {
        this.title = title;
        this.description = description;
        this.type = SettingType.INPUT;
        this.placeholder = placeholder;

        this.value = null;
    }

    public Setting(String title, String description, Boolean value) {
        this.title = title;
        this.description = description;
        this.type = SettingType.CHECKBOX;
        this.value = value;

        this.placeholder = null;

    }

    public Setting(String title, String description, List<String> data, int startingIndex) {
        this.title = title;
        this.description = description;
        this.type = SettingType.COMBOBOX;
        this.data = data;
        this.index = (startingIndex >= 0 && startingIndex < data.size()) ? startingIndex : 0;
        this.value = null;
        this.placeholder = null;
    }

    public static void add(String title, String description, SettingType type, Object value) {
        if (type == SettingType.INPUT && value instanceof String) {
            settings.add(new Setting(title, description, (String) value));
            Util.log("New setting added: " + title + " (input)");
        } else if (type == SettingType.CHECKBOX && value instanceof Boolean) {
            settings.add(new Setting(title, description, (Boolean) value));
            Util.log("New setting added: " + title + " (checkbox)");
        } else if (type == SettingType.COMBOBOX && value instanceof List<?>) {
            try {
                List<String> data = (List<String>) value;
                settings.add(new Setting(title, description, data, 0));
                Util.log("New setting added: " + title + " (combobox)");
            } catch (ClassCastException e) {
                Util.log("&cIncorrect data type for COMBOBOX: " + title + " - waiting List<String>");
            }
        } else {
            Util.log("&cIncorrect setting type " + type + " for setting " + title);
        }
    }

    public static List<Setting> getSettings() {
        return settings;
    }

}
