package net.noahf.firegen.discord.actions.registered;

import kotlin.Pair;
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
import net.noahf.firegen.discord.actions.ActionsContext;
import net.noahf.firegen.discord.actions.ButtonAction;
import net.noahf.firegen.discord.actions.ModalAction;
import net.noahf.firegen.discord.bot.DiscordMessages;
import net.noahf.firegen.discord.incidents.structure.IncidentImpl;
import net.noahf.firegen.discord.incidents.structure.IncidentLogEntryImpl;
import net.noahf.firegen.discord.users.Permission;
import net.noahf.firegen.discord.utilities.ImmutablePair;
import net.noahf.firegen.discord.utilities.Log;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents the "Add" button next to the "Narrative:" row
 */
public class AddNarrative implements ButtonAction, ModalAction {

    public static final int MIN_NARRATIVE_LENGTH = 5;
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
    public void execute(ActionsContext ctx, ButtonInteractionEvent event) {
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
    public void execute(ActionsContext ctx, ModalInteractionEvent event) {
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

        this.onSubmit(incident, event, textMapping.getAsString());
        DiscordMessages.noMessage(event, false);
    }

    private final Pattern pattern = Pattern.compile(
            "^(?:D(?<month>\\d{2})(?<day>\\d{2})(?<year>\\d{2}))?T(?<hour>\\d{2})(?<minute>\\d{2})\\s*"
    );

    private ImmutablePair<LocalDateTime, Matcher> extractTime(String text) {
        Matcher matcher = pattern.matcher(text);
        if (matcher.matches()) {
            int hour = Integer.parseInt(matcher.group("hour"));
            int minute = Integer.parseInt(matcher.group("minute"));

            LocalDateTime time = LocalDate.now().atTime(hour, minute);
            if (matcher.group("month") != null) {
                int month = Integer.parseInt(matcher.group("month"));
                int day = Integer.parseInt(matcher.group("day"));
                int year = Integer.parseInt(matcher.group("year"));
                time = LocalDate.of(year, month, day).atTime(hour, minute);
            }

            return ImmutablePair.of(time, matcher);
        }
        return ImmutablePair.of(LocalDateTime.now(), matcher);
    }

    public void onSubmit(Incident incident, IReplyCallback event, String narrative) {
        narrative = narrative.toUpperCase();
        LocalDateTime time = LocalDateTime.now();
        if (narrative.startsWith("T") || narrative.startsWith("D")) {
            ImmutablePair<LocalDateTime, Matcher> pair = this.extractTime(narrative);
            time = pair.getFirstElement() != null ? pair.getFirstElement() : time;
            narrative = narrative.substring(pair.getSecondElement().end()).stripLeading();
            // D070926T1826
            // T1826
        }

        if (narrative.length() < MIN_NARRATIVE_LENGTH) {
            DiscordMessages.error(event, "Your narrative is too short! (" + narrative.length() + " < " + MIN_NARRATIVE_LENGTH + ")");
            return;
        }

        if (narrative.length() > MAX_NARRATIVE_LENGTH) {
            DiscordMessages.error(event, "Your narrative is too long! (" + narrative.length() + " > " + MAX_NARRATIVE_LENGTH + ")");
            return;
        }

        Contributor<User> user = ((IncidentImpl)incident).addContributor(event.getUser());
        ((IncidentImpl)incident).addLog(
                time, user, IncidentLogEntry.EntryType.NARRATIVE, narrative
        );

        incident.update();
    }

    @Override
    public void execute(ActionsContext ctx, GenericInteractionCreateEvent event) {
        if (event instanceof  ButtonInteractionEvent e) { this.execute(ctx, e); }
        if (event instanceof  ModalInteractionEvent e) { this.execute(ctx, e); }
    }
}
