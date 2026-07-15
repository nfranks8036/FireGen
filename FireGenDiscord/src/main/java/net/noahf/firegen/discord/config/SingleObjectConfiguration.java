package net.noahf.firegen.discord.config;

import com.google.gson.JsonElement;
import lombok.AccessLevel;
import lombok.Getter;
import net.noahf.firegen.api.utilities.FireGenVariables;

import java.util.ArrayList;
import java.util.List;

@Getter
public abstract class SingleObjectConfiguration<T> {

    private final FireGenVariables vars;
    private final String path;

    private @Getter(value = AccessLevel.NONE) T object;

    public SingleObjectConfiguration(FireGenVariables vars, String path) {
        this.vars = vars;
        this.path = path;
    }

    public abstract void importObject(JsonElement element);

    protected void set(T object) {
        this.object = object;
    }

    public T get() {
        return object;
    }

}
