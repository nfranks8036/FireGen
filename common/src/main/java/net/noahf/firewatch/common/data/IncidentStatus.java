package net.noahf.firewatch.common.data;

import net.noahf.firewatch.common.data.objects.StructureObject;

public class IncidentStatus extends StructureObject {

    private final String incidentStatus;

    IncidentStatus(String incidentStatus) {
        this.incidentStatus = incidentStatus;
    }

    @Override public String getName() { return incidentStatus; }
    @Override public String getFormatted() { return incidentStatus.replace("_", " "); }

}