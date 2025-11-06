package iecompbot;

import my.utilities.json.JSONItem;

import java.io.FileReader;
import java.nio.charset.StandardCharsets;

public class Preferences extends JSONItem {
    public String Activity = "Playing";
    public String ActivityStatus = "Inazuma Eleven: Victory Road";
    public boolean isAutoLaunchEnabled = false;
    public boolean isAppConsoleEnabled = false;
    public boolean TestMode = true;
    public String Token = "N/A";
    public String TestToken = "N/A";
    public String DatabaseURL = "N/A";
    public String DatabaseUsername = "N/A";
    public String DatabasePassword = "N/A";
    public String ChatGPTToken = "N/A";
    public String DeepAIToken = "N/A";

    public static Preferences Load() {
        try (FileReader reader = new FileReader("./defaults.json", StandardCharsets.UTF_8)) {
            return GSON.fromJson(reader, Preferences.class);
        } catch (Exception e) {
            return new Preferences().Save();
        }
    }
    public Preferences Save() {
        System.out.println("[Default] Saving the defaults...");
        Save("./defaults.json");
        return this;
    }
}
