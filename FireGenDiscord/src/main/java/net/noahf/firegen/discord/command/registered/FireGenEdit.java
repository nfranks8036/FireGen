package net.noahf.firegen.discord.command.registered;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.AutoCompleteQuery;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.noahf.firegen.api.Contributor;
import net.noahf.firegen.api.incidents.Incident;
import net.noahf.firegen.api.incidents.status.IncidentStatus;
import net.noahf.firegen.api.incidents.types.IncidentType;
import net.noahf.firegen.discord.Main;
import net.noahf.firegen.discord.actions.FireGenAction;
import net.noahf.firegen.discord.actions.registered.AddNarrative;
import net.noahf.firegen.discord.actions.registered.EditMode;
import net.noahf.firegen.discord.actions.registered.Publish;
import net.noahf.firegen.discord.bot.DiscordMessages;
import net.noahf.firegen.discord.command.Command;
import net.noahf.firegen.discord.command.CommandFlags;
import net.noahf.firegen.discord.config.files.ConfigIncidentTypes;
import net.noahf.firegen.discord.incidents.structure.IncidentImpl;
import net.noahf.firegen.discord.incidents.structure.IncidentLogEntryImpl;
import net.noahf.firegen.discord.services.IncidentService;
import net.noahf.firegen.discord.services.ServiceResponse;
import net.noahf.firegen.discord.users.DiscordUser;
import net.noahf.firegen.discord.users.FireGenUser;
import net.noahf.firegen.discord.users.Permission;
import net.noahf.firegen.discord.utilities.MessageStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

import static net.noahf.firegen.discord.command.registered.CreateIncident.*;
import static net.noahf.firegen.discord.utilities.MessageStatus.CONTENT;

public class FireGenEdit extends Command {

    public FireGenEdit() {
        super("firegen-edit", "Sets details of the FireGen environment. Press 'Edit Mode' on an incident to start editing.",
                CommandFlags.include()
                        .options(new OptionData[]{
                                new OptionData(OptionType.STRING, "units", "The new units for this incident. Note this will only affect inputted agencies.", false, true),
                                new OptionData(OptionType.STRING, "narrative", "Add a new line to the narrative.", false, false)
                                        .setRequiredLength(AddNarrative.MIN_NARRATIVE_LENGTH, AddNarrative.MAX_NARRATIVE_LENGTH),
                                new OptionData(OptionType.STRING, "location", "The new present location for this incident.", false, true),
                                new OptionData(OptionType.STRING, "type", "The new incident type for this incident.", false, true),
                                new OptionData(OptionType.STRING, "time",
                                        "The new time (" + TIME_CREATE_FORMAT +") of the incident",
                                        false, false
                                ),
                                new OptionData(OptionType.STRING, "date",
                                        "The new date (" + DATE_CREATE_FORMAT + ") of the incident, MUST set " +
                                                "a 'time' field if this field is set.",
                                        false, false
                                ),
                                new OptionData(OptionType.STRING, "published", "The published status of the incident. [true or false]", false, true),
                                new OptionData(OptionType.STRING, "status", "The status of the incident, whether it's active, closed, or pending.", false, true),

                        })
                        .aliases(new String[]{"sd", "fge"})
                        .disableAutocompleteAutoFilter(true)
                        .finish());
    }

    @Override
    public void command(SlashCommandInteractionEvent event) {
        event.deferReply().setEphemeral(true).queue();
        DiscordUser user = Main.users.getByDiscordNotNull(event.getUser());
        IncidentImpl incident = (IncidentImpl) EditMode.editIncidents.get(user);

        if (incident == null) {
            DiscordMessages.error(event, "You are not currently editing an incident. " +
                    "Press 'Edit Mode' of the incident of your choice."
            );
            return;
        }

        if (!incident.getStatus().isInProgress()) {
            DiscordMessages.error(event, "This incident is closed and cannot be edited.");
            return;
        }

        IncidentService.IncidentEditRequest request = new IncidentService.IncidentEditRequest(
                event.getOption("type", ">NEW<", OptionMapping::getAsString),
                event.getOption("location", OptionMapping::getAsString),
                event.getOption("units", OptionMapping::getAsString),
                event.getOption("date", OptionMapping::getAsString),
                event.getOption("time", OptionMapping::getAsString),
                event.getOption("narrative", OptionMapping::getAsString),
                event.getOption("published", OptionMapping::getAsString),
                event.getOption("status", OptionMapping::getAsString)
        );

        ServiceResponse<?> response = Main.services.incidents().editIncident(user, incident, request);
        if (!response.success() || !response.isResponseType(IncidentImpl.class)) {
            DiscordMessages.error(event, response.response().toString());
            return;
        }

        DiscordMessages.noMessage(event, response);
    }

    @Override
    public List<String> autocomplete(CommandAutoCompleteInteractionEvent event, User user, String commandString, AutoCompleteQuery focused) {
        return switch (focused.getName()) {
            case "type" -> Main.commands.autocompleteSearch(focused.getValue(), Main.config.get(ConfigIncidentTypes.class).asAutocompleteStrings());
            case "location" -> CreateIncident.autocompleteLocation(focused, true);
            case "units" -> Main.commands.autocompleteSearch(focused.getValue(), CreateIncident.autocompleteUnits(focused));
            case "published" -> List.of("True", "False");
            case "status" -> Arrays.stream(IncidentStatus.values()).map(IncidentStatus::name).toList();
            default -> null;
        };
    }
}
