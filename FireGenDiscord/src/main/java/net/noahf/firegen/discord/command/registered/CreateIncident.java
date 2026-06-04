package net.noahf.firegen.discord.command.registered;

import net.dv8tion.jda.api.entities.User;
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
import net.noahf.firegen.api.incidents.status.StatusAttribute;
import net.noahf.firegen.api.incidents.units.AssignmentStatus;
import net.noahf.firegen.api.incidents.units.Unit;
import net.noahf.firegen.api.utilities.FireGenVariables;
import net.noahf.firegen.discord.Main;
import net.noahf.firegen.discord.actions.FireGenAction;
import net.noahf.firegen.discord.actions.registered.AddNarrative;
import net.noahf.firegen.discord.actions.registered.EditUnits;
import net.noahf.firegen.discord.actions.registered.EditDateTime;
import net.noahf.firegen.discord.actions.registered.EditLocation;
import net.noahf.firegen.discord.command.Command;
import net.noahf.firegen.discord.command.CommandFlags;
import net.noahf.firegen.discord.incidents.structure.units.AssignmentStatusImpl;
import net.noahf.firegen.discord.incidents.structure.IncidentImpl;
import net.noahf.firegen.discord.incidents.structure.location.IncidentLocationImpl;
import net.noahf.firegen.discord.users.Permission;
import net.noahf.firegen.discord.utilities.DiscordMessages;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        FireGenVariables vars = Main.incidents.getFireGenVariables();
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
        incident.setStatus(Main.incidents.getStatusesWithAttributes(StatusAttribute.DEFAULT).getFirst());
        incident.update();

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
                    return true;
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
            String unitsString = unitsOption.getAsString().replaceAll("\\s+", "");

            if (unitsString.endsWith(",") || unitsString.endsWith(":")) {
                unitsString = unitsString.substring(0, unitsString.length() - 1);
            }

            String[] unitsList = unitsString.split(",");

            Map<AssignmentStatus, EditUnits.UnitsChangeInput> units = new HashMap<>();
            for (String optionItem : unitsList) {
                String unitString = optionItem;
                String statusString = AssignmentStatusImpl.HIDE_STATUS.getShortName();

                if (optionItem.contains(":")) {
                    String[] parts = optionItem.split(":");
                    unitString = parts[0];
                    statusString = parts[1];
                }

                // required syntax of command is the shorthand. e.g., "BFD,BVRS:DSP,SUP5:ENR,BPD,VTPD"
                Unit a = Main.incidents.getUnitByShorthand(unitString);
                if (a == null) continue;

                AssignmentStatus s = Main.incidents.getAssignmentStatusByShortName(statusString);

                EditUnits.UnitsChangeInput input = units.getOrDefault(s, new EditUnits.UnitsChangeInput(new ArrayList<>(), s));
                List<Unit> inputUnits = input.getUnits();
                inputUnits.add(a);
                input.setUnits(inputUnits);
                units.put(s, input);
            }

            for (Map.Entry<AssignmentStatus, EditUnits.UnitsChangeInput> entry : units.entrySet()) {
                action.onSubmit(incident, event, entry.getValue());
            }

            return true;
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

            List<String> allUnits = Main.incidents.getUnits().stream()
                    .map(Unit::getShorthand)
                    .map(String::toUpperCase)
                    .toList();

            if (input.endsWith(",")) {
                return allUnits.stream()
                        .map(s -> input + s)
                        .toList();
            }

            List<String> allStatuses = Main.incidents.getAssignmentStatuses().stream()
                    .map(AssignmentStatus::getShortName)
                    .map(String::toUpperCase)
                    .toList();

            String[] parts = input.split(",");
            List<String> selectedUnits = new ArrayList<>();

            // completed entries except last token
            for (int i = 0; i < parts.length - 1; i++) {
                String token = parts[i].trim();
                if (!token.isEmpty()) {
                    String unit = token.contains(":")
                            ? token.substring(0, token.indexOf(':'))
                            : token;

                    selectedUnits.add(unit);
                }
            }

            String currentToken = parts.length == 0
                    ? ""
                    : parts[parts.length - 1].trim();

            String prefix = selectedUnits.isEmpty()
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

                String unit = currentToken.substring(0, currentToken.indexOf(':'));
                String statusPart = currentToken.substring(currentToken.indexOf(':') + 1);

                // if unit invalid, no suggestions
                if (!allUnits.contains(unit)) {
                    return List.of();
                }

                return allStatuses.stream()
                        .filter(s -> s.startsWith(statusPart))
                        .map(s -> prefix + unit + ":" + s + ",")
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
            if (allUnits.contains(currentToken)
                    && !selectedUnits.contains(currentToken)) {

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
            return allUnits.stream()
                    .filter(a -> !selectedUnits.contains(a))
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
