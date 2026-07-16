package net.noahf.firegen.discord.bot.channels;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.noahf.firegen.api.incidents.Incident;
import net.noahf.firegen.api.utilities.StringSelectors;
import net.noahf.firegen.discord.bot.BotManager;
import net.noahf.firegen.discord.incidents.structure.IncidentImpl;
import net.noahf.firegen.discord.incidents.structure.IncidentStatusEmoji;
import net.noahf.firegen.discord.utilities.JsonUtilities;
import net.noahf.firegen.discord.utilities.Log;
import org.reflections.Reflections;

import java.util.*;

public class ChannelManager {

    public static final String API_PACKAGE = "net.noahf.firegen.api";

    private final BotManager bot;

    private @Getter final List<Class<? extends StringSelectors>> classes;
    private @Getter final List<FireGenChannel> channels;

    public ChannelManager(BotManager bot) {
        this.bot = bot;
        this.classes = new ArrayList<>();
        this.channels = new ArrayList<>();
    }

    public List<TextChannel> getFor(ChannelRole role, Incident incident) {
        List<TextChannel> returned = new ArrayList<>();
        for (FireGenChannel channel : this.channels) {
            try {
                if (channel.getRole() != role) {
                    continue;
                }

                if (!channel.evaluateConditionals(incident)) {
                    continue;
                }

                returned.add(channel.getChannel());
            } catch (Exception exception) {
                throw new IllegalStateException("Failed to add channel '" + channel.toString() + "': " + exception, exception);
            }
        }
        return returned;
    }

    public void startImport() {
        this.classes.addAll(new Reflections(API_PACKAGE).getSubTypesOf(StringSelectors.class));

        JsonUtilities.stream(bot, null, "discord.json", (e) -> {
            JsonArray array = e.getAsJsonArray();
            for (JsonElement item : array.asList()) {
                JsonObject object = item.getAsJsonObject();

                long channel = JsonUtilities.asLong(object, "channel");
                ChannelRole role = ChannelRole.valueOf(JsonUtilities.asStr(object, "role"));
                JsonElement condElement = JsonUtilities.element(object, "if", true);
                List<ChannelConditional> conditionals = null;
                if (condElement != null && !condElement.isJsonNull() && condElement.isJsonArray()) {
                    conditionals = this.getConditionalsFor(condElement.getAsJsonArray());
                }

                try {
                    FireGenChannel fireGenChannel = new FireGenChannel(bot.jda(), channel, role, conditionals);
                    this.channels.add(fireGenChannel);
                } catch (DefectiveChannelError error) {
                    Log.warn(error.toString());
                }
            }
        });
    }

    private List<ChannelConditional> getConditionalsFor(JsonArray array) {
        List<ChannelConditional> conditionals = new ArrayList<>();
        for (JsonElement element : array.asList()) {
            String string = element.getAsString();

            List<Class<? extends StringSelectors>> path;
            List<String> arguments = new ArrayList<>();
            try {
                String classArgument = string.split("=")[0];
                path = Arrays.stream(classArgument.split("\\."))
                        .map(s -> {
                            for (Class<? extends StringSelectors> c : classes) {
                                if (c.getSimpleName().equalsIgnoreCase(s) ||
                                        c.getName().equalsIgnoreCase(s) ||
                                        c.getCanonicalName().equalsIgnoreCase(s))
                                    return c;
                            }
                            return InvalidStringSelector.class;
                        })
                        .filter(c -> c != InvalidStringSelector.class)
                        .toList();

                String equalsArgument = string.split("=")[1];
                if (equalsArgument.contains("|"))
                    arguments.addAll(Arrays.stream(equalsArgument.split("\\|")).toList());
                else
                    arguments.add(equalsArgument);

                conditionals.add(new ChannelConditional(
                        path, arguments
                ));
            } catch (Exception exception) {
                Log.warn("******* ERROR IMPORTING CHANNEL W/ CONDITIONAL *******");
                Log.warn("Conditional: " + string);
                Log.warn("Error: " + exception);
                Log.warn("******************************************************");
            }
        }

        return conditionals;
    }

    private static class InvalidStringSelector implements StringSelectors {
        @Override
        public List<String> asStringSelectors() {
            return List.of();
        }
    }

}
