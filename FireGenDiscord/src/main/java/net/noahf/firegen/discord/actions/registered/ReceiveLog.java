package net.noahf.firegen.discord.actions.registered;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.noahf.firegen.api.incidents.Incident;
import net.noahf.firegen.api.incidents.units.Agency;
import net.noahf.firegen.api.incidents.units.Unit;
import net.noahf.firegen.api.incidents.units.UnitAssignment;
import net.noahf.firegen.discord.actions.ActionsContext;
import net.noahf.firegen.discord.actions.ButtonAction;
import net.noahf.firegen.discord.incidents.structure.IncidentImpl;

import java.awt.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ReceiveLog implements ButtonAction {

    @Override
    public String getName() {
        return "log";
    }

    @Override
    public void execute(ActionsContext ctx, ButtonInteractionEvent event) {
        IncidentImpl incident = (IncidentImpl) ctx.getIncident();
        MessageEmbed embed = new EmbedBuilder()
                .setTitle("Incident Log (" + incident.getLog().size() + ")")
                .setDescription(String.join("\n", incident.getMessagingService().getNarrativeFormatted(incident, true, false)))
                .setColor(new Color(166, 92, 59))
                .build();
        event.replyEmbeds(embed)
                .setEphemeral(true)
                .setComponents(
                        ActionRow.of(
                                Button.primary("firegenuser-" + event.getUser().getIdLong() + "-refreshdetails-" + incident.getId() + "-" + 1, "Refresh")
                        )
                )
                .queue();
    }
}
