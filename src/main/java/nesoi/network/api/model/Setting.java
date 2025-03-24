package nesoi.network.api.model;

import nesoi.network.api.enumeration.SettingType;
import org.nandayo.DAPI.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Setting {

    private static final List<Setting> settings = new ArrayList<>();

    public String title; // GENERAL
    public String description; // GENERAL
    public SettingType type; // GENERAL

    public Boolean checkboxValue; // CHECKBOX
    public String inputPlaceholder; // INPUT

    public List<String> comboData; // COMBOBOX
    public Integer comboIndex; // COMBOBOX

    public Integer numericValue; // NumericUpDown
    public Integer maxNumeric; // NumericUpDown
    public Integer minNumeric; // NumericUpDown

    public Consumer<Object> saveHandler; // Save

    // INPUT Constructor
    public Setting(String title, String description, String placeholder, Consumer<Object> saveHandler) {
        this.title = title;
        this.description = description;
        this.type = SettingType.INPUT;
        this.inputPlaceholder = placeholder;
        this.checkboxValue = null;
        this.comboData = null;
        this.comboIndex = -1;
        this.numericValue = null;
        this.minNumeric = null;
        this.maxNumeric = null;
        this.saveHandler = saveHandler != null ? saveHandler : (val -> Util.log("No save handler for " + title + ": " + val));
    }

    // CHECKBOX Constructor
    public Setting(String title, String description, Boolean value, Consumer<Object> saveHandler) {
        this.title = title;
        this.description = description;
        this.type = SettingType.CHECKBOX;
        this.checkboxValue = value;
        this.inputPlaceholder = null;
        this.comboData = null;
        this.comboIndex = -1;
        this.numericValue = null;
        this.minNumeric = null;
        this.maxNumeric = null;
        this.saveHandler = saveHandler != null ? saveHandler : (val -> Util.log("No save handler for " + title + ": " + val));
    }

    // COMBOBOX Constructor
    public Setting(String title, String description, List<String> data, int startingIndex, Consumer<Object> saveHandler) {
        this.title = title;
        this.description = description;
        this.type = SettingType.COMBOBOX;
        this.comboData = data;
        this.comboIndex = (startingIndex >= 0 && startingIndex < data.size()) ? startingIndex : 0;
        this.checkboxValue = null;
        this.inputPlaceholder = null;
        this.numericValue = null;
        this.minNumeric = null;
        this.maxNumeric = null;
        this.saveHandler = saveHandler != null ? saveHandler : (val -> Util.log("No save handler for " + title + ": " + val));
    }

    // NUMERIC Constructor
    public Setting(String title, String description, int value, int minValue, int maxValue, Consumer<Object> saveHandler) {
        this.title = title;
        this.description = description;
        this.type = SettingType.NUMERIC;
        this.numericValue = value;
        this.minNumeric = minValue;
        this.maxNumeric = maxValue;
        this.checkboxValue = null;
        this.inputPlaceholder = null;
        this.comboData = null;
        this.comboIndex = -1;
        this.saveHandler = saveHandler != null ? saveHandler : (val -> Util.log("No save handler for " + title + ": " + val));
    }

    public Setting(String title, String description, String placeholder) {
        this(title, description, placeholder, null);
    }

    public Setting(String title, String description, Boolean value) {
        this(title, description, value, null);
    }

    public Setting(String title, String description, List<String> data, int startingIndex) {
        this(title, description, data, startingIndex, null);
    }

    public Setting(String title, String description, int value, int minValue, int maxValue) {
        this(title, description, value, minValue, maxValue, null);
    }

    public static void add(String title, String description, SettingType type, Object value, Consumer<Object> saveHandler) {
        if (type == SettingType.INPUT && value instanceof String) {
            settings.add(new Setting(title, description, (String) value, saveHandler));
            Util.log("New setting added: " + title + " (input)");
        } else if (type == SettingType.CHECKBOX && value instanceof Boolean) {
            settings.add(new Setting(title, description, (Boolean) value, saveHandler));
            Util.log("New setting added: " + title + " (checkbox)");
        } else if (type == SettingType.COMBOBOX && value instanceof List<?>) {
            try {
                List<String> data = (List<String>) value;
                settings.add(new Setting(title, description, data, 0, saveHandler));
                Util.log("New setting added: " + title + " (combobox)");
            } catch (ClassCastException e) {
                Util.log("&cIncorrect data type for COMBOBOX: " + title + " - waiting List<String>");
            }
        } else if (type == SettingType.NUMERIC && value instanceof Integer[]) {
            Integer[] numericData = (Integer[]) value;
            if (numericData.length == 3) {
                settings.add(new Setting(title, description, numericData[0], numericData[1], numericData[2], saveHandler));
                Util.log("New setting added: " + title + " (numeric up down)");
            } else {
                Util.log("&cIncorrect data for NUMERIC_UP_DOWN: " + title + " - waiting [value, min, max]");
            }
        } else {
            Util.log("&cIncorrect setting type " + type + " for setting " + title);
        }
    }

    public static List<Setting> getSettings() {
        return settings;
    }
}