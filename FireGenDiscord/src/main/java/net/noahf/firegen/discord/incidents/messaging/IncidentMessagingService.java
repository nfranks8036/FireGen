package net.noahf.firegen.discord.incidents.messaging;

import lombok.AccessLevel;
import lombok.Getter;
import net.noahf.firegen.api.incidents.Incident;
import net.noahf.firegen.api.incidents.IncidentLogEntry;
import net.noahf.firegen.discord.incidents.structure.IncidentImpl;
import net.noahf.firegen.discord.incidents.structure.IncidentLogEntryImpl;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

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

    public void send(MessageSender sender) {
        if (sender.getMessages().isEmpty()) {
            sender.sendInitial();
        } else {
            sender.sendEdited();
        }
    }

    public <T extends MessageSender> void send(Class<T> messageSender) {
        T sender = this.get(messageSender);
        if (sender == null) {
            throw new IllegalArgumentException("Entered class is not a valid/instantiated MessageSender: " + messageSender);
        }

        this.send(sender);
    }

    public void sendAll() {
        for (MessageSender sender : this.messages) {
            this.send(sender);
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

        List<String> response = new ArrayList<>();
        for (IncidentLogEntry entry : incident.getLog()) {
            if (!asAdmin && entry.getType() != IncidentLogEntryImpl.EntryType.NARRATIVE) {
                // we don't want admin update logs to be included in the narrative for the public necessarily
                continue;
            }
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
