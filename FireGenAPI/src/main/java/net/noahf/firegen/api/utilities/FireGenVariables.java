package net.noahf.firegen.api.utilities;

import lombok.*;
import lombok.experimental.Accessors;
import net.noahf.firegen.api.incidents.IncidentType;
import net.noahf.firegen.api.incidents.IncidentTypeTag;
import net.noahf.firegen.api.incidents.location.LocationField;
import net.noahf.firegen.api.incidents.location.LocationVenue;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Getter @Accessors(fluent = true, chain = true)
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FireGenVariables {

    public static FireGenVariables newInstanceWithDefaults() {
        return new FireGenVariables().resetToDefault();
    }

    public FireGenVariables resetToDefault() {
        this.incidentTypesFile = "incident_types.json";
        this.agenciesFile = "agencies.json";
        this.venuesFile = "venues.json";

        this.shortTimeFormat = "HH:mm";
        this.longTimeFormat = "HH:mm:ss";
        this.dateFormat = "MM/dd/yyyy";

        this.defaultTag = new IncidentTypeTag() {
            @Override public String getTagName() { return "None"; }
            @Override public List<String> getPriorities() { return List.of("1", "2", "3"); }
            @Override public Qualifier getQualifier() { return null; }
            @Override public List<String> findTypeOptions(String name) { return Collections.singletonList(name); }
        };
        this.defaultType = new IncidentType() {
            @Override public String getType() { return ">NEW<"; }
            @Override public IncidentTypeTag getTag() { return FireGenVariables.this.defaultTag; }
            @Override public int getQualifierChoice() { return 0; }
            @Override public String getSelectedName() { return this.getType(); }
            @Override public String getSelectedPriority() { return ""; }
            @Override public long getId() { return 0; }
        };

        return this;
    }

    private String incidentTypesFile;
    private String agenciesFile;
    private String venuesFile;

    private String shortTimeFormat;
    private String longTimeFormat;
    private String dateFormat;

    private IncidentTypeTag defaultTag;
    private IncidentType defaultType;

    public void setVenues(List<LocationVenue> venues) {
        String description = LocationField.VENUE.getDescription();
        LocationField.VENUE = LocationField.VENUE.toBuilder()
                .setDescription(description.replace(
                        "{VENUES}",
                        venues.stream().map(LocationVenue::getName).collect(Collectors.joining(", "))
                ))
                .build();
    }

}
