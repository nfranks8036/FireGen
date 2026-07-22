package net.noahf.firegen.discord.config;

import com.google.gson.JsonElement;
import lombok.AccessLevel;
import lombok.Getter;
import net.noahf.firegen.api.utilities.FireGenVariables;
import net.noahf.firegen.discord.Main;
import net.noahf.firegen.discord.utilities.JsonUtilities;

@Getter
public abstract class SingleObjectConfiguration<T> {

    private final FireGenVariables vars;
    private final String path;
    private final DependencyRequest requestedDependencies;
    private final DependencyProvider dependencies;

    private @Getter(value = AccessLevel.NONE) T object;

    public SingleObjectConfiguration(FireGenVariables vars, String path) {
        this(vars, path, new DependencyRequest());
    }

    public SingleObjectConfiguration(FireGenVariables vars, String path, DependencyRequest deps) {
        this.vars = vars;
        this.path = path;
        this.requestedDependencies = deps;
        this.dependencies = new DependencyProvider(this.requestedDependencies);
    }

    public abstract void importObject(JsonElement element);

    protected void set(T object) {
        this.object = object;
    }

    public T get() {
        return object;
    }

    public void reload() {
        this.object = null;
        JsonUtilities.stream(Main.bot, this.getPath(), this::importObject);
    }

}
