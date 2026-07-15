package net.noahf.firegen.discord.utilities;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.noahf.firegen.discord.Main;
import net.noahf.firegen.discord.bot.BotManager;

import java.io.*;
import java.util.function.Consumer;

public class JsonUtilities {

    public static void stream(BotManager bot, String path, Consumer<JsonElement> streamer) {
        String configPrefix = "";
        if (bot != null) {
            configPrefix = bot.getConfigPrefix();
        } else {
            Log.warn("Provided BotManager is null, defaulting to '' as the config path prefix for '" + path + "'!");
        }

        if (!configPrefix.endsWith("/")) {
            configPrefix = configPrefix + "/";
        }

        File file = new File(configPrefix + path);
        try
                (InputStream input = createInputStream(file, path)) {

            JsonElement element = JsonParser.parseReader(new InputStreamReader(input));
            streamer.accept(element);
        } catch (Exception exception) {
            throw new RuntimeException("Failed to load JSON file from '" + path + "': " + exception, exception);
        }
    }

    public static void stream(BotManager bot, String municipality, String fileString, Consumer<JsonElement> streamer) {
        String path = (municipality != null ? municipality + "/" : "") + fileString;

        stream(bot.getConfigPrefix(), path, streamer);
    }

    public static void stream(String municipality, String fileString, Consumer<JsonElement> streamer) {
        stream(Main.bot, municipality, fileString, streamer);
    }

    public static String asStr(JsonObject object, String key) { return element(object, key).getAsString(); }
    public static int asInt(JsonObject object, String key) { return element(object, key).getAsInt(); }

    public static JsonElement element(JsonObject object, String key) {
        return JsonUtilities.element(object, key, false);
    }

    public static JsonElement element(JsonObject object, String key, boolean optional) {
        if (object == null) {
            throw new IllegalArgumentException("Expected to find key '" + key + "' in json object but found an empty object.");
        }

        JsonElement element = object.get(key);
        if (element == null && !optional) {
            throw new IllegalArgumentException("Expected to find key '" + key + "' in JsonObject: " + object);
        }

        return element;
    }

    private static InputStream createInputStream(File file, String path) throws FileNotFoundException {
        InputStream returned = null;
        if (!file.exists()) {
            returned = Main.class.getClassLoader().getResourceAsStream(path);
        }
        if (file.exists()) {
            returned = new FileInputStream(file);
        }
        if (returned == null) {
            throw new IllegalArgumentException("No configuration file found at '" + file.getAbsolutePath() + "'.");
        }
        return returned;
    }

}
