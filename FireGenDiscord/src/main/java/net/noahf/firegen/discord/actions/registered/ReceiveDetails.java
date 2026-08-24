package net.noahf.firegen.discord.actions.registered;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.noahf.firegen.api.incidents.units.Agency;
import net.noahf.firegen.api.incidents.units.Unit;
import net.noahf.firegen.api.incidents.units.UnitAssignment;
import net.noahf.firegen.discord.actions.ActionsContext;
import net.noahf.firegen.discord.actions.ButtonAction;
import net.noahf.firegen.discord.incidents.structure.IncidentImpl;

import java.awt.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ReceiveDetails implements ButtonAction {

    @Override
    public String getName() {
        return "details";
    }

    @Override
    public void execute(ActionsContext ctx, ButtonInteractionEvent event) {
        IncidentImpl incident = (IncidentImpl) ctx.getIncident();
        String message =
                incident.getLocation().format("\n", null).toUpperCase() + "\n" +
                        "\n**Title** " + f(()->incident.getType().getSelectedName(), ">NEW<") +
                        "\n**Time** " + f(()->incident.getTime().formatDateAndTime(ctx.getConfig().getFireGenVariables(), " @ ")) +
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
                        .map(e -> "`" + ctx.getConfig().getFireGenVariables().formatTime(e.getTime(), false) + "` "
                                + e.getEntry()
                        )
                        .collect(Collectors.joining(" ")));
        Color color = switch (incident.getStatus()) {
            case ACTIVE -> new Color(50, 255, 50);
            case CLOSED, CLOSED_TIMED_OUT -> new Color(114, 114, 114);
            case PENDING -> new Color(94, 175, 255);
        };
        MessageEmbed embed = new EmbedBuilder()
                .setTitle(incident.getType().getSelectedName())
                .setDescription(message)
                .setColor(color)
                .build();
        event.replyEmbeds(embed)
                .setEphemeral(true)
                .setComponents(
                        ActionRow.of(
                                Button.primary("firegenuser-" + event.getUser().getIdLong() + "-refreshdetails-" + incident.getId() + "-" + 0, "Refresh")
                        )
                )
                .queue();
    }

    private String f(Supplier<String> returned) {
        return f(returned, "");
    }

    private String f(Supplier<String> returned, String def) {
        try {
            return returned.get();
        } catch (Exception exception) {
            return def;
        }
    }
}
