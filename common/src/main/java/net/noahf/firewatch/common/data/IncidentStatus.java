package net.noahf.firewatch.common.data;

import net.noahf.firewatch.common.data.objects.StructureObject;

public class IncidentStatus extends StructureObject {

    public static final String NEW_INCIDENT = "NEW";
    public static final String CLOSED_INCIDENT = "CLOSED";

    private final String incidentStatus;

    IncidentStatus(String incidentStatus) {
        this.incidentStatus = incidentStatus.replace("*", "");
    }

    @Override public String name() { return incidentStatus; }
    @Override public String formatted() { return incidentStatus.replace("_", " "); }

}