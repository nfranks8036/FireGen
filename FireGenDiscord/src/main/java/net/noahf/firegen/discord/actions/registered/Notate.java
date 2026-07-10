package net.noahf.firegen.discord.actions.registered;

import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import net.dv8tion.jda.api.modals.Modal;
import net.noahf.firegen.api.Contributor;
import net.noahf.firegen.api.incidents.Incident;
import net.noahf.firegen.discord.actions.ActionsContext;
import net.noahf.firegen.discord.actions.ButtonAction;
import net.noahf.firegen.discord.actions.ModalAction;
import net.noahf.firegen.discord.bot.DiscordMessages;
import net.noahf.firegen.discord.incidents.structure.IncidentImpl;
import net.noahf.firegen.discord.incidents.structure.IncidentLogEntryImpl;
import net.noahf.firegen.discord.users.Permission;
import net.noahf.firegen.discord.utilities.MessageStatus;

import static net.noahf.firegen.discord.actions.registered.AddNarrative.MAX_NARRATIVE_LENGTH;
import static net.noahf.firegen.discord.actions.registered.AddNarrative.MIN_NARRATIVE_LENGTH;

public class Notate implements ButtonAction, ModalAction {

    /**
     * Represents the text input field in the modal for the add narrative
     */
    private static final TextInput TEXT_INPUT =
            TextInput.create("text", TextInputStyle.SHORT)
                    .setRequiredRange(MIN_NARRATIVE_LENGTH, MAX_NARRATIVE_LENGTH)
                    .setRequired(true)
                    .setPlaceholder("Add note text here...")
                    .build();

    /**
     * The name of the command
     */
    @Override
    public String getName() {
        return "notate";
    }

    /**
     * The initial button press on the admin dashboard.
     * This opens the modal for the user to type in their note.
     */
    @Override
    public void execute(ActionsContext ctx, ButtonInteractionEvent event) {
        this.ensureIncidentOpen(event, ctx.getIncident());

        Modal modal = Modal.create(this.callbackId(ctx), "Add Note to " + ctx.getIncident().getFormattedId())
                .addComponents(Label.of(
                        "Note Text",
                        "Will be automatically converted to uppercase.",
                        TEXT_INPUT
                ))
                .build();

        event.replyModal(modal).queue();
    }

    /**
     * This event occurs when the user presses the complete button on the modal.
     */
    @Override
    public void execute(ActionsContext ctx, ModalInteractionEvent event) {
        event.deferReply().setEphemeral(true).queue();

        if (!this.checkUserPermission(event.getUser(), Permission.NARRATIVE_ADD)) {
            DiscordMessages.error(event, "You don't have permission to add text to the notes.");
            return;
        }

        IncidentImpl incident = (IncidentImpl) ctx.getIncident();

        this.ensureIncidentOpen(event, incident);

        ModalMapping textMapping = event.getValue("text");
        if (textMapping == null) {
            DiscordMessages.error(event, "You must input some text to add to the note!");
            return;
        }

        MessageStatus status = this.onSubmit(incident, event, textMapping.getAsString());
        DiscordMessages.noMessage(event, status);
    }

    public MessageStatus onSubmit(Incident incident, IReplyCallback event, String note) {
        if (note.length() < MIN_NARRATIVE_LENGTH) {
            DiscordMessages.error(event, "Your note is too short! (" + note.length() + " < " + MIN_NARRATIVE_LENGTH + ")");
            return MessageStatus.CONTENT;
        }

        if (note.length() > MAX_NARRATIVE_LENGTH) {
            DiscordMessages.error(event, "Your note is too long! (" + note.length() + " > " + MAX_NARRATIVE_LENGTH + ")");
            return MessageStatus.CONTENT;
        }

        Contributor<User> user = ((IncidentImpl)incident).addContributor(event.getUser());
        incident.addLog(
                user,
                IncidentLogEntryImpl.EntryType.NOTE,
                note
        );

        incident.update();
        return MessageStatus.NONE;
    }

    @Override
    public void execute(ActionsContext ctx, GenericInteractionCreateEvent event) {
        if (event instanceof  ButtonInteractionEvent e) { this.execute(ctx, e); }
        if (event instanceof  ModalInteractionEvent e) { this.execute(ctx, e); }
    }

}
