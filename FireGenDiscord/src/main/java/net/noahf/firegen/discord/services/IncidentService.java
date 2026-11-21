package net.noahf.firegen.discord.services;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dv8tion.jda.api.components.selections.SelectOption;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.noahf.firegen.api.incidents.Incident;
import net.noahf.firegen.api.incidents.IncidentLogEntry;
import net.noahf.firegen.api.incidents.IncidentPublishedStatus;
import net.noahf.firegen.api.incidents.location.IncidentLocation;
import net.noahf.firegen.api.incidents.location.LocationField;
import net.noahf.firegen.api.incidents.location.LocationType;
import net.noahf.firegen.api.incidents.location.LocationVenue;
import net.noahf.firegen.api.incidents.status.IncidentStatus;
import net.noahf.firegen.api.incidents.types.IncidentType;
import net.noahf.firegen.api.incidents.units.*;
import net.noahf.firegen.discord.Main;
import net.noahf.firegen.discord.actions.FireGenAction;
import net.noahf.firegen.discord.actions.registered.*;
import net.noahf.firegen.discord.config.ConfigManager;
import net.noahf.firegen.discord.config.files.ConfigAssignmentStatuses;
import net.noahf.firegen.discord.config.files.ConfigLocationPresets;
import net.noahf.firegen.discord.config.files.ConfigUnits;
import net.noahf.firegen.discord.config.files.ConfigVenues;
import net.noahf.firegen.discord.incidents.structure.IncidentImpl;
import net.noahf.firegen.discord.incidents.structure.location.IncidentLocationImpl;
import net.noahf.firegen.discord.incidents.structure.units.*;
import net.noahf.firegen.discord.users.FireGenUser;
import net.noahf.firegen.discord.users.Permission;
import net.noahf.firegen.discord.utilities.Log;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class IncidentService {

    public record IncidentCreateRequest(
            String type, String location, String[] units, LocalDateTime timestamp, String initialNarrative
    ) {  }

    public record IncidentEditRequest(
            String type, String location, String[] units, LocalDateTime timestamp, String narrative,
            Boolean published, String status
    ) { }

    public ServiceResponse<?> createIncident(FireGenUser<?> user, IncidentCreateRequest request) {
        if (request.type == null) {
            return ServiceResponse.fail(user, "At minimum, incidents must be created with an Incident Type. Found none!");
        }

        IncidentImpl incident = Main.incidents.createNewIncident();
        ServiceResponse<?> response = ServiceResponse.success(incident);
        try {
            // ---------- incident type ----------
            boolean publish = false;
            String type = request.type;
            if (type.startsWith("pub-")) {
                publish = true;
                type = request.type.substring("pub-".length()).toUpperCase();
            }

            incident.setTypeBySearch(type);

            // ---------- incident contributors // begin list ----------
            incident.addContributor(user);
            incident.addLog(user, IncidentLogEntry.EntryType.CREATE, "New Incident: " + incident.getType().getSelectedName());

            response = ServiceResponse.returnAUnlessBFail(response, _editIncident(user, incident, new IncidentEditRequest(
                    null, request.location, request.units, request.timestamp, request.initialNarrative, null, null
            ), false));

            incident.setStatus(IncidentStatus.PENDING);

            if (publish) {
                findAction(Publish.class).onSubmit(incident, user, null);
            }
        } catch (Exception exception) {
            return ServiceResponse.fail(user, exception.toString());
        }

        if (response.success()) {
            incident.update();
            EditMode.editIncidents.put(user, incident);
            Main.incidents.commitIncident(incident);
        }

        return response;
    }

    public ServiceResponse<?> editIncident(FireGenUser<?> user, Incident incident, IncidentEditRequest request) {
        return _editIncident(user, incident, request, true);
    }

    private ServiceResponse<?> _editIncident(FireGenUser<?> user, Incident incident, IncidentEditRequest request, boolean editAction) {
        ServiceResponse<?> response = ServiceResponse.success(incident);
        incident.addContributor(user);

        response = ServiceResponse.returnAUnlessBFail(response,
                this.type(incident, user, request.type)
        );

        response = ServiceResponse.returnAUnlessBFail(response,
                this.location(incident, user, request.location)
        );

        response = ServiceResponse.returnAUnlessBFail(response,
                this.units(incident, user, request.units)
        );

        response = ServiceResponse.returnAUnlessBFail(response,
                this.dateTime(incident, user, request.timestamp)
        );

        response = ServiceResponse.returnAUnlessBFail(response,
                this.narrative(incident, user, request.narrative)
        );

        response = ServiceResponse.returnAUnlessBFail(response,
                this.published(incident, user,  request.published)
        );

        response = ServiceResponse.returnAUnlessBFail(response,
                this.status(incident, user, request.status)
        );

        if (editAction) {
            incident.update();

            EditMode.editIncidents.put(user, incident);
        }

        return response;
    }

    public ServiceResponse<?> type(Incident incident, FireGenUser<?> user, String input) {
        if (input == null) {
            return ServiceResponse.successNone();
        }

        if (!user.hasPermission(Permission.CHANGE_CALL_TYPE)) {
            return ServiceResponse.fail(user, "You don't have permission to change the incident type.");
        }

        IncidentType oldType = incident.getType();
        try {
            ((IncidentImpl)incident).setTypeBySearch(input);
        } catch (IllegalArgumentException e) {
            return ServiceResponse.fail(user, "Invalid incident type: " + input);
        }
        IncidentType newType = incident.getType();

        if (oldType.equals(newType)) {
            return ServiceResponse.fail(user, "You cannot change the incident type to the existing incident type!");
        }

        String narrative = "Changed Incident Type: " + oldType.getSelectedName() + " --> " + newType.getSelectedName();
        incident.addLog(user, IncidentLogEntry.EntryType.UPDATE, narrative);
        incident.addContributor(user);

        incident.update();

        return ServiceResponse.successNone();
    }

    public ServiceResponse<?> location(Incident incident, FireGenUser<?> user, String input) {
        if (input == null) {
            return ServiceResponse.successNone();
        }

        if (!user.hasPermission(Permission.CHANGE_LOCATION)) {
            return ServiceResponse.fail(user, "You don't have permission to change the incident location.");
        }

        EditLocation action = findAction(EditLocation.class);

        IncidentLocation location;
        if (input.startsWith("$")) {
            location = extractLocation(input.substring(1));
        } else {
            location = Main.config.get(ConfigLocationPresets.class).fromAnyName(input);
        }

        if (location == null) {
            if (!user.hasPermission(Permission.USE_CUSTOM_LOCATION)) {
                return ServiceResponse.fail(user,
                        "You don't have permission to set a custom location, " +
                                "so the location was not set. Please use a preset, the buttons, or the " +
                                "correct custom time.");
            }

            location = new IncidentLocationImpl(List.of(input));
        }

        return action.onSubmit(incident, user, location.getType(), location);
    }
    private IncidentLocation extractLocation(String text) {
        String[] typeVsData = text.split(":");
        if (typeVsData.length != 2) {
            throw new IllegalArgumentException(
                    "Expected a LocationType and location data (such as 'LocationType:data1,data2,...'), "
                            + "got \"" + text + "\""
            );
        }

        LocationType type = LocationType.valueOf(typeVsData[0].toUpperCase());
        List<String> dataString = new ArrayList<>(Arrays.stream(typeVsData[1].split(";", -1)).toList());

        List<Integer> remove = new ArrayList<>();

        String common = null;
        LocationVenue venue = null;
        for (int i = 0; i < dataString.size(); i++) {
            if (dataString.get(i).isBlank()) {
                remove.add(i);
                continue;
            }

            LocationField field = type.getFields()[i];
            if (field.getId().equalsIgnoreCase("common-name")) {
                remove.add(i);
                common = dataString.get(i);
            }
            if (field.getId().equalsIgnoreCase("venue")) {
                remove.add(i);
                venue = Main.config.get(ConfigVenues.class).fromName(dataString.get(i));
            }
        }

        int j = 0;
        for (Integer i : remove) {
            dataString.remove(i - j);
            j++;
        }

        return new IncidentLocationImpl(dataString, type, common, venue);
    }

    @AllArgsConstructor @Getter enum UnitsTargetType {
        UNIT('\0'), AGENCY('@'), WILDCARD('*'), CUSTOM('?');

        private final char character;

        public static UnitsTargetType fromCharacter(char i) {
            for (UnitsTargetType t : UnitsTargetType.values()) { if (t.character == i) return t; }
            return UnitsTargetType.UNIT;
        }
    }
    public ServiceResponse<?> units(Incident incident, FireGenUser<?> user, String[] input) {
        if (input == null) { return ServiceResponse.successNone(); }

        if (!user.hasPermission(Permission.CHANGE_UNITS)) {
            return ServiceResponse.fail(user, "You don't have permission to change the incident units.");
        }

        ConfigManager config = Main.config;
        ConfigUnits configUnits = config.get(ConfigUnits.class);
        EditUnits action = findAction(EditUnits.class);

        ServiceResponse<?> response = ServiceResponse.success(incident);
        Map<AssignmentStatus, EditUnits.UnitsChangeInput> units = new HashMap<>();
        for (String unchangedItem : input) {
            response = ServiceResponse.returnAUnlessBFail(response,
                    this.addUnitItem(
                            incident, unchangedItem, user, config, configUnits, units
                    )
            );
        }

        for (Map.Entry<AssignmentStatus, EditUnits.UnitsChangeInput> entry : units.entrySet()) {
            response = ServiceResponse.returnAUnlessBFail(response, action.onSubmit(incident, user, entry.getValue()));
        }

        return response;
    }
    private ServiceResponse<?> addUnitItem(
            Incident incident, String unchangedItem, FireGenUser<?> user, ConfigManager config, ConfigUnits configUnits,
            Map<AssignmentStatus, EditUnits.UnitsChangeInput> units
    ) {
        ServiceResponse<?> response = ServiceResponse.successNone();

        // we create a targetString because it is intended to change, optionItem should not change
        String targetString = unchangedItem;

        UnitsTargetType type = UnitsTargetType.fromCharacter(targetString.charAt(0));

        String statusString = AssignmentStatusImpl.ADD_UNIT.getShortName();

        if (unchangedItem.contains(":")) {
            String[] parts = unchangedItem.split(":");
            targetString = parts[0];
            statusString = parts[1];
        }

        String secondaryString;
        if (statusString.contains(">")) {
            secondaryString = statusString.split(">")[1];
            statusString = statusString.split(">")[0];
        } else secondaryString = null;

        AssignmentStatus findStatus = config.get(ConfigAssignmentStatuses.class).fromShortName(statusString);
        if (findStatus == null) {
            findStatus = AssignmentStatusImpl.ADD_UNIT;
            response = ServiceResponse.success("No assignment exists with the name '" + statusString + "'," +
                    " defaulting to " + findStatus.getShortName() + " for " + targetString);
        }
        final AssignmentStatus status = findStatus;

        Secondary secondary = null;
        if (secondaryString != null) {
            secondary = status.getSecondaries().stream()
                    .filter(s -> s.shortName().equalsIgnoreCase(secondaryString)
                            || s.longName().equalsIgnoreCase(secondaryString)
                    )
                    .findFirst().orElse(new SecondaryImpl(secondaryString, secondaryString, null));
        }

        // required syntax of command is the shorthand. e.g., "BFD,BVRS:DSP,SUP5:ENR,BPD,VTPD,R51:LEF>RMH"
        List<Unit> inputUnits = new ArrayList<>();
        switch (type) {
            case UNIT -> {
                Unit u = configUnits.fromShorthand(targetString);
                if (u == null) {
                    return ServiceResponse.fail(user, "No unit exist by the name '" + targetString + "'");
                }
                inputUnits.add(u);
            }
            case AGENCY -> {
                Agency a = configUnits.agencyFromShorthand(targetString.substring(1));
                if (a == null) {
                    return ServiceResponse.fail(user, "No agency exist by the name '" + targetString + "'");
                }
                inputUnits.addAll(incident.getUnitAssignments().stream()
                        .filter(u -> !u.getLatestAssignment().getStatus().equals(status))
                        .map(UnitAssignment::getUnit)
                        .filter(unit -> unit.getAgency().equals(a))
                        .toList()
                );
            }
            case WILDCARD ->
                    inputUnits.addAll(incident.getUnitAssignments().stream()
                            .filter(u -> !u.getLatestAssignment().getStatus().equals(status))
                            .map(UnitAssignment::getUnit)
                            .toList()
                    );
            case CUSTOM -> {
                // ?SHORTHAND.LONGHAND.FORMATTED.AGENCY
                String[] text = targetString.substring(1).split("\\.");

                String shorthand = text[0];
                String longhand = text[1];
                String formatted = text[2];
                String agencyText = text[3];

                Agency agency = configUnits.agencyFromShorthand(agencyText);
                if (agency == null) {
                    agency = new AgencyImpl(agencyText, agencyText, agencyText, "N/A",
                            AgencyType.OTHER, null, Integer.MAX_VALUE,
                            new ArrayList<>(), Integer.MAX_VALUE
                    );
                    Log.warn("User " + user.getUsername() + " created a temporary agency: " + agency);
                }

                Emoji emoji;
                try {
                    emoji = ((AgencyImpl)agency).getEmoji();
                } catch (Exception exception) {
                    emoji = null;
                }
                UnitImpl custom = new UnitImpl(
                        shorthand, longhand.toUpperCase(), formatted,
                        emoji, agency, UnitTypeImpl.GENERATED, Integer.MIN_VALUE,
                        ((AgencyImpl)agency).getStartUnitOrdinal(), null, false,
                        SelectOption.of(longhand.toUpperCase(), shorthand)
                                .withEmoji(emoji)
                );
                configUnits.get().add(custom);
                agency.getUnits().add(custom);
                Log.warn("User " + user.getUsername() + " created a custom unit: " + custom.toStringJava());
                inputUnits.add(custom);
            }
        }

        if (inputUnits.isEmpty()) {
            return ServiceResponse.fail(user, "You attempted to add no units to the incident. " +
                    "Did you attempt to " + UnitsTargetType.AGENCY.getCharacter() + " an Agency that isn't attached to this " +
                    "incident?");
        }

        EditUnits.UnitsChangeInput input = units.getOrDefault(status,
                new EditUnits.UnitsChangeInput(new ArrayList<>(), status, secondary)
        );
        input.setUnits(inputUnits);
        units.put(status, input);

        return response;
    }

    public ServiceResponse<?> dateTime(Incident incident, FireGenUser<?> user, LocalDateTime timestamp) {
        if (timestamp == null) {
            return ServiceResponse.successNone();
        }

        return findAction(EditDateTime.class)
                .onSubmit(
                        incident, user, Main.config.getFireGenVariables(), timestamp.toLocalDate(), timestamp.toLocalTime()
                );
    }

    public ServiceResponse<?> published(Incident incident, FireGenUser<?> user, Boolean input) {
        if (input == null) {
            return ServiceResponse.successNone();
        }

        if (!user.hasPermission(Permission.INCIDENT_PUBLISH)) {
            return ServiceResponse.fail(user, "You don't have permission to publish an incident!");
        }

        IncidentPublishedStatus status = input ? IncidentPublishedStatus.PUBLISHED : IncidentPublishedStatus.UNPUBLISHED;
        return findAction(Publish.class).onSubmit(incident, user, status);
    }

    public ServiceResponse<?> status(Incident incident, FireGenUser<?> user, String input) {
        if (input == null) {
            return ServiceResponse.successNone();
        }

        if (!user.hasPermission(Permission.INCIDENT_STATUS)) {
            return ServiceResponse.fail(user, "You don't have permission to change the incident status!");
        }

        IncidentStatus status;
        try {
            status = IncidentStatus.valueOf(input.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ServiceResponse.fail(user, "That status doesn't exist: " + input);
        }

        return findAction(ChangeStatus.class).onSubmit(incident, user, status);
    }

    public ServiceResponse<?> narrative(Incident incident, FireGenUser<?> user, String narrative) {
        if (narrative == null) {
            return ServiceResponse.successNone();
        }

        if (!user.hasPermission(Permission.NARRATIVE_ADD)) {
            return ServiceResponse.fail(user, "You don't have permission to add to the narrative.");
        }

        if (narrative.isEmpty()) {
            return ServiceResponse.fail(user, "Expected narrative to contain content, found an empty text string.");
        }

        if (!narrative.toUpperCase().startsWith("NOTE:")) {
            return findAction(AddNarrative.class).onSubmit(incident, user, narrative);
        } else {
            return findAction(Notate.class).onSubmit(incident, user, narrative.substring(4));
        }
    }



    static @NotNull <T extends FireGenAction> T findAction(Class<T> clazz) {
        return Main.actions.getAction(clazz);
    }



}
