package net.noahf.firegen.discord.incidents.messaging;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.components.MessageTopLevelComponent;
import net.dv8tion.jda.api.entities.Message;
import net.noahf.firegen.api.incidents.IncidentPublishedStatus;
import net.noahf.firegen.discord.incidents.structure.IncidentImpl;

import java.util.ArrayList;
import java.util.List;

@Getter(value = AccessLevel.PROTECTED)
public abstract class MessageSender {

    private final IncidentMessagingService service;
    private final IncidentImpl incident;
    private @Getter(value = AccessLevel.PUBLIC) final List<Message> messages;

    private @Setter(value = AccessLevel.PROTECTED) List<MessageTopLevelComponent> components;

    public MessageSender(IncidentMessagingService service, IncidentImpl incident) {
        this.service = service;
        this.incident = incident;

        this.messages = new ArrayList<>();
        this.components = new ArrayList<>();
    }

    public abstract void sendInitial();

    public abstract void sendEdited();

    public void onPublishEvent(IncidentPublishedStatus newStatus) {
        throw new UnsupportedOperationException("Not supported by this MessageSender.");
    }


    protected String getButtonText(IncidentPublishedStatus status) {
        return switch (status) {
            case PUBLISHED -> "Publish";
            case UNPUBLISHED -> "Unpublish";
            case UNKNOWN -> "Unknown";
        };
    }

}
