package net.noahf.firegen.api.utilities;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.noahf.firegen.api.incidents.IncidentType;
import net.noahf.firegen.api.incidents.IncidentTypeTag;

import java.util.Collections;
import java.util.List;

@Getter @Accessors(fluent = true, chain = true)
@Setter
public class FireGenVariables {

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
            @Override public List<String> findTypeOptions(IncidentType type) { return Collections.singletonList(type.getSelectedName()); }
        };
        this.defaultType = new IncidentType() {
            @Override public String type() { return ">NEW<"; }
            @Override public IncidentTypeTag tag() { return FireGenVariables.this.defaultTag; }
            @Override public int qualifierChoice() { return 0; }
            @Override public String getSelectedName() { return this.type(); }
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

}
