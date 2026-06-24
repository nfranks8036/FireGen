package net.noahf.firegen.discord.utilities;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.noahf.firegen.api.utilities.FireGenVariables;
import net.noahf.firegen.discord.Main;

import java.io.*;
import java.util.function.Consumer;

public class JsonUtilities {

    public static void stream(String municipality, String fileString, Consumer<JsonElement> streamer) {
        String path = (municipality != null ? municipality + "/" : "") + fileString;
        File file = new File(path);
        try
                (InputStream input = createInputStream(file, path)) {

            JsonElement element = JsonParser.parseReader(new InputStreamReader(input));
            streamer.accept(element);
        } catch (Exception exception) {
            throw new RuntimeException("Failed to load JSON file from '" + path + "': " + exception, exception);
        }
    }

    public static InputStream createInputStream(File file, String path) throws FileNotFoundException {
        InputStream returned = null;
        if (!file.exists()) {
            returned = Main.class.getClassLoader().getResourceAsStream(path);
        }
        if (file.exists()) {
            returned = new FileInputStream(file);
        }
        if (returned == null) {
            throw new IllegalArgumentException("No configuration file found at '" + file.getAbsolutePath() + "' or internally.");
        }
        return returned;
    }

}
