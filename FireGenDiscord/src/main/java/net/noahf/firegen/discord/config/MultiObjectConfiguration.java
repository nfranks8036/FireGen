package net.noahf.firegen.discord.config;

import com.google.gson.JsonElement;
import lombok.Getter;
import net.noahf.firegen.api.utilities.FireGenVariables;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class MultiObjectConfiguration<T> extends SingleObjectConfiguration<List<T>> {

    private @Getter final Class<T> listElementType;

    public MultiObjectConfiguration(FireGenVariables vars, Class<T> listElementType, String path) {
        super(vars, path);
        this.listElementType = listElementType;
        this.clear();
    }

    protected void clear() {
        this.set(new ArrayList<>());
    }

    protected void add(T element) {
        this.get().add(element);
    }

    protected void addAll(List<T> elements) {
        this.get().addAll(elements);
    }

    protected void addAll(T... elements) {
        this.addAll(Arrays.stream(elements).toList());
    }

    public int count() {
        return this.get().size();
    }

}
