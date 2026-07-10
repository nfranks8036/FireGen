package net.noahf.firegen.discord.incidents.messaging;

import lombok.AccessLevel;
import lombok.Getter;
import net.noahf.firegen.api.incidents.Incident;
import net.noahf.firegen.api.incidents.IncidentLogEntry;
import net.noahf.firegen.discord.incidents.structure.IncidentImpl;
import net.noahf.firegen.discord.incidents.structure.IncidentLogEntryImpl;
import net.noahf.firegen.discord.utilities.Log;
import org.jetbrains.annotations.NotNull;

import java.time.ZoneOffset;
import java.util.*;

public class IncidentMessagingService {

    private final IncidentImpl incident;

    private @Getter(value = AccessLevel.PACKAGE) List<MessageSender> messages;

    public IncidentMessagingService(IncidentImpl incident) {
        this.incident = incident;

        this.messages = new ArrayList<>(
                List.of(
                        new AdminMessageSender(this, incident),
                        new ReceiveMessageSender(this, incident)
                )
        );
    }

    public <T extends MessageSender> void send(Class<T> messageSender) {
        T sender = this.get(messageSender);
        if (sender == null) {
            throw new IllegalArgumentException("Entered class is not a valid/instantiated MessageSender: " + messageSender);
        }

        sender.requestSend();
    }

    public void sendAll() {
        for (MessageSender sender : this.messages) {
            sender.requestSend();
        }
    }

    public <T extends MessageSender> T get(Class<T> messageSender) {
        for (MessageSender ms : this.messages) {
            if (ms.getClass().isAssignableFrom(messageSender)) {
                return messageSender.cast(ms);
            }
        }
        return null;
    }

    public @NotNull List<String> getNarrativeFormatted(Incident incident, boolean asAdmin) {
        if (incident.getLog() == null || incident.getLog().isEmpty()) {
            return new ArrayList<>();
        }

        List<String> response = new LinkedList<>();
        List<IncidentLogEntry> log = new ArrayList<>(incident.getLog()
                .stream()

                // we don't want admin update logs to be included in the narrative for the public necessarily
                .filter(n -> asAdmin || n.getType() == IncidentLogEntry.EntryType.NARRATIVE)

                .sorted((o1, o2) -> {
                    if (asAdmin) {
                        return o1.getTime().compareTo(o2.getTime());
                    }
                    return ((IncidentLogEntryImpl) o1).getCustomTimeOrDefault()
                            .compareTo(((IncidentLogEntryImpl) o2).getCustomTimeOrDefault());
                })
                .toList()
        );
        for (IncidentLogEntry entry : log) {
            IncidentLogEntryImpl entryImpl = (IncidentLogEntryImpl) entry;
            response.add(asAdmin ? entryImpl.formatAdmin() : entryImpl.formatReceiver());
        }

        return response;
    }

    public void notifyPublishChange() {
        for (MessageSender sender : this.messages) {
            sender.onPublishEvent(this.incident.getPublished());
        }
    }

}
