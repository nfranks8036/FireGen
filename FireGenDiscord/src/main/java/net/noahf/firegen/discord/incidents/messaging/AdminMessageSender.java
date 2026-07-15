package net.noahf.firegen.discord.incidents.messaging;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.MessageTopLevelComponent;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.noahf.firegen.api.incidents.IncidentPublishedStatus;
import net.noahf.firegen.api.incidents.types.IncidentType;
import net.noahf.firegen.api.incidents.types.IncidentTypeTag;
import net.noahf.firegen.api.incidents.units.AssignmentEvent;
import net.noahf.firegen.api.incidents.units.UnitAssignment;
import net.noahf.firegen.discord.Main;
import net.noahf.firegen.discord.bot.BotManager;
import net.noahf.firegen.discord.bot.DiscordMessages;
import net.noahf.firegen.discord.config.files.ConfigIncidentStatuses;
import net.noahf.firegen.discord.incidents.structure.IncidentImpl;
import net.noahf.firegen.discord.incidents.structure.IncidentStatusEmoji;
import net.noahf.firegen.discord.incidents.structure.location.IncidentLocationImpl;
import net.noahf.firegen.discord.incidents.structure.types.IncidentTypeImpl;
import net.noahf.firegen.discord.incidents.structure.units.AssignmentStatusImpl;
import net.noahf.firegen.discord.incidents.structure.units.UnitImpl;
import net.noahf.firegen.discord.utilities.Log;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
                        Button.secondary("firegen-disabled-narrative", "Log:").asDisabled(),
                        Button.success(super.getIncident().createInteractionIdString("addnarrative"), "Add"),
                        Button.primary(super.getIncident().createInteractionIdString("notate"), "Notate"),
                        Button.danger(super.getIncident().createInteractionIdString("hidenarrative"), "Hide")
                )
        )));
    }

    @Override
    public void sendInitial() {
        if (!super.getMessages().isEmpty()) {
            return;
        }

        BotManager bot = Main.bot;

        // send a starting message to the admin channels, this will be quickly changed by the following edit THOUGH
        // the content will remain
        String contents = "New incident " +
                super.getIncident().getType().getSelectedName() + " created by " +
                super.getIncident().getContributors().getFirst();

        List<TextChannel> channels = new ArrayList<>(bot.getAdminChannels());
        boolean shouldRemoveNulls = false;
        for (TextChannel channel : channels) {
            try {
                if (channel == null) {
                    Log.warn("ADMIN - Can't send a message here. This channel does not exist! Marked for removal.");
                    shouldRemoveNulls = true;
                    continue;
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

        if (shouldRemoveNulls) {
            int sizeBefore = bot.getAdminChannels().size();
            bot.getAdminChannels().removeIf(Objects::isNull);

            Log.warn("Removed " + (sizeBefore - bot.getAdminChannels().size()) + " null (non-existent) admin channels.");
        }
    }

    @Override
    public void sendEdited() {
        if (super.getMessages().isEmpty()) {
            throw new IllegalStateException("Expected messages to exist, found none.");
        }

        IncidentImpl incident = super.getIncident();

        // edit the admin messages with an updated admin panel
        List<MessageEmbed> adminMsg = this.getAdminEmbed();
        List<MessageTopLevelComponent> buttons = super.getComponents();
        if (!incident.getStatus().isInProgress()) {
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

    public List<MessageEmbed> getAdminEmbed() {
        List<MessageEmbed> returned = new ArrayList<>();
        IncidentImpl incident = super.getIncident();

        List<String> log = super.getService().getNarrativeFormatted(incident, true);
        IncidentStatusEmoji status = Main.config.get(ConfigIncidentStatuses.class).getEmoji(incident.getStatus());
        IncidentTypeImpl type = (IncidentTypeImpl) incident.getType();
        long time = incident.getTime().getUnix();
        IncidentLocationImpl location = (IncidentLocationImpl) incident.getLocation();
        ReceiveMessageSender receiver = super.getService().get(ReceiveMessageSender.class);

        returned.add(new EmbedBuilder()
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
                                "Qualifier: `" + this.getQualifierChoice(type) + "`",
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
                .build());
        returned.add(new EmbedBuilder()
                .setTitle("Responding Units (" + incident.getUnitAssignments().size() + ")")
                .setDescription(this.getUnitsFormatted())
                .setColor(new Color(255, 94, 94))
                .build());

        String logText = String.join("\n", log);
        returned.add(new EmbedBuilder()
                .setTitle("Incident Log (" + log.size() + ")")
                .setDescription(!log.isEmpty() ? DiscordMessages.truncate(logText, MessageEmbed.DESCRIPTION_MAX_LENGTH, "... *unable to show full output*!") : "None")
                .setColor(new Color(255, 94, 94))
                .build());
        return returned;
    }

    public String getUnitsFormatted() {
        IncidentImpl incident = super.getIncident();
        StringJoiner respondingUnitsJoiner = new StringJoiner("\n");
        AssignmentStatusImpl current = null;

        for (UnitAssignment unitAssignment : incident.getSortedAssignments()) {
            UnitImpl unit = (UnitImpl) unitAssignment.getUnit();
            AssignmentEvent assignment = unitAssignment.getLatestAssignment();
            AssignmentStatusImpl status = (AssignmentStatusImpl) assignment.getStatus();

            if (current == null || !current.equals(status)) {
                respondingUnitsJoiner.add("- " + (
                        status.getEmoji() != null ? status.getEmoji().getFormatted() + " " : ""
                ) + status.getName());
            }

            respondingUnitsJoiner.add((current == null ? "  " : "") + "  - " +
                    (unit.getEmoji() != null ? unit.getEmoji().getFormatted() + " " : "") +
                    "**" + unit.getLonghand().toUpperCase() + "**"
                    + " (`" + unit.getShorthand() + "`)"
            );

            current = status;
        }
        return respondingUnitsJoiner.toString().isBlank() ? "None"
                :  respondingUnitsJoiner.toString().substring(
                0, Math.min(1024, respondingUnitsJoiner.toString().length())
        );
    }

    private String getQualifierChoice(IncidentType type) {
        IncidentTypeTag tag = type.getTag();
        if (tag.getQualifiers() == null) {
            return "None";
        }
        List<String> qualifiers = tag.getQualifiers().getQualifiers();
        if (type.getQualifierChoice() > qualifiers.size() || type.getQualifierChoice() < 0) {
            return "None (OOB)";
        }
        return qualifiers.get(type.getQualifierChoice());
    }


    @Override
    public void onPublishEvent(IncidentPublishedStatus status) {
        this.getComponents().set(0, ActionRow.of(
                Button.secondary("firegen-disabled-status", "Status:").asDisabled(),
                Button.danger(super.getIncident().createInteractionIdString("status"), "Close Incident"),
                Button.danger(super.getIncident().createInteractionIdString("publish"), super.getButtonText(status.opposite()))
        ));
    }
}
