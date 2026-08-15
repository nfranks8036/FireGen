package net.noahf.firegen.discord.actions.listeners;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.SelfUser;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.noahf.firegen.api.incidents.Incident;
import net.noahf.firegen.api.incidents.units.Agency;
import net.noahf.firegen.api.incidents.units.Unit;
import net.noahf.firegen.api.incidents.units.UnitAssignment;
import net.noahf.firegen.api.utilities.FireGenVariables;
import net.noahf.firegen.api.utilities.IgnoreStringSelector;
import net.noahf.firegen.api.utilities.StringSelectors;
import net.noahf.firegen.discord.Main;
import net.noahf.firegen.discord.bot.DiscordMessages;
import net.noahf.firegen.discord.incidents.messaging.ReceiveMessageSender;
import net.noahf.firegen.discord.incidents.structure.IncidentImpl;
import net.noahf.firegen.discord.utilities.ImmutablePair;
import net.noahf.firegen.discord.utilities.Log;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ContextMenuDetector extends ListenerAdapter {

    public static final Map<String, BiFunction<IncidentImpl, FireGenVariables, MessageEmbed>> commands = new LinkedHashMap<>(
            Map.of(
                    "Show incident overview", ContextMenuDetector::createIncidentDetails,
                    "Show incident log", ContextMenuDetector::createIncidentLog,
                    "Show incident fields", ContextMenuDetector::createFieldsDisplay
            )
    );

    private static final Map<Long, IncidentImpl> cachedIncidents = new HashMap<>();

    @Override
    public void onMessageContextInteraction(@NotNull MessageContextInteractionEvent event) {
        if (!commands.containsKey(event.getName())) {
            return;
        }

        Message target = event.getTarget();
        SelfUser self = Main.bot.jda().getSelfUser();
        if (!target.getAuthor().isBot()
                || !target.getAuthor().equals(self)
        ) {
            DiscordMessages.error(event, "This command only works on messages by " +
                    self.getAsMention()
            );
            return;
        }

        IncidentImpl incident = cachedIncidents.computeIfAbsent(target.getIdLong(),
                (l) ->
                Main.incidents.getIncidents().stream()
                        .map(i -> (IncidentImpl) i)
                        .map(i -> new ImmutablePair<>(i, i.getMessagingService().get(ReceiveMessageSender.class)))
                        .filter(p -> p.getSecondElement() != null)
                        .filter(p -> p.getSecondElement().getMessages().contains(target))
                        .map(ImmutablePair::getFirstElement)
                        .findFirst()
                        .orElse(null)
        );

        if (incident == null) {
            DiscordMessages.error(event, "The message you tried to use this command on is not a valid (or recent) incident.");
            return;
        }

        FireGenVariables vars = Main.config.getFireGenVariables();

        MessageEmbed content = commands.get(event.getName()).apply(incident, vars);

        List<String> keys = new ArrayList<>(commands.keySet());
        int index = 0;
        for (int i = 0; i < keys.size(); i++) {
            if (keys.get(i).equalsIgnoreCase(event.getName())) {
                index = i; break;
            }
        }

        event.replyEmbeds(content)
                .setComponents(
                        ActionRow.of(
                                Button.primary("firegenuser-" + event.getUser().getIdLong() + "-refreshdetails-" + incident.getId() + "-" + index, "Refresh")
                        )
                )
                .setEphemeral(true).queue();
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        String id = event.getComponentId();
        User user = event.getUser();

        if (!id.startsWith("firegenuser")
                || !id.split("-")[2].equalsIgnoreCase("refreshdetails")
        ) {
            return;
        }

        Log.info(user.getName() + " (" + user.getIdLong() + ") pressed button '" + id + "'");

        try {
            long incidentId = Long.parseLong(id.split("-")[3]);
            Incident incident = Main.incidents.getIncidentBy(incidentId);
            MessageEmbed message = new EmbedBuilder()
                    .setTitle("Couldn't find incident!")
                    .setDescription(
                            "*Sorry for the inconvenience, but our records cannot match any incident with ID `" + incidentId + "`. Perhaps the incident was removed!*"
                    )
                    .setColor(new Color(255, 104, 104))
                    .build();
            int index = Integer.parseInt(id.split("-")[4]);
            if (incident != null) {
                String key = new ArrayList<>(commands.keySet()).get(index);
                message = commands.get(key).apply((IncidentImpl) incident, Main.config.getFireGenVariables());
            }

            event.editMessageEmbeds(message)
                    .setComponents(
                            ActionRow.of(
                                    Button.secondary("firegenuser-" + event.getUser().getIdLong() + "-refreshdetails-" + incidentId + "-" + index, "Refresh (Wait 5s)").asDisabled()
                            )
                    )
                    .complete().editOriginalComponents(
                            ActionRow.of(
                                    Button.primary("firegenuser-" + event.getUser().getIdLong() + "-refreshdetails-" + incidentId + "-" + index, "Refresh").asEnabled()
                            )
                    ).completeAfter(5, TimeUnit.SECONDS);
            ;
        } catch (Exception exception) {
            DiscordMessages.error(event, "An error occurred processing your button press", exception);
        }
    }

    private static MessageEmbed createIncidentLog(IncidentImpl incident, FireGenVariables vars) {
        return new EmbedBuilder()
                .setTitle("Incident Log (" + incident.getLog().size() + ")")
                .setDescription(String.join("\n", incident.getMessagingService().getNarrativeFormatted(incident, true, false)))
                .setColor(new Color(166, 92, 59))
                .build();
    }

    public static MessageEmbed createFieldsDisplay(IncidentImpl incident, FireGenVariables vars) {
        Map<String, StringSelectors> selectors = new HashMap<>();
        scan(incident, "", selectors, new HashSet<>());
        List<String> string = new ArrayList<>();
        for (Map.Entry<String, StringSelectors> selector :  selectors.entrySet()) {
            string.add((selector.getKey().isEmpty() ? " " : selector.getKey()) + " = `" + String.join("` | `", selector.getValue().asStringSelectors()) + "`");
        }

        return new EmbedBuilder()
                .setTitle("Incident Fields (" + string.size() + ")")
                .setDescription(DiscordMessages.truncate(String.join("\n", string),
                        MessageEmbed.DESCRIPTION_MAX_LENGTH, "..."))
                .setColor(new Color(59, 92, 166))
                .build();
    }

    private static void scan(
            Object object,
            String path,
            Map<String, StringSelectors> results,
            Set<Object> visited
    ) {
        if (object == null || visited.contains(object)) {
            return;
        }

        visited.add(object);

        Class<?> clazz = object.getClass();

        if (object instanceof StringSelectors selector) {
            results.put(path, selector);
        }

        for (Method method : clazz.getMethods()) {

            if (method.getParameterCount() != 0) {
                continue;
            }

            if (method.isAnnotationPresent(IgnoreStringSelector.class)) {
                continue;
            }

            Class<?> returnType = method.getReturnType();

            try {
                if (!method.getName().equalsIgnoreCase("toString") && String.class.isAssignableFrom(returnType)) {
                    StringSelectors selectors = () -> {
                        try {
                            return List.of((String) method.invoke(object));
                        } catch (NullPointerException nullPointerException) {
                            return List.of(">> NO VALUE SET <<");
                        } catch (Exception e) {
                            if (e.getCause().getClass().equals(NullPointerException.class)) {
                                return List.of(">> NO VALUE SET // CBNULL <<"); //cbnull=caused by null
                            }

                            Log.warn("Could not invoke " + method.getName() + ": " + e, e);
                            return null;
                        }
                    };
                    if (selectors.asStringSelectors() == null) {
                        continue;
                    }
                    results.put((path.isEmpty() ? "" : path + ".") + method.getName(), selectors);

                } else if (List.class.isAssignableFrom(returnType) && isStringSelectorsList(method)) {
                    continue;
//                    @SuppressWarnings("unchecked")
//                    List<? extends StringSelectors> children =
//                            (List<? extends StringSelectors>) method.invoke(object);
//                    if (children != null) {
//
//                        StringSelectors combinedSelector = () -> children.stream()
//                                .flatMap(selector -> selector.asStringSelectors().stream())
//                                .toList();
//
//                        results.put(
//                                path + "." + method.getName(),
//                                combinedSelector
//                        );
//
//                        for (StringSelectors child : children) {
//                            scan(
//                                    child,
//                                    path + "." + child.getClass().getSimpleName(),
//                                    results,
//                                    visited
//                            );
//                        }
//                    }

                } else if (StringSelectors.class.isAssignableFrom(returnType)) {
                    Object child = method.invoke(object);

                    if (child != null) {
                        String childPath =
                                (path.isEmpty() ? "" : path + ".") + returnType.getSimpleName();

                        scan(child, childPath, results, visited);
                    }

                }
            } catch (Exception e) {
                throw new RuntimeException(
                        "Could not invoke " + method.getName(),
                        e
                );
            }
        }
    }

    private static boolean isStringSelectorsList(Method method) {
        Type genericReturnType = method.getGenericReturnType();
        if (!(genericReturnType instanceof ParameterizedType parameterizedType)) {
            return false;
        }
        Type[] typeArguments = parameterizedType.getActualTypeArguments();
        if (typeArguments.length != 1) { return false; }
        Type elementType = typeArguments[0];
        if (elementType instanceof Class<?> clazz) {
            return StringSelectors.class.isAssignableFrom(clazz);
        }
        return false;
    }

    private static MessageEmbed createIncidentDetails(IncidentImpl incident, FireGenVariables vars) {
        String message = "**Title** " + f(()->incident.getType().getSelectedName(), ">NEW<") +
                "\n**Time** " + f(()->incident.getTime().formatDateAndTime(vars, " @ ")) +
                "\n**Incident Number** " + f(incident::getFormattedId, "<none assigned>") +
                "\n**Status** " + f(()->incident.getStatus().name(), "UNKNOWN") + " (" + f(()->incident.getPublished().name()) + ")"
                + " with " + f(()->String.valueOf(incident.getContributors().size()), "0") + " contributor(s)" +
                "\n**Units** " + f(() -> incident.getUnitAssignments().stream()
                .sorted()
                .map(UnitAssignment::getUnit)
                .map(Unit::getShorthand)
                .collect(Collectors.joining(" "))) +
                "\n**Agencies** " + f(()->incident.getUnitAssignments().stream().sorted()
                .map(UnitAssignment::getUnit)
                .map(Unit::getAgency)
                .distinct()
                .map(Agency::getFormatted)
                .collect(Collectors.joining(", "))) +
                "\n**Location** " + f(()->incident.getLocation().format()) +
                "\n**Venue** " + f(()->incident.getLocation().getVenue().toString()) +
                "\n**Narrative** " + f(() -> incident.getNarrative().stream()
                .map(e -> "`" + vars.formatTime(e.getTime(), false) + "` "
                        + e.getEntry()
                )
                .collect(Collectors.joining(" ")));
        Color color = switch (incident.getStatus()) {
            case ACTIVE -> new Color(50, 255, 50);
            case CLOSED, CLOSED_TIMED_OUT -> new Color(114, 114, 114);
            case PENDING -> new Color(94, 175, 255);
        };
        return new EmbedBuilder()
                .setTitle(incident.getType().getSelectedName())
                .setDescription(message)
                .setColor(color)
                .build();
    }

    private static String f(Supplier<String> returned) {
        return f(returned, "");
    }

    private static String f(Supplier<String> returned, String def) {
        try {
            return returned.get();
        } catch (Exception exception) {
            return def;
        }
    }
}
