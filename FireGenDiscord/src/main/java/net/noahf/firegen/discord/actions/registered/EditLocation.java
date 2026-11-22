package net.noahf.firegen.discord.actions.registered;

import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.selections.SelectOption;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import net.dv8tion.jda.api.modals.Modal;
import net.noahf.firegen.api.incidents.Incident;
import net.noahf.firegen.api.incidents.location.IncidentLocation;
import net.noahf.firegen.api.incidents.location.LocationField;
import net.noahf.firegen.api.incidents.location.LocationType;
import net.noahf.firegen.api.incidents.location.LocationVenue;
import net.noahf.firegen.discord.Main;
import net.noahf.firegen.discord.actions.ActionsContext;
import net.noahf.firegen.discord.actions.ButtonAction;
import net.noahf.firegen.discord.actions.ModalAction;
import net.noahf.firegen.discord.actions.StringDropdownAction;
import net.noahf.firegen.discord.bot.DiscordMessages;
import net.noahf.firegen.discord.config.files.ConfigLocationTypes;
import net.noahf.firegen.discord.config.files.ConfigVenues;
import net.noahf.firegen.discord.incidents.structure.IncidentLogEntryImpl;
import net.noahf.firegen.discord.incidents.structure.location.IncidentLocationImpl;
import net.noahf.firegen.discord.services.ServiceResponse;
import net.noahf.firegen.discord.users.FireGenUser;
import net.noahf.firegen.discord.users.Permission;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents the "Location" button in the "Edit:" second row.
 */
public class EditLocation implements ButtonAction, StringDropdownAction, ModalAction {

    /**
     * The name of the command needed to access this class.
     */
    @Override
    public String getName() {
        return "location";
    }

    /**
     * The event that occurs after pressing the 'Location' button in the 'Edit' row. This replies a
     * {@link StringSelectMenu} that allows the user to select the type of location they'd like to update the
     * incident to.
     */
    @Override
    public void execute(ActionsContext ctx, FireGenUser<?> user, ButtonInteractionEvent event) {
        event.deferReply().setEphemeral(true).queue();

        if (!this.checkUserPermission(event.getUser(), Permission.CHANGE_LOCATION)) {
            DiscordMessages.error(event, "You don't have permission to change the incident location.");
            return;
        }

        event.getHook().editOriginal("Choose a new location type")
                .setComponents(ActionRow.of(
                        StringSelectMenu.create(this.callbackId(ctx))
                                .addOptions(Main.config.get(ConfigLocationTypes.class).getSelectOptions())
                                .build()
                ))
                .complete();
    }

    /**
     * The event that occurs after pressing the appropriate {@link EditLocation Location}
     * select option. This event opens a modal for the user to more granularly define the location of the incident.
     */
    @Override
    public void execute(ActionsContext ctx, FireGenUser<?> user, StringSelectInteractionEvent event) {
        this.ensureIncidentOpen(event, ctx.getIncident());

        ConfigLocationTypes types = Main.config.get(ConfigLocationTypes.class);

        // get(0) because only one option is allowed, so there should never be >0
        String selected = event.getInteraction().getSelectedOptions().getFirst().getValue();
        LocationType type = types.fromId(selected);

        if (type == null) {
            DiscordMessages.error(event, "Unable to find location type by '" + selected + "'");
            return;
        }

        event.getHook().deleteOriginal().queue();

        Modal modal = Modal.create(
                        this.callbackId(ctx, type.getId()),
                        DiscordMessages.truncate(
                                "Location of " + ctx.getIncident().getFormattedId() + " - " + type.getTitle(),
                                45,
                                "..."
                        )
                )
                .addComponents(this.getLocationModalFields(type, ctx.getIncident()))
                .build();

        event.replyModal(modal).queue();
    }

    /**
     * The event that occurs after submitting the modal and validating that the information is valid.
     */
    @Override
    public void execute(ActionsContext ctx, FireGenUser<?> user, ModalInteractionEvent event) {
        event.deferReply().setEphemeral(true).queue();

        net.noahf.firegen.api.incidents.Incident incident = ctx.getIncident();
        LocationType type = Main.config.get(ConfigLocationTypes.class).fromId(ctx.getParameters().getFirst());

        if (type.getId().equalsIgnoreCase("CUSTOM") && !this.checkUserPermission(event.getUser(), Permission.USE_CUSTOM_LOCATION)) {
            DiscordMessages.error(event, "You don't have permission to set custom locations.");
            return;
        }

        this.ensureIncidentOpen(event, incident);

        // ------- [ GET VENUE IF SET ] --------
        ModalMapping venueMapping = event.getValue("venue");
        LocationVenue venue = ctx.getConfig().get(ConfigVenues.class).fromName(venueMapping != null ? venueMapping.getAsString() : null);

        // ------- [ GET COMMON NAME IF SET ] --------
        ModalMapping commonNameMapping = event.getValue("common-name");
        String commonName = commonNameMapping != null && !commonNameMapping.getAsString().isBlank() ?
                commonNameMapping.getAsString() : null;

        // ------- [ GET CUSTOM LOCATION DATA IF SET ] --------
        List<String> data = this.getData(event);

        // ------- [ COMPLETE AND PACKAGE DATA ] -------
        IncidentLocation location = new IncidentLocationImpl(data, type, commonName, venue);

        ServiceResponse<?> response = this.onSubmit(incident, user, type, location);
        DiscordMessages.noMessage(event, response);
    }

    public ServiceResponse<?> onSubmit(Incident incident, FireGenUser<?> user, LocationType type, IncidentLocation location) {
        if (!user.hasPermission(Permission.CHANGE_LOCATION)) {
            return ServiceResponse.fail(user, "You don't have permission to change the incident location.");
        }

        incident.setLocation(location);

        String narrative = "Location Updated: " + location.format().toUpperCase();

        incident.addContributor(user);
        incident.addLog(user, IncidentLogEntryImpl.EntryType.UPDATE, narrative);
        incident.update();

        return ServiceResponse.successNone();
    }

    /**
     * Gets the actual location data provided by the modals that isn't the {@code common-name} or {@code venue}.
     * @param event the current event
     * @return the list of string data to be interpreted by the {@link IncidentLocationImpl} class at a future date
     */
    @NotNull
    private List<String> getData(ModalInteractionEvent event) {
        List<String> data = new ArrayList<>();
        for (ModalMapping mapping : event.getValues()) {

            // ignore common-name and venue as we just set them above
            if (mapping.getCustomId().equalsIgnoreCase("common-name")
                    || mapping.getCustomId().equalsIgnoreCase("venue")) {
                continue;
            }

            // if the string is blank, then we won't be considering it in our list
            if (mapping.getAsString().isBlank()) {
                continue;
            }

            data.add(mapping.getAsString());
        }

        return data;
    }

    @Override
    public void execute(ActionsContext ctx, FireGenUser user, GenericInteractionCreateEvent event) {
        if (event instanceof  StringSelectInteractionEvent e) { this.execute(ctx, user, e); }
        if (event instanceof  ButtonInteractionEvent e) { this.execute(ctx, user, e); }
        if (event instanceof  ModalInteractionEvent e) { this.execute(ctx, user, e); }
    }



    private Label[] getLocationModalFields(LocationType type, Incident incident) {
        LocationField[] fields = type.getFields();
        Label[] labels = new Label[fields.length];
        for (int i = 0; i < fields.length; i++) {
            labels[i] = this.convertToLabel(fields[i], incident);

            if (fields[i].getId().equalsIgnoreCase("venue")) {
                String venues = "OPTIONAL: " + Main.config.get(ConfigVenues.class).get().stream().map(LocationVenue::getName).collect(Collectors.joining(", "));
                venues = DiscordMessages.truncate(venues, 100, "...");
                labels[i] = labels[i].withDescription(venues);
            }

        }
        return labels;
    }

    private Label convertToLabel(LocationField field, Incident incident) {
        return Label.of(field.getTitle(), DiscordMessages.truncate(field.getDescription(), Label.DESCRIPTION_MAX_LENGTH, "..."),
                TextInput.create(field.getId(), TextInputStyle.SHORT)
                        .setRequired(field.isRequired())
                        .setMinLength(field.getMinLength())
                        .setMaxLength(field.getMaxLength())
                        .setPlaceholder(field.getPlaceholder())
                        .setValue(autofill(field, incident))
                        .build()
        );
    }

    private String autofill(LocationField field, Incident incident) {
        try {
            final int VENUE_INDEX = 99;
            final int COMMON_NAME_INDEX = 98;

            int index = field.getAutofillIndex();
            if (index == VENUE_INDEX) {
                return incident.getLocation().getVenue().getName();
            } else if (index == COMMON_NAME_INDEX) {
                return incident.getLocation().getCommonName();
            }

            return incident.getLocation().getData().get(index);
        } catch (Exception exception) {
            return null;
        }
    }
}
