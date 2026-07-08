package net.noahf.firegen.discord.command.registered;

import lombok.AllArgsConstructor;
import lombok.Getter;
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
import net.noahf.firegen.api.incidents.IncidentLogEntry;
import net.noahf.firegen.api.incidents.location.IncidentLocation;
import net.noahf.firegen.api.incidents.status.IncidentStatus;
import net.noahf.firegen.api.incidents.units.*;
import net.noahf.firegen.api.utilities.FireGenVariables;
import net.noahf.firegen.discord.Main;
import net.noahf.firegen.discord.actions.FireGenAction;
import net.noahf.firegen.discord.actions.registered.*;
import net.noahf.firegen.discord.command.Command;
import net.noahf.firegen.discord.command.CommandFlags;
import net.noahf.firegen.discord.incidents.structure.units.AgencyImpl;
import net.noahf.firegen.discord.incidents.structure.units.AssignmentStatusImpl;
import net.noahf.firegen.discord.incidents.structure.IncidentImpl;
import net.noahf.firegen.discord.incidents.structure.location.IncidentLocationImpl;
import net.noahf.firegen.discord.incidents.structure.units.UnitImpl;
import net.noahf.firegen.discord.users.Permission;
import net.noahf.firegen.discord.utilities.DiscordMessages;
import net.noahf.firegen.discord.utilities.Log;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

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
                        .aliases(new String[]{"ci"})
                        .finish()
        );
    }

    @Override
    public void command(SlashCommandInteractionEvent event) {
        event.deferReply(true).queue();
        if (!Main.users.hasPermission(event.getUser(), Permission.INCIDENT_CREATE)) {
            DiscordMessages.error(event, "You don't have permission to create incidents.");
            return;
        }

        IncidentImpl incident = Main.incidents.createNewIncident();

        // ---------- incident type ----------
        OptionMapping typeOption = event.getOption("type");
        if (typeOption != null) {
            incident.setTypeBySearch(typeOption.getAsString());
        }

        // ---------- incident contributors // begin list ----------
        Contributor<User> contributor = incident.addContributor(event.getUser());
        incident.addLog(contributor, IncidentLogEntry.EntryType.CREATE, "NEW INCIDENT: " + incident.getType().getSelectedName());

        // ---------- incident location ----------
        OptionMapping locationOption = event.getOption("location");
        if (locationOption != null && !Helper.setLocation(incident, event, locationOption)) {
            return;
        }

        // ---------- incident agencies ----------
        OptionMapping unitsOption = event.getOption("units");
        if (unitsOption != null && !Helper.setUnits(incident, event, unitsOption)) {
            return;
        }

        // ---------- incident date and time ----------
        OptionMapping dateOption = event.getOption("date");
        OptionMapping timeOption = event.getOption("time");
        if ((dateOption != null || timeOption != null) && !Helper.setDateTime(incident, event, dateOption, timeOption)) {
            return;
        }

        // ---------- incident initial narrative ----------
        OptionMapping initialNarrativeOption = event.getOption("initial-narrative");
        if (initialNarrativeOption != null && !Helper.setInitialNarrative(incident, event, initialNarrativeOption)) {
            return;
        }

        // ---------- post update for first time to channels ----------
        incident.setStatus(IncidentStatus.PENDING);
        incident.update();

        EditMode.editIncidents.put(event.getUser(), incident);

        DiscordMessages.selfDestruct(event, 5,
                "Created new incident with those details. Check an admin channel for more information."
        );
    }


    @Override
    public List<String> autocomplete(CommandAutoCompleteInteractionEvent event, User user, String commandString, AutoCompleteQuery focused) {
        return switch (focused.getName()) {
            case "type" ->
                // send the list of all motor vehicle crashes
                Main.incidents.listAllIncidentTypesForAutocomplete();

            case "location" ->
                    Main.incidents.listAllPresetLocationsForAutocomplete();

            case "units" ->
                Helper.autocompleteUnits(focused);

            default -> null;
        };
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static class Helper {
        static boolean setLocation(IncidentImpl incident, IReplyCallback event, OptionMapping locationOption) {
            if (!Main.users.hasPermission(event.getUser(), Permission.CHANGE_LOCATION)) {
                DiscordMessages.error(event, "You don't have permission to change the incident location.");
                return false;
            }

            EditLocation action = findAction(EditLocation.class);

            IncidentLocation location = Main.incidents.getPresetByAnyName(locationOption.getAsString());
            if (location == null) {
                if (!Main.users.hasPermission(event.getUser(), Permission.USE_CUSTOM_LOCATION)) {
                    DiscordMessages.error(event, "You don't have permission to set a custom location, " +
                            "so the location was not set. Please use a preset or the buttons.");
                    return false;
                }

                location = new IncidentLocationImpl(List.of(locationOption.getAsString()));
            }

            action.onSubmit(incident, event, location.getType(), location);

            return true;
        }

        static boolean setUnits(IncidentImpl incident, IReplyCallback event, OptionMapping unitsOption) {
            if (!Main.users.hasPermission(event.getUser(), Permission.CHANGE_UNITS)) {
                DiscordMessages.error(event, "You don't have permission to change the incident units.");
                return false;
            }

            EditUnits action = findAction(EditUnits.class);

            // remove whitespace from the agencies string, we don't care if they put a space or not after a comma
            String unitsString = asUnitsString(unitsOption);

            String[] targetsList = unitsString.split(",");

            boolean returned = true;
            Map<AssignmentStatus, EditUnits.UnitsChangeInput> units = new HashMap<>();
            for (String optionItem : targetsList) {
                String targetString = optionItem;

                UnitsTargetType type = switch (targetString.charAt(0)) {
                    case WILDCARD -> UnitsTargetType.WILDCARD;
                    case AGENCY_PREFIX -> UnitsTargetType.AGENCY;
                    case CUSTOM -> UnitsTargetType.CUSTOM;
                    default -> UnitsTargetType.UNIT;
                };

                String statusString = AssignmentStatusImpl.ADD_UNIT.getShortName();

                if (optionItem.contains(":")) {
                    String[] parts = optionItem.split(":");
                    targetString = parts[0];
                    statusString = parts[1];
                }

                // required syntax of command is the shorthand. e.g., "BFD,BVRS:DSP,SUP5:ENR,BPD,VTPD"
                List<Unit> inputUnits = new ArrayList<>();
                switch (type) {
                    case UNIT -> {
                        Unit u = Main.incidents.getUnitByShorthand(targetString);
                        if (u == null) {
                            DiscordMessages.error(event, "No unit exist by the name '" + targetString + "'");
                            returned = false;
                            continue;
                        }
                        inputUnits.add(u);
                    }
                    case AGENCY -> {
                        Agency a = Main.incidents.getAgencyByShorthand(targetString.substring(1));
                        if (a == null) {
                            DiscordMessages.error(event, "No agency exist by the name '" + targetString + "'");
                            returned = false;
                            continue;
                        }
                        inputUnits.addAll(incident.getUnitAssignments().stream()
                                .map(UnitAssignment::getUnit)
                                .filter(unit -> unit.getAgency().equals(a))
                                .toList()
                        );
                    }
                    case WILDCARD ->
                            inputUnits.addAll(incident.getUnitAssignments().stream().map(UnitAssignment::getUnit).toList());
                    case CUSTOM -> {
                        String[] text = targetString.substring(1).split("\\.");
                        Agency agency = Main.incidents.getAgencyByShorthand(text[2]);
                        if (agency == null) {
                            agency = new AgencyImpl(text[2], text[2], text[2], "N/A",
                                    AgencyType.OTHER, null, Integer.MAX_VALUE,
                                    new ArrayList<>()
                            );
                            Log.warn("User " + event.getUser().getName() + " created a temporary agency: " + agency);
                        }

                        Emoji emoji;
                        try {
                            emoji = ((AgencyImpl)agency).getEmoji();
                        } catch (Exception exception) {
                            emoji = null;
                        }
                        UnitImpl custom = new UnitImpl(
                                text[0], text[1].toUpperCase(), text[1],
                                emoji, agency,
                                agency.ordinal(), false,
                                SelectOption.of(text[1].toUpperCase(), text[0])
                                        .withEmoji(emoji)
                        );
                        Main.incidents.getUnits().add(custom);
                        agency.getUnits().add(custom);
                        Log.warn("User " + event.getUser().getName() + " created a custom unit: " + custom);
                        inputUnits.add(custom);
                    }
                }

                if (inputUnits.isEmpty()) {
                    DiscordMessages.error(event, "You attempted to add no units to the call with input '" + unitsString + "'. Did you attempt to " + AGENCY_PREFIX + " an Agency that isn't attached to this call?");
                    returned = false;
                    continue;
                }

                AssignmentStatus s = Main.incidents.getAssignmentStatusByShortName(statusString);
                if (s == null) {
                    s = AssignmentStatusImpl.ADD_UNIT;
                    DiscordMessages.error(event, "No assignment exists with the name '" + statusString + "'," +
                            " defaulting to " + s.getShortName() + " for " + targetString);
                    returned = false;
                }

                EditUnits.UnitsChangeInput input = units.getOrDefault(s, new EditUnits.UnitsChangeInput(new ArrayList<>(), s));
                input.setUnits(inputUnits);
                units.put(s, input);
            }

            for (Map.Entry<AssignmentStatus, EditUnits.UnitsChangeInput> entry : units.entrySet()) {
                action.onSubmit(incident, event, entry.getValue());
            }

            return returned;
        }

        private static @NotNull String asUnitsString(OptionMapping unitsOption) {
            String unitsString = unitsOption.getAsString();
            if (!unitsString.contains(String.valueOf(CUSTOM))) {
                // we don't want to replace whitespace for CUSTOM units because they can actually be used in formatting
                // so we only remove whitespace if we know that this string does not contain a custom unit
                unitsString = unitsString.replaceAll("\\s+", "");
            }

            if (unitsString.endsWith(",") || unitsString.endsWith(":")) {
                unitsString = unitsString.substring(0, unitsString.length() - 1);
            }
            return unitsString;
        }

        static boolean setDateTime(IncidentImpl incident, IReplyCallback event, OptionMapping dateOption, OptionMapping timeOption) {
            if (!Main.users.hasPermission(event.getUser(), Permission.CHANGE_DATE_TIME)) {
                DiscordMessages.error(event, "You don't have permission to change the incident date & time.");
                return false;
            }

            EditDateTime actions = findAction(EditDateTime.class);

            LocalDate date = LocalDate.now(); // default to now if no date provided
            LocalTime time = LocalTime.now(); // default to now if no time provided
            if (timeOption != null) {

                String timeString = timeOption.getAsString();
                try {
                    time = LocalTime.parse(timeString, DateTimeFormatter.ofPattern(TIME_CREATE_FORMAT));
                } catch (DateTimeParseException e) {
                    DiscordMessages.error(event, "Failed to parse your time, expected format '" +
                            TIME_CREATE_FORMAT + "', got '" + timeString + "'", e
                    );
                    return false;
                }
            }

            if (dateOption != null) {
                if (timeOption == null) {
                    DiscordMessages.error(event, "You must set a time if you also select a date.");
                    return false;
                }

                String dateString = dateOption.getAsString();
                try {
                    date = LocalDate.parse(dateString, DateTimeFormatter.ofPattern(DATE_CREATE_FORMAT));
                } catch (DateTimeParseException e) {
                    DiscordMessages.error(event, "Failed to parse your date, expected format '" +
                            DATE_CREATE_FORMAT + "', got '" + dateString + "'", e
                    );
                    return false;
                }
            }

            actions.onSubmit(incident, event, Main.incidents.getFireGenVariables(), date, time);
            return true;
        }

        static boolean setInitialNarrative(IncidentImpl incident, IReplyCallback event, OptionMapping initialNarrative) {
            if (!Main.users.hasPermission(event.getUser(), Permission.NARRATIVE_ADD)) {
                DiscordMessages.error(event, "You don't have permission to add to the narrative.");
                return false;
            }

            if (initialNarrative == null) {
                DiscordMessages.error(event, "Expected narrative to not be null.");
                return false;
            }

            AddNarrative action = findAction(AddNarrative.class);

            String narrative = initialNarrative.getAsString();

            action.onSubmit(incident, event, narrative);
            return true;
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

            String input = focused
                    .getValue()
                    .replaceAll("\\s+", "")
                    .toUpperCase();

            List<String> allTargets = new ArrayList<>(Main.incidents.getUnits().stream()
                    .map(Unit::getShorthand)
                    .map(String::toUpperCase)
                    .toList());
            allTargets.addAll(Main.incidents.getAgencies().stream()
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

            List<String> allStatuses = Main.incidents.getAssignmentStatuses().stream()
                    .map(AssignmentStatus::getShortName)
                    .map(String::toUpperCase)
                    .toList();

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
                String statusPart = currentToken.substring(currentToken.indexOf(':') + 1);

                // if unit invalid, no suggestions
                if (!allTargets.contains(target)) {
                    return List.of();
                }

                return allStatuses.stream()
                        .filter(s -> s.startsWith(statusPart))
                        .map(s -> prefix + target + ":" + s + ",")
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

        @SuppressWarnings("unchecked")
        private static @NotNull <T extends FireGenAction> T findAction(Class<T> clazz) {
            T response = (T) Main.actions.getAction(clazz);
            if (response == null) {
                throw new IllegalStateException("Expected to find action at class '" + clazz + "', instead got 'null'.");
            }
            return response;
        }
    }
}
