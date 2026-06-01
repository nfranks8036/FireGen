package net.noahf.firegen.discord.incidents.messaging;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.noahf.firegen.api.incidents.IncidentPublishedStatus;
import net.noahf.firegen.api.incidents.location.IncidentLocation;
import net.noahf.firegen.api.incidents.units.Agency;
import net.noahf.firegen.discord.Main;
import net.noahf.firegen.discord.incidents.structure.*;
import net.noahf.firegen.discord.incidents.structure.location.IncidentLocationImpl;
import net.noahf.firegen.discord.utilities.Log;

import java.util.List;
import java.util.Map;
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

        IncidentImpl incident = super.getIncident();
        String startingMessage = "New Call- " + incident.getType().getSelectedName();

        IncidentLocation location = incident.getLocation();
        String formattedLocation = location.format();
        if (location.isSet() && !formattedLocation.isBlank()) {
            startingMessage = startingMessage + "\nWhere- " + formattedLocation;
        }

        List<Agency> attachedAgencies = incident.getAttachedAgencies();
        if (!attachedAgencies.isEmpty()) {
            startingMessage = startingMessage + "\n" +
                    "Who- " + String.join(", ",
                    attachedAgencies.stream().map(Agency::getShorthand).toList()
                    );
        }

        startingMessage = startingMessage + "\nWhen- <t:" + incident.getTime().getUnix() + ":t>";

        // send a starting message to the subscribed channels, this will be quickly changed by the following edit
        for (TextChannel channel : Main.receiveChannels) {
            try {
                if (channel == null) {
                    Log.warn("RECEIVE - Can't send a message here. This channel does not exist!"); continue;
                }

                Message message = channel.sendMessage(startingMessage).complete();
                super.getMessages().add(message);
            } catch (Exception exception) {
                Log.error("RECEIVE - Can't send message to " + (channel != null ? channel.getName() : null) +
                        ": " + exception, exception);
            }
        }

        this.sendEdited();
    }

    @Override
    public void sendEdited() {
        String fullMessage = this.getReceivingString();
        for (Message message : super.getMessages()) {
            try {
                message.editMessage(fullMessage).queue(null, (t) -> {
                    Log.warn("Message does not exist. Removing from the list (possibly deleted by staff).", t);
                    super.getMessages().remove(message);
                });
            } catch (Exception exception) {
                Log.error("Can't edit message: " + exception, exception);
            }
        }
    }

    public String getReceivingString() {
        IncidentImpl incident = super.getIncident();

        List<String> log = super.getService().getNarrativeFormatted(incident, false);

        IncidentStatusImpl status = (IncidentStatusImpl) incident.getStatus();
        IncidentTimeImpl time = (IncidentTimeImpl) incident.getTime();
        IncidentLocationImpl location = (IncidentLocationImpl) incident.getLocation();

        return String.format(
                """
                        # %s %s
                        [`%s` @ `%s` // <t:%d:R>]
                        
                        **Responding:** %s
                        **%s:** %s""" +
                        (!log.isEmpty() ? "\n\n**Narrative:**\n%s" : ""),
                status.getEmojisFormattedCombined(),
                incident.getType().getSelectedName(),
                time.formatDate(Main.incidents.getFireGenVariables()),
                time.formatTimeShort(Main.incidents.getFireGenVariables()),
                time.getUnix(),
                this.getAgenciesFormatted(),
                location.getType().getPrefix(),
                location.format(),
                !log.isEmpty() ? String.join("\n", log) : "None"
        );
    }

    private String getAgenciesFormatted() {
        IncidentImpl incident = super.getIncident();
        StringJoiner joiner = new StringJoiner(", ");

        for (Map.Entry<Agency, AssignmentStatus> entry : incident.getSortedAgencies().entrySet()) {
            Agency agency = entry.getKey();
            AssignmentStatus status = entry.getValue();

            String returned;
            if (incident.getStatus().getAttributes().isInProgress()) {
                returned = ((AgencyImpl) agency).getFormattedStatus(status);
            } else {
                // we are not going to show the statuses when an incident is closed
                // the idea being that all of them are going to be "CLEAR" anyways
                returned = agency.getFormatted();
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
