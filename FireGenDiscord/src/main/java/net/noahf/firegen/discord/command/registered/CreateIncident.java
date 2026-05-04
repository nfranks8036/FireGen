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
import net.noahf.firegen.api.incidents.units.Agency;
import net.noahf.firegen.api.utilities.FireGenVariables;
import net.noahf.firegen.discord.Main;
import net.noahf.firegen.discord.command.Command;
import net.noahf.firegen.discord.command.CommandFlags;
import net.noahf.firegen.discord.incidents.structure.AssignmentStatus;
import net.noahf.firegen.discord.incidents.structure.IncidentImpl;
import net.noahf.firegen.discord.incidents.structure.location.IncidentLocationImpl;
import net.noahf.firegen.discord.users.Permission;
import net.noahf.firegen.discord.utilities.DiscordMessages;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents the command used to create an incident.
 * {@code /create-incident <type> [date] [time] [location] [agencies]}
 */
public class CreateIncident extends Command {

    /**
     * Represents the {@code date} format that the user must type in order to be parsed correctly.
     * See {@link DateTimeFormatter} for more information.
     */
    private static final String DATE_CREATE_FORMAT = "MM/dd/yyyy";

    /**
     * Represents the {@code time} format that the user must type in order to be parsed correct.
     * See {@link DateTimeFormatter} for more information.
     */
    private static final String TIME_CREATE_FORMAT = "HH:mm";

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
                                new OptionData(OptionType.STRING, "agencies",
                                        "Any agencies attached, separated with a comma.",
                                        false, true
                                )
                        })
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
        if (locationOption != null) {

            IncidentLocation location = Main.incidents.getPresetByAnyName(locationOption.getAsString());
            if (location == null) {
                location = this.setCustomLocation(event, event.getUser(), locationOption.getAsString());
            }

            incident.setLocation(location);
        }

        // ---------- incident agencies ----------
        OptionMapping agenciesOption = event.getOption("agencies");
        if (agenciesOption != null) {
            // remove whitespace from the agencies string, we don't care if they put a space or not after a comma
            String agenciesString = agenciesOption.getAsString().replaceAll("\\s+", "");

            String[] agenciesList = agenciesString.split(",");

            List<Agency> agencies = new ArrayList<>();
            for (String agencyString : agenciesList) {
                // required syntax of command is the shorthand. e.g., "BFD,BVRS,SUP5,BPD,VTPD"
                Agency a = Main.incidents.getAgencyByShorthand(agencyString);
                if (a == null) continue;

                agencies.add(a);
            }
            incident.putAgencies(agencies);
            incident.addLog(contributor, IncidentLogEntry.EntryType.AGENCY,
                    (agencies.size() == 1 ? "Agency" : "Agencies") + " " +
                            agencies.stream().map(Agency::getShorthand).collect(Collectors.joining(", ")) +
                            " added"
                    );
        }

        // ---------- incident date and time ----------
        OptionMapping dateOption = event.getOption("date");
        OptionMapping timeOption = event.getOption("time");
        LocalDate date = LocalDate.now(); // default to now if no date provided
        LocalTime time = LocalTime.now(); // default to now if no time provided
        if (timeOption != null) {

            String timeString = timeOption.getAsString();
            try {
                time = LocalTime.parse(timeString, DateTimeFormatter.ofPattern(TIME_CREATE_FORMAT));
            } catch (DateTimeParseException e) {
                DiscordMessages.error(event,
                        "Failed to parse your time, expected format '" + TIME_CREATE_FORMAT + "', " +
                                "got '" + timeString + "'", e
                );
                return;
            }
        }

        if (dateOption != null) {
            if (timeOption == null) {
                DiscordMessages.error(event, "You must set a time if you also select a date.");
                return;
            }

            String dateString = dateOption.getAsString();
            try {
                date = LocalDate.parse(dateString, DateTimeFormatter.ofPattern(DATE_CREATE_FORMAT));
            } catch (DateTimeParseException e) {
                DiscordMessages.error(event, "Failed to parse your date, expected format '" +
                        DATE_CREATE_FORMAT + "', got '" + dateString + "'", e
                );
                return;
            }
        }

        if (dateOption != null || timeOption != null) {
            incident.addLog(contributor, IncidentLogEntry.EntryType.UPDATE,
                    "Changed date & time to " +
                            date.format(DateTimeFormatter.ofPattern(vars.dateFormat())) + " @ " +
                            time.format(DateTimeFormatter.ofPattern(vars.longTimeFormat()))
            );
        }

        incident.getTime().setDate(date, time);

        // ---------- post update for first time to channels ----------
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

            // -------- [ BELOW THIS LINE CONTAINS SOME LLM-WRITTEN OR MODIFIED CODE ] --------
            case "agencies" -> {
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

                String input = event.getFocusedOption()
                        .getValue()
                        .replaceAll("\\s+", "")
                        .toUpperCase();

                List<String> allAgencies = Main.incidents.getAgencies().stream()
                        .map(Agency::getShorthand)
                        .map(String::toUpperCase)
                        .toList();

                if (input.endsWith(",")) {
                    yield allAgencies.stream()
                            .map(s -> input + s)
                            .toList();
                }

                List<String> allStatuses = Main.incidents.getAssignmentStatuses().stream()
                        .map(AssignmentStatus::getShortName)
                        .map(String::toUpperCase)
                        .toList();

                String[] parts = input.split(",");
                List<String> selectedAgencies = new ArrayList<>();

                // completed entries except last token
                for (int i = 0; i < parts.length - 1; i++) {
                    String token = parts[i].trim();
                    if (!token.isEmpty()) {
                        String agency = token.contains(":")
                                ? token.substring(0, token.indexOf(':'))
                                : token;

                        selectedAgencies.add(agency);
                    }
                }

                String currentToken = parts.length == 0
                        ? ""
                        : parts[parts.length - 1].trim();

                String prefix = selectedAgencies.isEmpty()
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

                    String agency = currentToken.substring(0, currentToken.indexOf(':'));
                    String statusPart = currentToken.substring(currentToken.indexOf(':') + 1);

                    // if agency invalid, no suggestions
                    if (!allAgencies.contains(agency)) {
                        yield List.of();
                    }

                    yield allStatuses.stream()
                            .filter(s -> s.startsWith(statusPart))
                            .map(s -> prefix + agency + ":" + s + ",")
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
                if (allAgencies.contains(currentToken)
                        && !selectedAgencies.contains(currentToken)) {

                    List<String> out = new ArrayList<>();

                    out.add(prefix + currentToken + ",");
                    out.add(prefix + currentToken + ":");

                    yield out.stream().limit(25).toList();
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
                yield allAgencies.stream()
                        .filter(a -> !selectedAgencies.contains(a))
                        .filter(a -> a.startsWith(currentToken))
                        .map(a -> prefix + a)
                        .limit(25)
                        .toList();
            }
                // -------- [ ABOVE THIS LINE CONTAINS SOME LLM-WRITTEN OR MODIFIED CODE ] --------

            default -> null;
        };
    }

    private IncidentLocation setCustomLocation(IReplyCallback reply, User user, String input) {
        if (!Main.users.hasPermission(user, Permission.USE_CUSTOM_LOCATION)) {
            DiscordMessages.error(reply, "You don't have permission to use a custom location and the location " +
                    "you inputted is not a saved location.");
            return null;
        }

        return new IncidentLocationImpl(new ArrayList<>(List.of(input)));
    }
}
