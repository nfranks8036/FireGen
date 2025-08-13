package net.noahf.firewatch.common.data;

import com.google.gson.annotations.JsonAdapter;
import net.noahf.firewatch.common.data.ems.EmsMedical;
import net.noahf.firewatch.common.data.ems.EmsMedicalDeserializer;
import net.noahf.firewatch.common.data.objects.StructureList;
import net.noahf.firewatch.common.data.objects.StructureObject;

import java.util.List;

public class IncidentStructure extends StructureObject {

    private String municipality;
    private List<String> incident_statuses;
    private List<String> unit_assignment_statuses;
    private List<String> unit_operation_statuses;
    private List<IncidentType> incident_types;
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

    public StructureList<IncidentStatus> getIncidentStatuses() { return new StructureList<>(this.incident_statuses, IncidentStatus::new); }

    public StructureList<UnitAssignmentStatus> getUnitAssignmentStatuses() { return new StructureList<>(this.unit_assignment_statuses, UnitAssignmentStatus::new); }

    public StructureList<UnitOperationStatus> getUnitOperationStatuses() { return new StructureList<>(this.unit_operation_statuses, UnitOperationStatus::new); }

    public StructureList<IncidentType> getIncidentTypes() { return new StructureList<>(this.incident_types); }

    public StructureList<CallerType> getCallerTypes() { return new StructureList<>(this.caller_types, CallerType::new); }

    public EmsMedical getEmsMedical() { return this.ems_medical; }

    public StructureList<AgencyType> getAgencyTypes() { return new StructureList<>(this.agency_types, AgencyType::new); }

    public StructureList<IncidentPriority> getIncidentPriorities() { return new StructureList<>(IncidentType.allPriorities); }

    public StructureList<RadioChannel> getRadioChannels() { return new StructureList<>(this.radio_channels, RadioChannel::new); }

}
