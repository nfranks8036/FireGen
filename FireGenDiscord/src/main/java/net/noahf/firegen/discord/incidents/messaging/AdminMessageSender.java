package net.noahf.firegen.discord.incidents.messaging;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.MessageTopLevelComponent;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.noahf.firegen.api.incidents.IncidentPublishedStatus;
import net.noahf.firegen.api.incidents.units.Unit;
import net.noahf.firegen.discord.Main;
import net.noahf.firegen.discord.incidents.structure.*;
import net.noahf.firegen.discord.incidents.structure.location.IncidentLocationImpl;
import net.noahf.firegen.discord.utilities.Log;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

public class AdminMessageSender extends MessageSender {

    public AdminMessageSender(IncidentMessagingService service, IncidentImpl incident) {
        super(service, incident);

        super.setComponents(new ArrayList<>(List.of(
                // components following are the button row that are used in the admin channel
                // id should be in the format of 'firegen-<incident ID>-<command>-<additional info>'
                ActionRow.of(
                        Button.secondary("firegen-disabled-status", "Status:").asDisabled(),
                        Button.danger(super.getIncident().createInteractionIdString("status"), "Close Incident"),
                        Button.danger(super.getIncident().createInteractionIdString("publish"), "Publish")
                ),
                ActionRow.of(
                        Button.secondary("firegen-disabled-incident1", "Edit:").asDisabled(),
                        Button.primary(super.getIncident().createInteractionIdString("editmode"), "Edit Mode"),
                        Button.primary(super.getIncident().createInteractionIdString("datetime"), "Date/Time")
                ),
                ActionRow.of(
                        Button.secondary("firegen-disabled-incident2", "Edit:").asDisabled(),
                        Button.primary(super.getIncident().createInteractionIdString("location"), "Location"),
                        Button.primary(super.getIncident().createInteractionIdString("units"), "Units")
                ),
                ActionRow.of(
                        Button.secondary("firegen-disabled-misc", "Misc:").asDisabled(),
                        Button.primary(super.getIncident().createInteractionIdString("preview"), "Preview")
                ),
                ActionRow.of(
                        Button.secondary("firegen-disabled-narrative", "Narrative:").asDisabled(),
                        Button.success(super.getIncident().createInteractionIdString("addnarrative"), "Add"),
                        Button.danger(super.getIncident().createInteractionIdString("hidenarrative"), "Hide")
                )
        )));
    }

    @Override
    public void sendInitial() {
        if (!super.getMessages().isEmpty()) {
            return;
        }

        // send a starting message to the admin channels, this will be quickly changed by the following edit THOUGH
        // the content will remain
        String contents = "New incident " +
                super.getIncident().getType().getSelectedName() + " created by " +
                super.getIncident().getContributors().getFirst();
        for (TextChannel channel : Main.adminChannels) {
            try {
                if (channel == null) {
                    Log.warn("ADMIN - Can't send a message here. This channel does not exist!"); continue;
                }

                Message message = channel.sendMessage(contents)
                        .setComponents(super.getComponents())
                        .complete();

                message.createThreadChannel("Incident " +
                        super.getIncident().getFormattedId() + " Discussion").complete();

                super.getMessages().add(message);
            } catch (Exception exception) {
                Log.error("ADMIN - Can't send message to " + (channel != null ? channel.getName() : null)
                        + ": " + exception, exception);
            }
        }
    }

    @Override
    public void sendEdited() {
        if (super.getMessages().isEmpty()) {
            throw new IllegalStateException("Expected messages to exist, found none.");
        }

        IncidentImpl incident = super.getIncident();

        // edit the admin messages with an updated admin panel
        MessageEmbed[] adminMsg = this.getAdminEmbed();
        List<MessageTopLevelComponent> buttons = super.getComponents();
        if (!incident.getStatus().getAttributes().isInProgress()) {
            buttons = new ArrayList<>(List.of(ActionRow.of(
                    Button.secondary("firegen-disabled-status", "Status:").asDisabled(),
                    Button.success("firegen-" + incident.getId() + "-status", "Re-open Incident")
            )));
        }

        for (Message message : super.getMessages()) {
            try {
                // add or remove the components if the status requires it
                message.editMessageComponents(buttons).queue();

                message.editMessageEmbeds(adminMsg).queue(null, (t) -> {
                    Log.warn("Admin message in #" + message.getChannel().getName() + " in " +
                            message.getGuild().getName() + " does not exist. Removing from the list " +
                            "(possibly deleted by staff).", t);
                    super.getMessages().remove(message);
                });
            } catch (Exception exception) {
                Log.error("Can't edit admin message: " + exception, exception);
            }
        }
    }

    public MessageEmbed[] getAdminEmbed() {
        IncidentImpl incident = super.getIncident();

        List<String> log = super.getService().getNarrativeFormatted(incident, true);
        IncidentStatusImpl status = (IncidentStatusImpl) incident.getStatus();
        IncidentTypeImpl type = (IncidentTypeImpl) incident.getType();
        long time = incident.getTime().getUnix();
        IncidentLocationImpl location = (IncidentLocationImpl) incident.getLocation();
        ReceiveMessageSender receiver = super.getService().get(ReceiveMessageSender.class);

        MessageEmbed adminOverview = new EmbedBuilder()
                .setTitle("ADMIN OVERVIEW")
                .setDescription("Incident `" + incident.getFormattedId() + "`"
                        + "\nStatus: " + status.getEmojisFormattedCombined()
                        + "\nMessages (" + receiver.getMessages().size() + "): " +
                        String.join(" , ", receiver.getMessages().stream().map(msg ->
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
        MessageEmbed respondingUnits = new EmbedBuilder()
                .setTitle("Responding Units (" + incident.getAttachedUnits().size() + ")")
                .setDescription(this.getUnitsFormatted())
                .setColor(new Color(255, 94, 94))
                .build();
        MessageEmbed incidentLog = new EmbedBuilder()
                .setTitle("Incident Log (" + log.size() + ")")
                .setDescription(!log.isEmpty() ? String.join("\n", log) : "None")
                .setColor(new Color(255, 94, 94))
                .build();
        return new MessageEmbed[]{adminOverview, respondingUnits, incidentLog};
    }

    public String getUnitsFormatted() {
        IncidentImpl incident = super.getIncident();
        StringJoiner respondingUnitsJoiner = new StringJoiner("\n");
        AssignmentStatus current = null;

        for (Map.Entry<Unit, AssignmentStatus> entry : incident.getSortedUnits().entrySet()) {
            UnitImpl agency = (UnitImpl) entry.getKey();
            AssignmentStatus status = entry.getValue();

            if (current == null || !current.equals(status)) {
                respondingUnitsJoiner.add("- " + (
                        status.getEmoji() != null ? status.getEmoji().getFormatted() + " " : ""
                ) + status.getName());
            }

            respondingUnitsJoiner.add((current == null ? "  " : "") + "  - " +
                    (agency.getEmoji() != null ? agency.getEmoji().getFormatted() + " " : "") +
                    "**" + agency.getLonghand().toUpperCase() + "**"
                    + " (`" + agency.getShorthand() + "`)"
            );

            current = status;
        }
        return respondingUnitsJoiner.toString().isBlank() ? "None"
                :  respondingUnitsJoiner.toString().substring(
                0, Math.min(1024, respondingUnitsJoiner.toString().length())
        );
    }


    @Override
    public void onPublishEvent(IncidentPublishedStatus status) {
        this.getComponents().set(0, ActionRow.of(
                Button.secondary("firegen-disabled-status", "Status:").asDisabled(),
                Button.danger(super.getIncident().createInteractionIdString("status"), "Close Incident"),
                Button.danger(super.getIncident().createInteractionIdString("publish"), super.getButtonText(status))
        ));
    }
}
