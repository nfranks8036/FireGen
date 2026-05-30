package net.noahf.firegen.discord.incidents.messaging;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.MessageTopLevelComponent;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.noahf.firegen.api.incidents.IncidentType;
import net.noahf.firegen.api.incidents.location.IncidentLocation;
import net.noahf.firegen.api.incidents.units.Agency;
import net.noahf.firegen.discord.Main;
import net.noahf.firegen.discord.incidents.structure.*;
import net.noahf.firegen.discord.incidents.structure.location.IncidentLocationImpl;
import net.noahf.firegen.discord.utilities.Log;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

public class AdminMessageSender implements MessageSender {

    @Override
    public void sendInitial(MessageContext ctx) {
        if (!ctx.getService().getAdminMessages().isEmpty()) {
            return;
        }

        // send a starting message to the admin channels, this will be quickly changed by the following edit THOUGH
        // the content will remain
        String contents = "New incident " +
                ctx.getIncident().getType().getSelectedName() + " created by " +
                ctx.getIncident().getContributors().getFirst();
        for (TextChannel channel : Main.adminChannels) {
            try {
                if (channel == null) {
                    Log.warn("ADMIN - Can't send a message here. This channel does not exist!"); continue;
                }

                Message message = channel.sendMessage(contents)
                        .setComponents(ctx.getService().getAdminComponents())
                        .complete();

                message.createThreadChannel("Incident " +
                        ctx.getIncident().getFormattedId() + " Discussion").complete();

                ctx.getService().getAdminMessages().add(message);
            } catch (Exception exception) {
                Log.error("ADMIN - Can't send message to " + (channel != null ? channel.getName() : null)
                        + ": " + exception, exception);
            }
        }
    }

    @Override
    public void sendEdited(MessageContext ctx) {
        if (ctx.getService().getAdminMessages().isEmpty()) {
            throw new IllegalStateException("Expected messages to exist, found none.");
        }

        IncidentMessagingService service = ctx.getService();
        IncidentImpl incident = ctx.getIncident();

        // edit the admin messages with an updated admin panel
        MessageEmbed[] adminMsg = this.getAdminEmbed();
        List<MessageTopLevelComponent> buttons = service.getAdminComponents();
        if (!incident.getStatus().getAttributes().isInProgress()) {
            buttons = new ArrayList<>(List.of(ActionRow.of(
                    Button.secondary("firegen-disabled-status", "Status:").asDisabled(),
                    Button.success("firegen-" + incident.getId() + "-status", "Re-open Incident")
            )));
        }

        for (Message message : service.getAdminMessages()) {
            try {
                // add or remove the components if the status requires it
                message.editMessageComponents(buttons).queue();

                message.editMessageEmbeds(adminMsg).queue(null, (t) -> {
                    Log.warn("Admin message in #" + message.getChannel().getName() + " in " +
                            message.getGuild().getName() + " does not exist. Removing from the list " +
                            "(possibly deleted by staff).", t);
                    service.getAdminMessages().remove(message);
                });
            } catch (Exception exception) {
                Log.error("Can't edit admin message: " + exception, exception);
            }
        }
    }

    public MessageEmbed[] getAdminEmbed(IncidentImpl incident, MessageContext ctx) {
        List<String> narrative = incident.getNarrativeFormatted(true);
        IncidentStatusImpl status = (IncidentStatusImpl) incident.getStatus();
        IncidentTypeImpl type = (IncidentTypeImpl) incident.getType();
        long time = incident.getTime().getUnix();
        IncidentLocationImpl location = (IncidentLocationImpl) incident.getLocation();

        MessageEmbed adminOverview = new EmbedBuilder()
                .setTitle("ADMIN OVERVIEW")
                .setDescription("Incident `" + incident.getFormattedId() + "`"
                        + "\nStatus: " + status.getEmojisFormattedCombined()
                        + "\nMessages (" + ctx.getService().getReceivingMessages().size() + "): " + String.join(" , ", ctx.getService().getReceivingMessages().stream().map(msg ->
                        "https://discord.com/channels/" + msg.getGuild().getId() + "/" + msg.getChannel().getId() + "/" + msg.getId()).toList())
                        + "\nContributors (" + incident.getContributors().size() + "): " + String.join(", ", incident.getContributors().stream().map(c -> "<@" + c.getId() + ">").toList())
                )
                .addField("Call Type",
                        type + "\n\n" +
                                "Base: `" + type.getType() + "`\n" +
                                "Tag: `" + type.getTag().getTagName() + "`\n" +
                                "Qualifier: `" + (type.getTag().getQualifier() != null ? type.getTag().getQualifier().getQualifiers().get(type.getQualifierChoice()) : "None") + "`",
                        true
                )
                .addField("Date/Time",
                        "Date: <t:" + time + ":d>\n" +
                                "Time: <t:" + time + ":T>\n" +
                                "Relative: <t:" + time + ":R>\n" +
                                "Unix: `" + time + "`", true
                )
                .addField("Location",
                        "Type: `" + location.getType().name() + "`\n" +
                                "Data: " + String.join(", ", location.getData().stream().map(s -> "`" + s + "`").toList()) + "\n" +
                                "Common Name: `" + (location.getCommonName() != null ? location.getCommonName() : " ") + "`\n" +
                                "Venue: `" + (location.getVenue() != null ? location.getVenue() : " ") + "`\n" +
                                "Formatted: `" + location + "`",
                        true
                )
                .setColor(new Color(255, 94, 94))
                .build();
        MessageEmbed respondingAgencies = new EmbedBuilder()
                .setTitle("Responding Agencies (" + incident.getAttachedAgencies().size() + ")")
                .setDescription(formatAgenciesAdmin())
                .setColor(new Color(255, 94, 94))
                .build();
        MessageEmbed log = new EmbedBuilder()
                .setTitle("Incident Log (" + this.log.size() + ")")
                .setDescription(!narrative.isEmpty() ? String.join("\n", narrative) : "None")
                .setColor(new Color(255, 94, 94))
                .build();
        return new MessageEmbed[]{adminOverview, respondingAgencies, log};
    }

    public String getAgenciesFormatted(IncidentImpl incident) {
        StringJoiner respondingAgenciesJoiner = new StringJoiner("\n");
        AssignmentStatus current = null;
        for (Map.Entry<Agency, AssignmentStatus> entry : incident.getSortedAgencies().entrySet()) {
            AgencyImpl agency = (AgencyImpl) entry.getKey();
            AssignmentStatus status = entry.getValue();

            if (current == null || !current.equals(status)) {
                respondingAgenciesJoiner.add("- " + (
                        status.getEmoji() != null ? status.getEmoji().getFormatted() + " " : ""
                ) + status.getName());
            }

            respondingAgenciesJoiner.add((current == null ? "  " : "") + "  - " +
                    (agency.getEmoji() != null ? agency.getEmoji().getFormatted() + " " : "") +
                    "**" + agency.getLonghand().toUpperCase() + "**"
                    + " (`" + agency.getShorthand() + "`)"
            );

            current = status;
        }
        return respondingAgenciesJoiner.toString().isBlank() ? "None"
                :  respondingAgenciesJoiner.toString().substring(
                0, Math.min(1024, respondingAgenciesJoiner.toString().length())
        );
    }



}
