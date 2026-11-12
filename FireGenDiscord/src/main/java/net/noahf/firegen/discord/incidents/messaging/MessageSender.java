package net.noahf.firegen.discord.incidents.messaging;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.components.MessageTopLevelComponent;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.noahf.firegen.api.incidents.IncidentPublishedStatus;
import net.noahf.firegen.discord.incidents.structure.IncidentImpl;
import net.noahf.firegen.discord.utilities.Log;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter(value = AccessLevel.PROTECTED)
public abstract class MessageSender {

    public static final int MILLISECONDS_TO_SEND_AFTER = 1_500;

    private final IncidentMessagingService service;
    private final IncidentImpl incident;
    private final List<TextChannel> channels;
    private @Getter(value = AccessLevel.PUBLIC) final List<Message> messages;

    private @Setter(value = AccessLevel.PROTECTED) List<MessageTopLevelComponent> components;
    private @Nullable SendRequested sendRequested;

    public MessageSender(IncidentMessagingService service, IncidentImpl incident) {
        this.service = service;
        this.incident = incident;

        this.channels = new ArrayList<>();
        this.messages = new ArrayList<>();
        this.components = new ArrayList<>();

        this.sendRequested = null;
    }

    public abstract void sendInitial();

    public abstract void sendEdited();


    public void requestSend() {
        this.requestSend(MILLISECONDS_TO_SEND_AFTER);
    }

    void requestSend(int delay) {
        Runnable runnable = () -> {
            try {
                if (this.messages.isEmpty()) {
                    this.sendInitial();
                }
                this.sendEdited();
            } catch (Exception exception) {
                Log.error("Failed to send messages in message sender", exception);
                throw exception;
            }
        };

        if (delay <= 10) {
            runnable.run();
            return;
        }

        this.sendRequested = SendRequested.createOrIgnore(sendRequested, delay, runnable);
    }


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

    @Override
    public String toString() {
        return "MessageSender{" +
                "incident=" + incident +
                ", channels=" + channels.stream().map(t -> t.getName() + " @ " + t.getId()).collect(Collectors.joining(", ")) +
                ", messages=" + messages.stream().map(m -> m.getId()).collect(Collectors.joining(", ")) +
                ", components=" + components.stream().map(c -> c.getClass().getCanonicalName() + " w/ " + c.getType().toString()).collect(Collectors.joining(", ")) +
                ", sendRequested=" + sendRequested +
                '}';
    }
}
