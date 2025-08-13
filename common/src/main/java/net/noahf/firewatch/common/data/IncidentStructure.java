package net.noahf.firewatch.common.data;

import com.google.gson.annotations.JsonAdapter;
import net.noahf.firewatch.common.data.ems.EmsMedical;
import net.noahf.firewatch.common.data.ems.EmsMedicalDeserializer;
import net.noahf.firewatch.common.data.objects.ListMark;
import net.noahf.firewatch.common.data.objects.StructureList;
import net.noahf.firewatch.common.data.objects.StructureObject;
import net.noahf.firewatch.common.units.UnitAssignment;

import java.util.List;
import java.util.function.Function;

@SuppressWarnings("unchecked")
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


    @Override public String name() {
        return this.municipality;
    }
    @Override public String formatted() {
        return this.municipality.replace("_", " ");
    }


    public String municipality() { return this.municipality; }

    public StructureList<IncidentStatus> incidentStatuses() {
        return new StructureList<>(this.incident_statuses, IncidentStatus::new,
                ListMark.of(IncidentStatus.NEW_INCIDENT, (s) -> s.contains("*")),
                ListMark.of(IncidentStatus.CLOSED_INCIDENT, (s) -> s.contains("!"))
        );
    }

    public StructureList<UnitAssignmentStatus> unitAssignmentStatuses() {
        return new StructureList<>(this.unit_assignment_statuses, UnitAssignmentStatus::new,
                ListMark.of(UnitAssignmentStatus.ASSIGNED, (s) -> s.contains("*")),
                ListMark.of(UnitAssignmentStatus.AVAILABLE, (s) -> s.contains("!"))
        );
    }

    public StructureList<UnitOperationStatus> unitOperationStatuses() {
        return new StructureList<>(this.unit_operation_statuses, UnitOperationStatus::new,
                ListMark.of(UnitOperationStatus.IN_SERVICE, (s) -> s.contains("*")),
                ListMark.of(UnitOperationStatus.OUT_OF_SERVICE, (s) -> s.contains("!"))
        );
    }

    public StructureList<IncidentType> incidentTypes() {
        return new StructureList<>(this.incident_types, (i) -> i);
    }

    public StructureList<CallerType> callerTypes() {
        return new StructureList<>(this.caller_types, CallerType::new);
    }

    public EmsMedical emsMedical() {
        return this.ems_medical;
    }

    public StructureList<AgencyType> agencyTypes() {
        return new StructureList<>(this.agency_types, AgencyType::new);
    }

    public StructureList<IncidentPriority> incidentPriorities() {
        return new StructureList<>(IncidentType.allPriorities, (i) -> i);
    }

    public StructureList<RadioChannel> radioChannels() {
        return new StructureList<>(this.radio_channels, RadioChannel::new);
    }

}
