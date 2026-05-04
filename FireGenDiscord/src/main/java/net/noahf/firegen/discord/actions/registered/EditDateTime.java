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
import net.noahf.firegen.api.utilities.FireGenVariables;
import net.noahf.firegen.discord.actions.ActionsContext;
import net.noahf.firegen.discord.actions.ButtonAction;
import net.noahf.firegen.discord.actions.ModalAction;
import net.noahf.firegen.discord.incidents.structure.IncidentImpl;
import net.noahf.firegen.discord.incidents.structure.IncidentLogEntryImpl;
import net.noahf.firegen.discord.users.Permission;
import net.noahf.firegen.discord.utilities.DiscordMessages;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents the "Date/Time" button in the "Edit:" first row.
 */
public class EditDateTime implements ButtonAction, ModalAction {

    /**
     * The name of the command needed to access this class
     */
    @Override
    public String getName() {
        return "datetime";
    }

    /**
     * The event that occurs after pressing the "Date/Time" button in the "Edit" row. This replies a {@link Modal} in
     * Discord for the user to edit the date/time.
     */
    @Override
    public void execute(ActionsContext ctx, ButtonInteractionEvent event) {
        if (!this.checkUserPermission(event.getUser(), Permission.CHANGE_DATE_TIME)) {
            DiscordMessages.error(event, "You don't have permission to change the date & time of an incident.");
            return;
        }

        IncidentImpl incident = (IncidentImpl) ctx.getIncident();
        FireGenVariables vars = ctx.getManager().getFireGenVariables();

        this.ensureIncidentOpen(event, incident);

        String timeFormat = vars.longTimeFormat();
        String dateFormat = vars.dateFormat();

        // the reason the following date/time fields can't be static is because they require the current incident's
        // date/time to pre-fill the value.
        TextInput time = TextInput.create("time", TextInputStyle.SHORT)
                .setPlaceholder(timeFormat)
                .setRequired(true)
                .setValue(incident.getTime().formatTimeLong(vars))
                .build();

        TextInput date = TextInput.create("date", TextInputStyle.SHORT)
                .setPlaceholder(dateFormat)
                .setRequired(false)
                .setValue(incident.getTime().formatDate(vars))
                .build();

        Modal modal = Modal.create(this.callbackId(ctx), "Date/Time of " + incident.getFormattedId())
                .addComponents(
                        Label.of(
                                "Time of Incident",
                                "In the format of (" + timeFormat + ")",
                                time
                        ),
                        Label.of(
                                "Date of Incident",
                                "In the format of (" + dateFormat + ")",
                                date
                        )
                ).build();

        event.replyModal(modal).queue();
    }

    /**
     * The event that occurs after submitting the modal from the event above.
     */
    @Override
    public void execute(ActionsContext ctx, ModalInteractionEvent event) {
        event.deferReply().setEphemeral(true).queue();

        IncidentImpl incident = (IncidentImpl) ctx.getIncident();
        FireGenVariables vars = ctx.getManager().getFireGenVariables();

        String timeFormat = vars.longTimeFormat();
        String dateFormat = vars.dateFormat();

        ModalMapping timeMapping = event.getValue("time");
        ModalMapping dateMapping = event.getValue("date");

        this.ensureIncidentOpen(event, incident);

        if (timeMapping == null) {
            // we place this condition at the top because it's REQUIRED regardless of what is inputted
            DiscordMessages.error(event, "Expected value 'time' to be set in modal, found none.");
            return;
        }

        // LocalTime is always going to be set per the condition above, LocalDate may not be so we will assume
        //   the date to be today.
        LocalTime time = LocalTime.parse(
                timeMapping.getAsString(),
                DateTimeFormatter.ofPattern(timeFormat)
        );
        LocalDate date = incident.getTime().getDate();

        if (dateMapping != null) {
            // the date mapping is OPTIONAL, if not set it will default to the `toLocalDate` mentioned above
            date = LocalDate.parse(dateMapping.getAsString(), DateTimeFormatter.ofPattern(dateFormat));
        }

        this.onSubmit(incident, event, vars, date, time);
        DiscordMessages.noMessage(event);
    }

    public void onSubmit(Incident incident, IReplyCallback event, FireGenVariables vars, LocalDate date, LocalTime time) {
        incident.getTime().setDate(date, time);

        String narrative = "Changed date & time to " +
                date.format(DateTimeFormatter.ofPattern(vars.dateFormat())) + " @ " +
                time.format(DateTimeFormatter.ofPattern(vars.longTimeFormat()));

        Contributor<User> user = ((IncidentImpl) incident).addContributor(event.getUser());
        incident.addLog(user, IncidentLogEntryImpl.EntryType.UPDATE, narrative);
        incident.update();
    }




    @Override
    public void execute(ActionsContext ctx, GenericInteractionCreateEvent event) {
        if (event instanceof  ButtonInteractionEvent e) { this.execute(ctx, e); }
        if (event instanceof  ModalInteractionEvent e) { this.execute(ctx, e); }
    }
}
