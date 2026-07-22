package net.noahf.firegen.discord.config;

import lombok.AccessLevel;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter(value = AccessLevel.PACKAGE)
public class DependencyRequest {

    private final List<Class<? extends SingleObjectConfiguration<?>>> classes;

    public DependencyRequest() {
        this.classes = new ArrayList<>();
    }

    public DependencyRequest dependOn(Class<? extends SingleObjectConfiguration<?>> clazz) {
        this.classes.add(clazz); return this;
    }

    public boolean hasDependencies() {
        return !this.classes.isEmpty();
    }

}
