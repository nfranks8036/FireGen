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
import net.noahf.firegen.api.incidents.IncidentLogEntry;
import net.noahf.firegen.api.utilities.FireGenVariables;
import net.noahf.firegen.discord.Main;
import net.noahf.firegen.discord.actions.ActionsContext;
import net.noahf.firegen.discord.actions.ButtonAction;
import net.noahf.firegen.discord.actions.ModalAction;
import net.noahf.firegen.discord.bot.DiscordMessages;
import net.noahf.firegen.discord.incidents.structure.IncidentImpl;
import net.noahf.firegen.discord.incidents.structure.IncidentLogEntryImpl;
import net.noahf.firegen.discord.services.ServiceResponse;
import net.noahf.firegen.discord.users.FireGenUser;
import net.noahf.firegen.discord.users.Permission;
import net.noahf.firegen.discord.utilities.MessageStatus;

import java.time.LocalDateTime;

/**
 * Represents the "Add" button next to the "Narrative:" row
 */
public class AddNarrative implements ButtonAction, ModalAction {

    public static final int MIN_NARRATIVE_LENGTH = 4;
    public static final int MAX_NARRATIVE_LENGTH = 512;

    /**
     * Represents the text input field in the modal for the add narrative
     */
    private static final TextInput TEXT_INPUT =
            TextInput.create("text", TextInputStyle.SHORT)
            .setRequiredRange(MIN_NARRATIVE_LENGTH, MAX_NARRATIVE_LENGTH)
                .setRequired(true)
                .setPlaceholder("Add narrative text here...")
                .build();

    /**
     * The name of the command
     */
    @Override
    public String getName() {
        return "addnarrative";
    }

    /**
     * The initial button press on the admin dashboard.
     * This opens the modal for the user to type in their narrative
     */
    @Override
    public void execute(ActionsContext ctx, FireGenUser user, ButtonInteractionEvent event) {
        this.ensureIncidentOpen(event, ctx.getIncident());

        Modal modal = Modal.create(this.callbackId(ctx), "Add Narrative to " + ctx.getIncident().getFormattedId())
                .addComponents(Label.of(
                        "Narrative Text",
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
    public void execute(ActionsContext ctx, FireGenUser user, ModalInteractionEvent event) {
        event.deferReply().setEphemeral(true).queue();

        if (!this.checkUserPermission(event.getUser(), Permission.NARRATIVE_ADD)) {
            DiscordMessages.error(event, "You don't have permission to add text to the narrative.");
            return;
        }

        IncidentImpl incident = (IncidentImpl) ctx.getIncident();

        this.ensureIncidentOpen(event, incident);

        ModalMapping textMapping = event.getValue("text");
        if (textMapping == null) {
            DiscordMessages.error(event, "You must input some text to add to the narrative!");
            return;
        }

        ServiceResponse<?> response = this.onSubmit(incident, user, textMapping.getAsString());
        DiscordMessages.noMessage(event, response);
    }

    public ServiceResponse<?> onSubmit(Incident incident, FireGenUser user, String narrative) {
        FireGenVariables vars = Main.config.getFireGenVariables();

        narrative = narrative.toUpperCase();

        if (narrative.length() < MIN_NARRATIVE_LENGTH) {
            return ServiceResponse.fail(user, "Your narrative is too short! (" + narrative.length() + " < " + MIN_NARRATIVE_LENGTH + ")");
        }

        if (narrative.length() > MAX_NARRATIVE_LENGTH) {
            return ServiceResponse.fail(user, "Your narrative is too long! (" + narrative.length() + " > " + MAX_NARRATIVE_LENGTH + ")");
        }

        incident.addContributor(user);
        IncidentLogEntryImpl entry = (IncidentLogEntryImpl)
                IncidentLogEntryImpl.of(user, narrative, IncidentLogEntry.EntryType.NARRATIVE);
        LocalDateTime time = entry.getCustomTimeOrDefault();
        if (time.isAfter(LocalDateTime.now())) {
            return ServiceResponse.fail(user, "The time of the current narrative entry cannot be after the current moment!");
        }

        LocalDateTime incidentTime = incident.getTime().getDateTime();
        // withSecond(1) and withSecond(0) so that if an incident is at 22:39:30 that a time of 22:39:00 works
        if (time.withSecond(1).isBefore(incidentTime.withSecond(0))) {
            return ServiceResponse.fail(user,
                    "The time of the current narrative entry cannot be before the current incident " +
                            "(" + vars.formatTime(incidentTime, true) + ")"
                            + "\n*Tip: Change the incident time by setting the 'Date & Time' field.*"
            );
        }

        incident.addLog(entry);
        incident.update();

        return ServiceResponse.successNone();
    }

    @Override
    public void execute(ActionsContext ctx, FireGenUser user, GenericInteractionCreateEvent event) {
        if (event instanceof  ButtonInteractionEvent e) { this.execute(ctx, user, e); }
        if (event instanceof  ModalInteractionEvent e) { this.execute(ctx, user, e); }
    }
}
