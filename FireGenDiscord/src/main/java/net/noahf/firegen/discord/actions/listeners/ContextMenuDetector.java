package net.noahf.firegen.discord.actions.listeners;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.noahf.firegen.api.incidents.Incident;
import net.noahf.firegen.api.incidents.units.Unit;
import net.noahf.firegen.api.incidents.units.UnitAssignment;
import net.noahf.firegen.api.utilities.FireGenVariables;
import net.noahf.firegen.discord.Main;
import net.noahf.firegen.discord.incidents.messaging.ReceiveMessageSender;
import net.noahf.firegen.discord.incidents.structure.IncidentImpl;
import net.noahf.firegen.discord.utilities.DiscordMessages;
import net.noahf.firegen.discord.utilities.ImmutablePair;
import net.noahf.firegen.discord.utilities.Log;
import net.noahf.firegen.discord.utilities.UnitsResponseType;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ContextMenuDetector extends ListenerAdapter {

    private static final Map<Long, IncidentImpl> cachedIncidents = new HashMap<>();

    @Override
    public void onMessageContextInteraction(@NotNull MessageContextInteractionEvent event) {
        if (!event.getName().equalsIgnoreCase("Show incident details")) {
            return;
        }

        Message target = event.getTarget();
        if (!target.getAuthor().isBot()
                || !target.getAuthor().equals(Main.JDA.getSelfUser())
        ) {
            DiscordMessages.error(event, "This command only works on messages by " +
                    Main.JDA.getSelfUser().getAsMention()
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

        FireGenVariables vars = Main.incidents.getFireGenVariables();
        MessageEmbed content = this.createContent(incident, vars);

        event.replyEmbeds(content)
                .setComponents(
                        ActionRow.of(
                                Button.primary("firegenuser-" + event.getUser().getIdLong() + "-refreshdetails-" + incident.getId(), "Refresh")
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
            if (incident != null) {
                message = createContent(incident, Main.incidents.getFireGenVariables());
            }

            event.editMessageEmbeds(message)
                    .setComponents(
                            ActionRow.of(
                                    Button.secondary("firegenuser-" + event.getUser().getIdLong() + "-refreshdetails-" + incidentId, "Refresh (Wait 5s)").asDisabled()
                            )
                    )
                    .complete().editOriginalComponents(
                            ActionRow.of(
                                    Button.primary("firegenuser-" + event.getUser().getIdLong() + "-refreshdetails-" + incidentId, "Refresh").asEnabled()
                            )
                    ).completeAfter(5, TimeUnit.SECONDS);
            ;
        } catch (Exception exception) {
            DiscordMessages.error(event, "An error occurred processing your button press", exception);
        }
    }

    private MessageEmbed createContent(Incident iIncident, FireGenVariables vars) {
        IncidentImpl incident = (IncidentImpl) iIncident;
        String message = "**Title** " + f(()->incident.getType().getSelectedName(), ">NEW<") +
                "\n**Time** " + f(()->incident.getTime().formatDateAndTime(vars, " @ ")) +
                "\n**Units** " + f(() -> incident.getUnitAssignments().stream()
                .map(UnitAssignment::getUnit)
                .map(Unit::getShorthand)
                .collect(Collectors.joining(" "))) +
                "\n**Location** " + f(()->incident.getLocation().format() + (incident.getLocation().isSet() ? " (type: " + incident.getLocation().getType().name() + ")" : "")) +
                "\n**Incident Number** " + f(incident::getFormattedId, "<none assigned>") +
                "\n**Status** " + f(()->incident.getStatus().name(), "UNKNOWN") + " (" + f(()->incident.getPublished().name()) + ")"
                                + " with " + f(()->String.valueOf(incident.getContributors().size()), "0") + " contributor(s)" +
                "\n**Notes** " + f(() -> incident.getNarrative().stream()
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

    private String f(Supplier<String> returned) {
        return this.f(returned, "");
    }

    private String f(Supplier<String> returned, String def) {
        try {
            return returned.get();
        } catch (Exception exception) {
            return def;
        }
    }
}
