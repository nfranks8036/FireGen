package net.noahf.firegen.discord.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DependencyProvider {

    private final DependencyRequest requested;
    private final Map<Class<? extends SingleObjectConfiguration<?>>, SingleObjectConfiguration<?>> configurations;

    DependencyProvider(DependencyRequest requested) {
        this.requested = requested;
        this.configurations = new HashMap<>();
    }

    void inject(Class<? extends SingleObjectConfiguration<?>> clazz, SingleObjectConfiguration<?> newInstance) {
        configurations.put(clazz, newInstance);
    }

    public <T extends SingleObjectConfiguration<?>> T get(Class<T> clazz) {
        if (!requested.getClasses().contains(clazz)) {
            throw new IllegalArgumentException("Class '" + clazz.getCanonicalName() + "' was not noted in the dependency request!");
        }

        Object object = configurations.get(clazz);
        if (object == null) {
            throw new IllegalArgumentException("Object for '" + clazz.getCanonicalName() + "' failed to resolve.");
        }

        return clazz.cast(object);
    }

    public boolean isSatisfied() {
        return requested.getClasses().size() == configurations.size();
    }

    @SuppressWarnings("unchecked")
    void loadIfDependenciesAreLoaded(DependencyRequest requester, List<? extends SingleObjectConfiguration<?>> dependencies) {
        for (SingleObjectConfiguration<?> dep : dependencies) {
            if (!requester.getClasses().contains(dep.getClass())) {
                continue;
            }
            this.inject((Class<? extends SingleObjectConfiguration<?>>) dep.getClass(), dep);
        }
    }

}
