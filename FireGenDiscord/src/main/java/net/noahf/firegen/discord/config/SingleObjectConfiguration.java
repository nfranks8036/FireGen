package net.noahf.firegen.discord.config;

import com.google.gson.JsonElement;
import lombok.AccessLevel;
import lombok.Getter;
import net.noahf.firegen.api.utilities.FireGenVariables;
import net.noahf.firegen.discord.Main;
import net.noahf.firegen.discord.utilities.JsonUtilities;
import net.noahf.firegen.discord.utilities.Log;
import net.noahf.firegen.discord.utilities.Time;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Getter
public abstract class SingleObjectConfiguration<T> {

    private final FireGenVariables vars;
    private final String path;
    private final DependencyRequest requestedDependencies;
    private final DependencyProvider dependencies;

    protected List<String> lastReloadLog;

    private @Getter(value = AccessLevel.NONE) T object;

    public SingleObjectConfiguration(FireGenVariables vars, String path) {
        this(vars, path, new DependencyRequest());
    }

    public SingleObjectConfiguration(FireGenVariables vars, String path, DependencyRequest deps) {
        this.vars = vars;
        this.path = path;
        this.requestedDependencies = deps;
        this.dependencies = new DependencyProvider(this.requestedDependencies);
        this.lastReloadLog = new LinkedList<>();
    }

    public abstract void importObject(JsonElement element);

    protected void set(T object) {
        this.object = object;
    }

    public T get() {
        return object;
    }

    public void reload() {
        this.lastReloadLog.clear();
        this.object = null;
        JsonUtilities.stream(Main.bot, this.getPath(), this::importObject);
    }

    protected void log(String text) {
        Log.info(text);
        this.lastReloadLog.add("[" +
                LocalDateTime.now()
                        .format(DateTimeFormatter.ofPattern("MMM d yyyy @ HH:mm:ss"))
                + "] " + text
        );
    }

}
