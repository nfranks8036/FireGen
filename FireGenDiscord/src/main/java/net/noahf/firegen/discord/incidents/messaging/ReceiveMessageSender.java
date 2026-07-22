package net.noahf.firegen.discord.incidents.messaging;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.noahf.firegen.api.incidents.IncidentPublishedStatus;
import net.noahf.firegen.api.incidents.location.IncidentLocation;
import net.noahf.firegen.api.incidents.units.AssignmentEvent;
import net.noahf.firegen.api.incidents.units.Unit;
import net.noahf.firegen.api.incidents.units.UnitAssignment;
import net.noahf.firegen.discord.Main;
import net.noahf.firegen.discord.bot.BotManager;
import net.noahf.firegen.discord.bot.DiscordMessages;
import net.noahf.firegen.discord.bot.channels.ChannelRole;
import net.noahf.firegen.discord.config.files.ConfigIncidentStatuses;
import net.noahf.firegen.discord.incidents.structure.IncidentImpl;
import net.noahf.firegen.discord.incidents.structure.IncidentStatusEmoji;
import net.noahf.firegen.discord.incidents.structure.IncidentTimeImpl;
import net.noahf.firegen.discord.incidents.structure.location.IncidentLocationImpl;
import net.noahf.firegen.discord.incidents.structure.units.UnitImpl;
import net.noahf.firegen.discord.utilities.ImmutablePair;
import net.noahf.firegen.discord.utilities.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class ReceiveMessageSender extends MessageSender {

    public ReceiveMessageSender(IncidentMessagingService service, IncidentImpl incident) {
        super(service, incident);
    }

    @Override
    public void sendInitial() {
        if (super.getIncident().getPublished() != IncidentPublishedStatus.PUBLISHED) {
            return;
        }

        if (!super.getMessages().isEmpty()) {
            return;
        }

        BotManager bot = Main.bot;

        IncidentImpl incident = super.getIncident();
        String startingMessage = "New Call- " + incident.getType().getSelectedName();

        IncidentLocation location = incident.getLocation();
        String formattedLocation = location.format();
        if (location.isSet() && !formattedLocation.isBlank()) {
            startingMessage = startingMessage + "\nWhere- " + formattedLocation;
        }

        List<UnitAssignment> attachedUnits = incident.getSortedAssignments();
        if (!attachedUnits.isEmpty()) {
            startingMessage = startingMessage + "\n" +
                    "Who- " + String.join(", ",
                    attachedUnits.stream().map(UnitAssignment::getUnit).map(Unit::getShorthand).toList()
                    );
        }

        startingMessage = startingMessage + "\nWhen- <t:" + incident.getTime().getUnix() + ":t>";

        // send a starting message to the subscribed channels, this will be quickly changed by the following edit
        List<TextChannel> channels = new ArrayList<>(bot.getChannelManager().getFor(ChannelRole.RECEIVER, incident));
        for (TextChannel channel : channels) {
            try {
                if (channel == null) {
                    Log.warn("RECEIVE - Can't send a message here. This channel does not exist! Marked for removal.");
                    continue;
                }

                Message message = channel.sendMessage(startingMessage).complete();
                super.getMessages().add(message);
            } catch (Exception exception) {
                Log.error("RECEIVE - Can't send message to " + (channel != null ? channel.getName() : null) +
                        ": " + exception, exception);
            }
        }
    }

    @Override
    public void sendEdited() {
        ImmutablePair<String, List<MessageEmbed>> response = this.getReceivingFormat();
        for (Message message : super.getMessages()) {
            try {
                message.editMessage(DiscordMessages.truncate(response.getFirstElement(), Message.MAX_CONTENT_LENGTH, "... <message too long to display>"))
                        .setEmbeds(response.getSecondElement())
                        .queue(null, (t) -> {
                            Log.warn("Message does not exist. Removing from the list (possibly deleted by staff).", t);
                            super.getMessages().remove(message);
                        });
            } catch (Exception exception) {
                Log.error("Can't edit message: " + exception, exception);
            }
        }
    }

    public ImmutablePair<String, List<MessageEmbed>> getReceivingFormat() {
        IncidentImpl incident = super.getIncident();

        List<String> log = super.getService().getNarrativeFormatted(incident, false);

        IncidentStatusEmoji status = Main.config.get(ConfigIncidentStatuses.class).asEmoji(incident.getStatus());
        IncidentTimeImpl time = (IncidentTimeImpl) incident.getTime();
        IncidentLocationImpl location = (IncidentLocationImpl) incident.getLocation();

        String stringForm = String.format(
                """
                        # %s %s
                        [`%s` @ `%s` // <t:%d:R>]
                        
                        **Responding:** %s
                        **%s:** %s""" + Character.MAX_VALUE +
                        (!log.isEmpty() ? "\n\n**Narrative:**\n%s" : ""),
                status.getEmojisFormattedCombined(),
                incident.getType().getSelectedName(),
                time.formatDate(Main.config.getFireGenVariables()),
                time.formatTimeShort(Main.config.getFireGenVariables()),
                time.getUnix(),
                this.getUnitsFormatted(),
                location.getType().getPrefix(),
                location.format(),
                !log.isEmpty() ? String.join("\n", log) : "None"
        );
        List<MessageEmbed> embedForm = new ArrayList<>();
        if (stringForm.length() >= Message.MAX_CONTENT_LENGTH) {
            stringForm = stringForm.split(String.valueOf(Character.MAX_VALUE))[0];
            embedForm.add(new EmbedBuilder()
                    .setTitle("Narrative:")
                    .setDescription(DiscordMessages.truncate(String.join("\n", log), MessageEmbed.DESCRIPTION_MAX_LENGTH, "... *unable to show full output!*"))
                    .build());
        }

        stringForm = stringForm.replace(String.valueOf(Character.MAX_VALUE), "");

        return ImmutablePair.of(stringForm, embedForm);
    }

    private String getUnitsFormatted() {
        IncidentImpl incident = super.getIncident();
        StringJoiner joiner = new StringJoiner(", ");

        for (UnitAssignment unitAssignment : incident.getSortedAssignments()) {
            Unit unit = unitAssignment.getUnit();
            AssignmentEvent assignment = unitAssignment.getLatestAssignment();

            String returned;
            if (incident.getStatus().isInProgress()) {
                returned = ((UnitImpl) unit).getFormattedStatus(assignment);
            } else {
                // we are not going to show the statuses when an incident is closed
                // the idea being that all of them are going to be "CLEAR" anyways
                returned = unit.getFormatted();
            }

            joiner.add(returned);
        }

        return joiner.toString();
    }

    @Override
    public void onPublishEvent(IncidentPublishedStatus newStatus) {
        if (newStatus != IncidentPublishedStatus.UNPUBLISHED) {
            return;
        }

        for (Message message : super.getMessages()) {
            message.delete().complete();
        }
        super.getMessages().clear();
    }
}
