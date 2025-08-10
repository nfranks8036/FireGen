package net.noahf.firewatch.common.newincidents;

import com.google.gson.annotations.JsonAdapter;
import net.noahf.firewatch.common.newincidents.objects.StructureList;
import net.noahf.firewatch.common.newincidents.objects.StructureObject;
import net.noahf.firewatch.common.newincidents.medical.EmsMedical;
import net.noahf.firewatch.common.newincidents.medical.EmsMedicalDeserializer;

import java.util.List;

public class IncidentStructure implements StructureObject {

    private String municipality;

    private List<IncidentType> incident_types;

    private List<UnitStatus> unit_statuses;

    private List<UnitType> unit_types;

    private List<String> caller_types;

    @JsonAdapter(EmsMedicalDeserializer.class)
    private EmsMedical ems_medical;

    private List<String> agency_types;

    private List<String> radio_channels;



    @Override public String getName() {
        return this.municipality;
    }
    @Override public String getFormatted() {
        return this.municipality.replace("_", " ");
    }


    public String getMunicipality() { return this.municipality; }

    public StructureList<IncidentType> getIncidentTypes() { return new StructureList<>(this.incident_types); }

    public StructureList<UnitStatus> getUnitStatuses() { return new StructureList<>(this.unit_statuses); }

    public StructureList<CallerType> getCallerTypes() { return new StructureList<>(this.caller_types, CallerType::new); }

    public EmsMedical getEmsMedical() { return this.ems_medical; }

    public StructureList<AgencyType> getAgencyTypes() { return new StructureList<>(this.agency_types, AgencyType::new); }

    public StructureList<IncidentPriority> getIncidentPriorities() { return new StructureList<>(IncidentType.allPriorities); }

    public StructureList<RadioChannel> getRadioChannels() { return new StructureList<>(this.radio_channels, RadioChannel::new); }

}
