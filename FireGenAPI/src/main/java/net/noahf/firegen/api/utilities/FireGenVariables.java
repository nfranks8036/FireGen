package net.noahf.firegen.api.utilities;

import lombok.*;
import lombok.experimental.Accessors;
import net.noahf.firegen.api.incidents.location.LocationField;
import net.noahf.firegen.api.incidents.location.LocationVenue;
import net.noahf.firegen.api.incidents.types.IncidentType;
import net.noahf.firegen.api.incidents.types.IncidentTypeTag;
import net.noahf.firegen.api.incidents.types.IncidentTypeTagQualifierList;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

@Getter @Accessors(fluent = true, chain = true)
@Setter
@Builder(builderMethodName = "")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
public class FireGenVariables {

    public static FireGenVariablesBuilder createFromFolder(String municipality) {
        return new FireGenVariablesBuilder()
                .municipality(municipality);
    }

    public FireGenVariables resetToDefault(String municipality, boolean includeUserSet) {
        this.municipality = municipality;

        if (includeUserSet) {
            this.incidentStaleMinutes = 60;
            this.incidentStaleCloseMinutes = 300;

            this.incidentTypesFile = "incident_types.json";
            this.unitsFile = "units.json";
            this.venuesFile = "venues.json";
            this.municipalityFile = "municipality.json";
            this.assignmentStatusFile = "assignments.json";
            this.incidentStatusFile = "incident_status.json";
            this.locationPresetsFile = "locations.json";
            this.radioChannelsFile = "radio_channels.json";
            this.unitTypesFile = "unit_types.json";

            this.usersFile = "users.json";

            this.shortTimeFormat = "HH:mm";
            this.longTimeFormat = "HH:mm:ss";
            this.dateFormat = "MM/dd/yyyy";

            this.defaults = true;
        }

        this.defaultTag = new IncidentTypeTag() {
            @Override public String getTagName() { return "Not Set"; }
            @Override public List<String> getPriorities() { return List.of("1", "2", "3"); }
            @Override public IncidentTypeTagQualifierList getQualifiers() { return null; }
            @Override public List<String> findTypeOptions(String genericIncidentType) { return Collections.singletonList(genericIncidentType); }
            @Override @NotNull public String toString() {
                return this.getTagName().toUpperCase();
            }
        };
        this.defaultType = new IncidentType() {
            @Override public String getType() { return ">NEW<"; }
            @Override public IncidentTypeTag getTag() { return FireGenVariables.this.defaultTag; }
            @Override public int getQualifierChoice() { return 0; }
            @Override public int getPriorityChoice() { return 0; }
            @Override public String getStringQualifierChoice() { return ""; }
            @Override public String getSelectedName() { return this.getType(); }
            @Override public long getId() { return 0; }
        };

        return this;
    }

    private String municipality;

    private int incidentStaleMinutes;
    private int incidentStaleCloseMinutes;

    private String incidentTypesFile;
    private String unitsFile;
    private String venuesFile;
    private String municipalityFile;
    private String assignmentStatusFile;
    private String incidentStatusFile;
    private String locationPresetsFile;
    private String radioChannelsFile;
    private String unitTypesFile;

    private String usersFile;

    private String shortTimeFormat;
    private String longTimeFormat;
    private String dateFormat;

    private IncidentTypeTag defaultTag;
    private IncidentType defaultType;


    private boolean defaults;

    @SuppressWarnings("deprecation")
    public void setVenues(List<LocationVenue> venues) {
        LocationField.setKnownVenues(venues);
    }

    public String formatTime(LocalDateTime time, boolean withDate) {
        return (withDate ? time.format(DateTimeFormatter.ofPattern(this.dateFormat())) + " ": "")
                + time.format(DateTimeFormatter.ofPattern(this.longTimeFormat()));
    }

}
