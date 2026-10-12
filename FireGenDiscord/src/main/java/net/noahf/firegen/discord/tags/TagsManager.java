package net.noahf.firegen.discord.tags;

import lombok.Getter;
import net.noahf.firegen.api.incidents.tags.IncidentTag;
import net.noahf.firegen.discord.utilities.Log;
import org.reflections.Reflections;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

public class TagsManager {

    public static final String TAGS_PACKAGE = "net.noahf.firegen.discord.tags.registered";

    private final @Getter List<IncidentTag> tags = new ArrayList<>();

    public TagsManager() {
        // find tags classes and instantiate
        Set<Class<? extends IncidentTag>> classes =
                new Reflections(TAGS_PACKAGE)
                        .getSubTypesOf(IncidentTag.class).stream()
                        .filter(c -> !c.isInterface())
                        .collect(Collectors.toSet());

        Log.info("Attempting to load and register " + classes.size() + " commands in " + TAGS_PACKAGE + "...");

        StringJoiner registered = new StringJoiner(", ");
        for (Class<? extends IncidentTag> clazz : classes) {

            try {

                @Nullable Constructor<?> constructor =
                        Arrays.stream(clazz.getDeclaredConstructors())
                                // since we don't really know what other constructors exist, we can only confidently
                                // instantiate a constructor with zero parameters.
                                .filter(c -> c.getParameterCount() == 0)

                                .findFirst().orElse(null);

                if (constructor == null) {
                    throw new IllegalArgumentException("Expected at least one constructor of " + clazz.getCanonicalName() + " to have zero parameters, failed to find any.");
                }

                IncidentTag newInstance = (IncidentTag) constructor.newInstance();

                tags.add(newInstance);
                registered.add(newInstance.getName());

            } catch (InstantiationException | IllegalAccessException | InvocationTargetException error) {
                Log.error("An error occurred while registering commands: " + error, error);
            }

        }

        Log.info("Registered " + tags.size() + " tags: " + registered.toString());
    }

    public IncidentTag getByName(String name) {
        for (IncidentTag tag : tags) {
            if (tag.getName().equalsIgnoreCase(name)) {
                return tag;
            }
        }
        return null;
    }

}
