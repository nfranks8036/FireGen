package net.noahf.firewatch.common.incidents;

import net.noahf.firewatch.common.data.IncidentPriority;
import net.noahf.firewatch.common.data.IncidentStatus;
import net.noahf.firewatch.common.data.IncidentType;
import net.noahf.firewatch.common.geolocation.IncidentAddress;
import net.noahf.firewatch.common.narrative.IncidentNarrative;

import java.time.Instant;
import java.util.List;

public class Incident {

    private int id;
    private IncidentStatus status;
    private IncidentType type;
    private IncidentPriority priority;
    private IncidentAddress address;
    private IncidentNarrative narrative;
    private Instant created, closed;


}
