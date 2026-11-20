package net.noahf.firegen.discord.command.registered;

import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.components.selections.SelectOption;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.AutoCompleteQuery;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.noahf.firegen.api.Contributor;
import net.noahf.firegen.api.incidents.Incident;
import net.noahf.firegen.api.incidents.IncidentLogEntry;
import net.noahf.firegen.api.incidents.IncidentPublishedStatus;
import net.noahf.firegen.api.incidents.location.IncidentLocation;
import net.noahf.firegen.api.incidents.location.LocationField;
import net.noahf.firegen.api.incidents.location.LocationType;
import net.noahf.firegen.api.incidents.location.LocationVenue;
import net.noahf.firegen.api.incidents.status.IncidentStatus;
import net.noahf.firegen.api.incidents.units.*;
import net.noahf.firegen.discord.Main;
import net.noahf.firegen.discord.actions.FireGenAction;
import net.noahf.firegen.discord.actions.registered.*;
import net.noahf.firegen.discord.bot.DiscordMessages;
import net.noahf.firegen.discord.command.Command;
import net.noahf.firegen.discord.command.CommandFlags;
import net.noahf.firegen.discord.config.ConfigManager;
import net.noahf.firegen.discord.config.files.*;
import net.noahf.firegen.discord.incidents.structure.IncidentImpl;
import net.noahf.firegen.discord.incidents.structure.location.IncidentLocationImpl;
import net.noahf.firegen.discord.incidents.structure.units.*;
import net.noahf.firegen.discord.services.IncidentService;
import net.noahf.firegen.discord.services.ServiceResponse;
import net.noahf.firegen.discord.users.DiscordUser;
import net.noahf.firegen.discord.users.Permission;
import net.noahf.firegen.discord.utilities.Log;
import net.noahf.firegen.discord.utilities.MessageStatus;
import net.noahf.firegen.discord.utilities.Time;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Stream;
import static net.noahf.firegen.discord.utilities.MessageStatus.CONTENT;

/**
 * Represents the command used to create an incident.
 * {@code /create-incident <type> [date] [time] [location] [agencies]}
 */
public class CreateIncident extends Command {

    /**
     * Represents the {@code date} format that the user must type in order to be parsed correctly.
     * See {@link DateTimeFormatter} for more information.
     */
    public static final String DATE_CREATE_FORMAT = "MM/dd/yyyy";

    /**
     * Represents the {@code time} format that the user must type in order to be parsed correct.
     * See {@link DateTimeFormatter} for more information.
     */
    public static final String TIME_CREATE_FORMAT = "HH:mm";

    public static final char AGENCY_PREFIX = '@';
    public static final char WILDCARD = '*';
    public static final char CUSTOM = '?';

    @AllArgsConstructor
    enum UnitsTargetType {
        UNIT,
        AGENCY,
        WILDCARD,
        CUSTOM;
    }

    public CreateIncident() {
        super("create-incident", "Creates an incident to put in radio activity.",
                CommandFlags.include()
                        .options(new OptionData[]{
                                new OptionData(OptionType.STRING, "type",
                                        "REQUIRED: The type of incident.",
                                        true, true
                                ),
                                new OptionData(OptionType.STRING, "time",
                                        "The time (" + TIME_CREATE_FORMAT +") of the incident, will default" +
                                                " to today if no 'date' field is set.",
                                        false, false
                                ),
                                new OptionData(OptionType.STRING, "date",
                                        "The date (" + DATE_CREATE_FORMAT + ") of the incident, MUST set " +
                                                "a 'time' field if this field is set.",
                                        false, false
                                ),
                                new OptionData(OptionType.STRING, "location",
                                        "The location of the incident.",
                                        false, true
                                ),
                                new OptionData(OptionType.STRING, "units",
                                        "Any units attached, separated with a comma.",
                                        false, true
                                ),
                                new OptionData(OptionType.STRING, "initial-narrative",
                                        "The initial narrative line (NOTE: Only the FIRST line)",
                                        false, true
                                )
                                        .setRequiredLength(AddNarrative.MIN_NARRATIVE_LENGTH, AddNarrative.MAX_NARRATIVE_LENGTH)
                        })
                        .disableAutocompleteAutoFilter(true)
                        .aliases(new String[]{"ci"})
                        .finish()
        );
    }

    @Override
    public void command(SlashCommandInteractionEvent event) {
        event.deferReply(true).queue();
        DiscordUser user = Main.users.getByDiscordNotNull(event.getUser());
        if (!user.hasPermission(Permission.INCIDENT_CREATE)) {
            DiscordMessages.error(event, "You don't have permission to create incidents.");
            return;
        }

        ServiceResponse<?> dateTime = Time.fromInputs(user,
                event.getOption("date", OptionMapping::getAsString),
                event.getOption("time", OptionMapping::getAsString)
        );

        IncidentService.IncidentCreateRequest request = new IncidentService.IncidentCreateRequest(
                event.getOption("type", ">NEW<", OptionMapping::getAsString),
                event.getOption("location", OptionMapping::getAsString),
                event.getOption("units", (m) -> m.getAsString().strip().split(",")),
                dateTime.isResponseType(LocalDateTime.class) ? (LocalDateTime) dateTime.response() : null,
                event.getOption("initial-narrative", OptionMapping::getAsString)
        );

        ServiceResponse<?> response = Main.services.incidents().createIncident(user, request);
        response = ServiceResponse.returnAUnlessBFail(response, dateTime);
        if (!response.success() || !response.isResponseType(IncidentImpl.class)) {
            DiscordMessages.error(event, response.response().toString());
            return;
        }

        DiscordMessages.selfDestruct(event, 5,
                "Created new incident: " + response.response().toString() + "\nCheck an admin channel for more information."
        );
    }


    @Override
    public List<String> autocomplete(CommandAutoCompleteInteractionEvent event, User user, String commandString, AutoCompleteQuery focused) {
        return switch (focused.getName()) {
            case "type" ->
                // send the list of all motor vehicle crashes
                Main.commands.autocompleteSearch(focused.getValue(), Main.config.get(ConfigIncidentTypes.class).asAutocompleteStrings());

            case "location" ->
                    autocompleteLocation(focused, true);

            case "units" ->
                    Main.commands.autocompleteSearch(focused.getValue(), autocompleteUnits(focused));

            default -> null;
        };
    }

    static List<String> autocompleteUnits(AutoCompleteQuery focused) {
        // -------- [ BELOW THIS LINE CONTAINS SOME LLM-WRITTEN OR MODIFIED CODE ] --------
        /*
         * Format now supports:
         *
         * BFD,BVRS,SUP5
         * BFD,BVRS,SUP5:
         * BFD,BVRS,SUP5:DSP
         * BFD,BVRS,SUP5:DSP,
         * BFD,BVRS,SUP5:DSP,BPD
         *
         * Meaning:
         * agency[:status],agency[:status],agency[:status]
         */

        ConfigUnits configUnits = Main.config.get(ConfigUnits.class);
        String input = focused
                .getValue()
                .replaceAll("\\s+", "")
                .toUpperCase();

        List<String> allTargets = new ArrayList<>(configUnits.get().stream()
                .map(Unit::getShorthand)
                .map(String::toUpperCase)
                .toList());
        allTargets.addAll(configUnits.getAgencies().stream()
                .map(Agency::getShorthand)
                .map(String::toUpperCase)
                .map(s -> AGENCY_PREFIX + s)
                .toList()
        );
        allTargets.add(String.valueOf(WILDCARD));

        if (input.endsWith(",")) {
            return allTargets.stream()
                    .map(s -> input + s)
                    .toList();
        }

        List<AssignmentStatus> allStatuses = Main.config.get(ConfigAssignmentStatuses.class).get();

        String[] parts = input.split(",");
        List<String> selectedTargets = new ArrayList<>();

        // completed entries except last token
        for (int i = 0; i < parts.length - 1; i++) {
            String token = parts[i].trim();
            if (!token.isEmpty()) {
                String unit = token.contains(":")
                        ? token.substring(0, token.indexOf(':'))
                        : token;

                selectedTargets.add(unit);
            }
        }

        String currentToken = parts.length == 0
                ? ""
                : parts[parts.length - 1].trim();

        String prefix = selectedTargets.isEmpty()
                ? ""
                : String.join(",", parts).substring(0,
                input.lastIndexOf(currentToken));

        /*
         * -----------------------------------------
         * MODE 1: User is typing a status
         * Example:
         * BFD:
         * BFD:DS
         * -----------------------------------------
         */
        if (currentToken.contains(":")) {

            String target = currentToken.substring(0, currentToken.indexOf(':'));
//                String statusPart = currentToken.substring(currentToken.indexOf(':') + 1);

            // if unit invalid, no suggestions
            if (!allTargets.contains(target)) {
                return List.of();
            }

            String afterColon = currentToken.substring(currentToken.indexOf(':') + 1);
            if (afterColon.contains(">")) {
                String statusPart = afterColon.substring(0, afterColon.indexOf('>'));
                String secondaryPart = afterColon.substring(afterColon.indexOf('>') + 1);

                AssignmentStatus status = allStatuses.stream()
                        .filter(s -> s.getShortName().equalsIgnoreCase(statusPart))
                        .findFirst().orElse(null);
                if (status == null) return List.of();

                return status.getSecondaries().stream()
                        .map(Secondary::getShortName)
                        .map(String::toUpperCase)
                        .filter(s -> s.startsWith(secondaryPart))
                        .map(s -> prefix + target + ":" + status.getShortName().toUpperCase() + ">" + s + ",")
                        .toList();
            }

            String statusPart = currentToken.substring(currentToken.indexOf(':') + 1);
            return allStatuses.stream()
                    .flatMap(s -> {
                        List<String> values = new ArrayList<>(List.of(s.getShortName() + ","));
                        if (s.getSecondaries() != null && !s.getSecondaries().isEmpty()) {
                            values.add(s.getShortName() + ">");
                        }
                        return Stream.of(values.toArray(String[]::new));
                    })
                    .filter(s -> s.startsWith(statusPart))
                    .map(s -> prefix + target + ":" + s)
                    .limit(25)
                    .toList();
        }

        /*
         * -----------------------------------------
         * MODE 2: User finished agency and may want :
         * Example:
         * BFD
         * SUP5
         * -----------------------------------------
         */
        if (allTargets.contains(currentToken)
                && !selectedTargets.contains(currentToken)) {

            List<String> out = new ArrayList<>();

            out.add(prefix + currentToken + ",");
            out.add(prefix + currentToken + ":");

            return out.stream().limit(25).toList();
        }

        /*
         * -----------------------------------------
         * MODE 3: Typing agency normally
         * Example:
         * B
         * BF
         * BFD,B
         * -----------------------------------------
         */
        return allTargets.stream()
                .filter(a -> !selectedTargets.contains(a))
                .filter(a -> a.startsWith(currentToken))
                .map(a -> prefix + a)
                .limit(25)
                .toList();
        // -------- [ ABOVE THIS LINE CONTAINS SOME LLM-WRITTEN OR MODIFIED CODE ] --------
    }

    static List<String> autocompleteLocation(AutoCompleteQuery focused, boolean includeLocCreation) {
        String input = focused.getValue().toUpperCase();
        List<String> returned = new ArrayList<>(Main.config.get(ConfigLocationPresets.class).asAutocompleteStrings());

        if (includeLocCreation) {
            returned.addAll(addLocCreation(input));
        }

        return Main.commands.autocompleteSearch(focused.getValue(), returned);
    }

    private static List<String> addLocCreation(String input) {
        List<String> returned;
        if (input.contains(":")) {

            try {
                String[] split = input.split(":", 2);

                LocationType type = LocationType.valueOf(split[0].toUpperCase().substring(1));
                String fieldData = split.length > 1 ? split[1] : "";

                LocationField[] fields = type.getFields();

                String[] enteredFields = fieldData.isEmpty()
                        ? new String[0]
                        : fieldData.split(";", -1); // keep trailing empty fields

                int currentIndex = enteredFields.length - 1;

                if (currentIndex >= fields.length) {
                    return List.of("<COMPLETE>");
                }

                StringBuilder suggestion = new StringBuilder();
                suggestion.append(type.name()).append(":");

                // Add all completed fields
                for (int i = 0; i < currentIndex; i++) {
                    if (i > 0) {
                        suggestion.append(";");
                    }
                    suggestion.append(enteredFields[i]);
                }

                // Add separator before current field if needed
                if (currentIndex > 0) {
                    suggestion.append(";");
                }

                currentIndex = Math.max(currentIndex, 0);

                // Add placeholder for current field
                suggestion.append("<")
                        .append(fields[currentIndex].getTitle())
                        .append(">");

                return List.of(suggestion.toString());

            } catch (IllegalArgumentException e) {
                return List.of("<INVALID LOCATION TYPE>");
            } catch (Exception e) {
                Log.warn("Error autocompleting location: " + e, e);
                return List.of(DiscordMessages.truncate(
                        "<ERROR: " + e + ">", 100, "...>"
                ));
            }


        } else returned = new ArrayList<>(LocationType.values().stream()
                .map(LocationType::name)
                .map(s -> "$" + s + ":")
                .toList());

        return returned;
    }
}
