package net.noahf.firegen.discord.config;

import com.google.gson.JsonObject;
import lombok.Getter;
import net.noahf.firegen.api.utilities.FireGenVariables;
import net.noahf.firegen.discord.bot.BotManager;
import net.noahf.firegen.discord.utilities.Log;
import net.noahf.firegen.discord.utilities.Manager;
import org.reflections.Reflections;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static net.noahf.firegen.discord.utilities.JsonUtilities.*;

@Getter
public class ConfigManager extends Manager<ConfigManager> {

    public static final String CONFIG_FILES_PACKAGE = "net.noahf.firegen.discord.config.files";
    public static final int MAXIMUM_DEPENDENCY_TRIES = 5;

    private final BotManager bot;
    private final FireGenVariables fireGenVariables;
    private final List<SingleObjectConfiguration<?>> configs;

    public ConfigManager(BotManager bot) {
        super(ConfigManager.class, "Config");

        this.bot = bot;
        this.fireGenVariables = this.findVars(bot);
        this.configs = new ArrayList<>();
    }

    private FireGenVariables findVars(BotManager bot) {
        String municipality = bot.getMunicipalityFolder();
        FireGenVariables.FireGenVariablesBuilder builder = FireGenVariables.createFromFolder(municipality);
        AtomicReference<FireGenVariables> vars = new AtomicReference<>(builder.build().resetToDefault(municipality, true));
        stream(bot, "settings.json", (e) -> {
            JsonObject root = e.getAsJsonObject();
            JsonObject files = root.get("files").getAsJsonObject();
            builder
                    .incidentStaleMinutes(asInt(root, "incidents_become_stale_minutes"))
                    .shortTimeFormat(asStr(root, "short_time"))
                    .longTimeFormat(asStr(root, "long_time"))
                    .dateFormat(asStr(root, "date"))
                    .incidentTypesFile(asStr(files, "incident_types"))
                    .unitsFile(asStr(files, "units"))
                    .venuesFile(asStr(files, "venues"))
                    .municipalityFile(asStr(files, "municipality"))
                    .assignmentStatusFile(asStr(files, "assignment_statuses"))
                    .incidentStatusFile(asStr(files, "incident_statuses"))
                    .locationPresetsFile(asStr(files, "locations_presets"))
                    .radioChannelsFile(asStr(files, "radio_channels"))
                    .usersFile(asStr(files, "users"));
            vars.set(builder.build());
        });
        FireGenVariables returned = vars.get();
        if (returned == null) {
            throw new IllegalStateException("Atomic did not return a value, got 'null' expected FireGenVariables");
        }
        Log.info("Imported settings: " + returned.toString());
        if (returned.defaults()) {
            Log.warn("Using defaults for FireGenVars, not user-selected ones from settings.json!");
        }
        return returned;
    }

    @SuppressWarnings("rawtypes")
    public ConfigManager startImport() {
        // find config files classes and instantiate
        Set<Class<? extends SingleObjectConfiguration>> classes =
                new Reflections(CONFIG_FILES_PACKAGE)
                        .getSubTypesOf(SingleObjectConfiguration.class); // we only care about those which extend this class

        classes = classes.stream()
                .filter(c -> !c.equals(MultiObjectConfiguration.class))
                .collect(Collectors.toSet());

        Log.info("Attempting to load and register " + classes.size() + " configuration files in " + CONFIG_FILES_PACKAGE + "...");

        StringJoiner registered = new StringJoiner(", ");
        this.register(0, classes).forEach(registered::add);

        Log.info("Registered " + this.configs.size() + " configuration files: " + registered.toString());

        return this;
    }

    @SuppressWarnings("rawtypes")
    private List<String> register(int depth, Set<Class<? extends SingleObjectConfiguration>> classes) {
        if (depth > MAXIMUM_DEPENDENCY_TRIES) {
            throw new IllegalStateException("Failed to register classes and their dependencies for configuration files: " + classes.toString() + " (tried " + depth + " times). Are the requests circular?");
        }

        List<String> registeredList = new ArrayList<>();
        Set<Class<? extends SingleObjectConfiguration>> rerunDependencies = new HashSet<>();
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

                DependencyRequest dependencies = newInstance.getRequestedDependencies();
                DependencyProvider provider = newInstance.getDependencies();
                if (dependencies.hasDependencies()) {
                    provider.loadIfDependenciesAreLoaded(dependencies, this.configs);
                    if (!provider.isSatisfied()) {
                        rerunDependencies.add(clazz);
                        continue;
                    }
//                    Log.info("Dependencies satisfied: " + dependencies.getClasses().toString());
                }

                newInstance.reload();
                this.configs.add(newInstance);
                registeredList.add(newInstance.getPath());

            } catch (InstantiationException | IllegalAccessException | InvocationTargetException error) {
                Log.error("An error occurred while registering commands: " + error, error);
            }

        }

        if (!rerunDependencies.isEmpty()) {
//            Log.info("Dependencies not satisfied, re-running: " + rerunDependencies.toString());
            registeredList.addAll(
                    this.register(++depth, rerunDependencies)
            );
        }

        return registeredList;
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
