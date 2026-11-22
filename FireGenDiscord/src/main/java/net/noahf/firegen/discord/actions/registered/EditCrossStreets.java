package net.noahf.firegen.discord.actions.registered;

import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import net.dv8tion.jda.api.modals.Modal;
import net.noahf.firegen.api.FireGenVariables;
import net.noahf.firegen.api.incidents.Incident;
import net.noahf.firegen.discord.actions.ActionsContext;
import net.noahf.firegen.discord.actions.ButtonAction;
import net.noahf.firegen.discord.actions.ModalAction;
import net.noahf.firegen.discord.bot.DiscordMessages;
import net.noahf.firegen.discord.incidents.structure.IncidentImpl;
import net.noahf.firegen.discord.incidents.structure.IncidentLogEntryImpl;
import net.noahf.firegen.discord.incidents.structure.location.IncidentLocationImpl;
import net.noahf.firegen.discord.services.ServiceResponse;
import net.noahf.firegen.discord.users.FireGenUser;
import net.noahf.firegen.discord.users.Permission;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class EditCrossStreets implements ButtonAction, ModalAction {

    @Override
    public String getName() {
        return "cross";
    }

    @Override
    public void execute(ActionsContext ctx, FireGenUser<?> user, ButtonInteractionEvent event) {
        if (!this.checkUserPermission(event.getUser(), Permission.CHANGE_CROSS_STREETS)) {
            DiscordMessages.error(event, "You don't have permission to change the incident cross-streets.");
            return;
        }

        IncidentImpl incident = (IncidentImpl) ctx.getIncident();
        List<String> cross = incident.getCrossStreets();

        this.ensureIncidentOpen(event, incident);

        TextInput one = TextInput.create("cross1", TextInputStyle.SHORT)
                .setPlaceholder("Ex: Commerce St")
                .setRequired(false)
                .setValue(getAutofill(cross, 0))
                .build();

        TextInput two = TextInput.create("cross2", TextInputStyle.SHORT)
                .setPlaceholder("Ex: Industrial Park Rd")
                .setRequired(false)
                .setValue(getAutofill(cross, 1))
                .build();

        TextInput three = TextInput.create("cross3", TextInputStyle.SHORT)
                .setPlaceholder("Ex: South Main St")
                .setRequired(false)
                .setValue(getAutofill(cross, 2))
                .build();

        Modal modal = Modal.create(this.callbackId(ctx), "Cross-streets of " + incident.getFormattedId())
                .addComponents(
                        Label.of(
                                "First Cross Street",
                                "REQUIRED: The closest street to the incident",
                                one
                        ),
                        Label.of(
                                "Second Cross Street",
                                "OPTIONAL: The second-closest street to the incident",
                                two
                        ),
                        Label.of(
                                "Third Cross Street",
                                "OPTIONAL: The third-closest street to the incident",
                                three
                        )
                ).build();

        event.replyModal(modal).queue();
    }

    private String getAutofill(List<String> list, int index) {
        try {
            return list.get(index);
        } catch (NoSuchElementException | IndexOutOfBoundsException ignored) {
            return null;
        }
    }

    @Override
    public void execute(ActionsContext ctx, FireGenUser<?> user, ModalInteractionEvent event) {
        event.deferReply().setEphemeral(true).queue();

        IncidentImpl incident = (IncidentImpl) ctx.getIncident();

        this.ensureIncidentOpen(event, incident);

        List<String> finalCrossStreets = new ArrayList<>();
        for (ModalMapping modal : event.getValues()) {
            String item = modal.getAsOptionalString();
            if (item == null) continue;
            finalCrossStreets.add(item);
        }

        ServiceResponse<?> response = this.onSubmit(incident, user, finalCrossStreets);
        DiscordMessages.noMessage(event, response);
    }

    public ServiceResponse<?> onSubmit(Incident incident, FireGenUser<?> user, List<String> crossStreets) {
        incident.setCrossStreets(crossStreets);

        String narrative = "Cross-streets Updated: " + incident.formatCrossStreets().toUpperCase();
        if (crossStreets.isEmpty()) {
            narrative = "Cross-streets Cleared";
        }

        incident.addContributor(user);
        incident.addLog(user, IncidentLogEntryImpl.EntryType.UPDATE, narrative);
        incident.update();

        return ServiceResponse.successNone();
    }

    @Override
    public void execute(ActionsContext ctx, FireGenUser<?> user, GenericInteractionCreateEvent event) {
        if (event instanceof  ButtonInteractionEvent e) { this.execute(ctx, user, e); }
        if (event instanceof  ModalInteractionEvent e) { this.execute(ctx, user, e); }
    }

}
