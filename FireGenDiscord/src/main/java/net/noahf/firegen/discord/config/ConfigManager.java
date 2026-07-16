package net.noahf.firegen.discord.config;

import lombok.Getter;
import net.noahf.firegen.api.utilities.FireGenVariables;
import net.noahf.firegen.discord.bot.BotManager;
import net.noahf.firegen.discord.utilities.JsonUtilities;
import net.noahf.firegen.discord.utilities.Log;
import net.noahf.firegen.discord.utilities.Manager;
import org.reflections.Reflections;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public class ConfigManager extends Manager<ConfigManager> {

    public static final String CONFIG_FILES_PACKAGE = "net.noahf.firegen.discord.config.files";

    private final BotManager bot;
    private final FireGenVariables fireGenVariables;
    private final List<SingleObjectConfiguration<?>> configs;

    public ConfigManager(BotManager bot) {
        super(ConfigManager.class, "Config");

        this.bot = bot;
        this.fireGenVariables = FireGenVariables.createFromFolder(bot.getMunicipalityFolder());
        this.configs = new ArrayList<>();
    }

    @SuppressWarnings("rawtypes")
    public ConfigManager startImport() {
        // find config files classes and instantiate
        long start = System.currentTimeMillis();
        Set<Class<? extends SingleObjectConfiguration>> classes =
                new Reflections(CONFIG_FILES_PACKAGE)
                        .getSubTypesOf(SingleObjectConfiguration.class); // we only care about those which extend this class

        classes = classes.stream()
                .filter(c -> !c.equals(MultiObjectConfiguration.class))
                .collect(Collectors.toSet());

        Log.info("Importing structure data from municipality '" + bot.getMunicipalityFolder() + "'");
        for (Class<? extends SingleObjectConfiguration> clazz : classes) {
            try {
                @Nullable Constructor<?> constructor =
                        Arrays.stream(clazz.getDeclaredConstructors())
                                // since we don't really know what other constructors exist, we can only confidently
                                // instantiate a constructor with one parameter.
                                .filter(c -> c.getParameterCount() == 1)
                                .filter(c -> c.getParameters()[0].getType().isAssignableFrom(FireGenVariables.class))

                                .findFirst().orElse(null);

                if (constructor == null) {
                    throw new IllegalArgumentException("Expected at least one constructor of " + clazz.getCanonicalName() + " to have one parameter of FireGenVariables, failed to find any.");
                }

                SingleObjectConfiguration<?> newInstance = (SingleObjectConfiguration<?>) constructor.newInstance(fireGenVariables);
                newInstance.reload();
                this.configs.add(newInstance);

            } catch (InstantiationException | IllegalAccessException | InvocationTargetException error) {
                Log.error("An error occurred while registering commands: " + error, error);
            }

        }

        Log.info("Imported " + this.configs.size() + " configuration files in " + (System.currentTimeMillis()-start) + "ms!");

        return this;
    }

    public <T extends SingleObjectConfiguration<?>> T get(Class<T> object) {
        for (SingleObjectConfiguration<?> value : this.getConfigs()) {
            if (value.getClass().equals(object)) {
                return object.cast(value);
            }
        }
        throw new IllegalStateException("Class '" + object.getCanonicalName() + "' is not a valid configuration class.");
    }

}
