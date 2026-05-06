package net.noahf.firegen.discord.command.registered;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.AutoCompleteQuery;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.noahf.firegen.api.Contributor;
import net.noahf.firegen.api.incidents.IncidentType;
import net.noahf.firegen.discord.Main;
import net.noahf.firegen.discord.actions.registered.EditMode;
import net.noahf.firegen.discord.command.Command;
import net.noahf.firegen.discord.command.CommandFlags;
import net.noahf.firegen.discord.incidents.structure.IncidentImpl;
import net.noahf.firegen.discord.incidents.structure.IncidentLogEntryImpl;
import net.noahf.firegen.discord.incidents.structure.IncidentTypeImpl;
import net.noahf.firegen.discord.users.Permission;
import net.noahf.firegen.discord.utilities.DiscordMessages;

import java.util.List;

import static net.noahf.firegen.discord.command.registered.CreateIncident.DATE_CREATE_FORMAT;
import static net.noahf.firegen.discord.command.registered.CreateIncident.TIME_CREATE_FORMAT;

public class SetDetails extends Command {

    public SetDetails() {
        super("set-details", "Sets specific details of an incident. Press 'Edit Type' on an incident to start editing.",
                CommandFlags.include()
                        .options(new OptionData[]{
                                new OptionData(OptionType.STRING, "type", "The new incident type for this incident.", false, true),
                                new OptionData(OptionType.STRING, "location", "The new present location for this incident.", false, true),
                                new OptionData(OptionType.STRING, "agencies", "The new agencies for this incident. Note this will only affect inputted agencies.", false, true),
                                new OptionData(OptionType.STRING, "time",
                                        "The new time (" + TIME_CREATE_FORMAT +") of the incident",
                                        false, false
                                ),
                                new OptionData(OptionType.STRING, "date",
                                        "The new date (" + DATE_CREATE_FORMAT + ") of the incident, MUST set " +
                                                "a 'time' field if this field is set.",
                                        false, false
                                )
                        })
                        .finish());
    }

    @Override
    public void command(SlashCommandInteractionEvent event) {
        IncidentImpl incident = (IncidentImpl) EditMode.editIncidents.get(event.getUser());

        if (incident == null) {
            DiscordMessages.error(event, "You are not currently editing an incident. " +
                    "Press 'Edit Mode' of the incident of your choice."
            );
            return;
        }

        if (!incident.getStatus().getAttributes().isInProgress()) {
            DiscordMessages.error(event, "This incident is closed and cannot be edited.");
            return;
        }

        OptionMapping typeOption = event.getOption("type");
        if (typeOption != null) {
            IncidentType oldType = incident.getType();
            incident.setTypeBySearch(typeOption.getAsString());
            IncidentType newType = incident.getType();

            if (oldType.equals(newType)) {
                DiscordMessages.error(event, "An unknown error occurred, the incident type did not change.");
                return;
            }

            String narrative = "CHANGED INCIDENT TYPE FROM " + oldType.getSelectedName() + " TO " + newType.getSelectedName();

            Contributor<User> user = incident.addContributor(event.getUser());
            incident.addLog(user, IncidentLogEntryImpl.EntryType.UPDATE, narrative);
            incident.update();
        }

        OptionMapping locationOption = event.getOption("location");
        if (locationOption != null && !CreateIncident.Helper.setLocation(incident, event, locationOption)) {
            return;
        }

        OptionMapping agenciesOption = event.getOption("agencies");
        if (agenciesOption != null && !CreateIncident.Helper.setAgencies(incident, event, agenciesOption)) {
            return;
        }

        OptionMapping timeOption = event.getOption("time");
        OptionMapping dateOption = event.getOption("date");
        if ((timeOption != null || dateOption != null) && !CreateIncident.Helper.setDateTime(incident, event, dateOption, timeOption)) {
            return;
        }

        DiscordMessages.selfDestruct(event, 10,
                "Those details have been changed if they were able to. Check the Incident Log for information."
        );
    }

    @Override
    public List<String> autocomplete(CommandAutoCompleteInteractionEvent event, User user, String commandString, AutoCompleteQuery focused) {
        return switch (focused.getName()) {
            case "type" -> Main.incidents.listAllIncidentTypesForAutocomplete();
            case "location" -> Main.incidents.listAllPresetLocationsForAutocomplete();
            case "agencies" -> CreateIncident.Helper.autocompleteAgencies(focused);
            default -> null;
        };
    }
}
