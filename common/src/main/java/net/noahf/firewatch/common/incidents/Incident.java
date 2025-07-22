package net.noahf.firewatch.common.incidents;

import net.noahf.firewatch.common.geolocation.GeoAddress;
import net.noahf.firewatch.common.geolocation.IncidentAddress;
import net.noahf.firewatch.common.incidents.narrative.Narrative;
import net.noahf.firewatch.common.units.Unit;
import net.noahf.firewatch.common.utils.TimeHelper;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Incident {

    private long incidentNumber;
    private long dispatchTime;
    private IncidentType type;
    private IncidentPriority priority;
    private Narrative narrative;
    private CallerType callerType;
    private IncidentAddress address;
    private List<Unit> units;

    public Incident(long dispatchTime, IncidentType type, IncidentPriority priority, CallerType caller, IncidentAddress address, Unit... units) {
        this.incidentNumber = new Random().nextLong(100000000, 1000000000);
        this.dispatchTime = dispatchTime;
        this.type = type;
        this.priority = priority;
        this.narrative = new Narrative();
        this.callerType = caller;
        this.address = address;
        this.units = Arrays.stream(units).toList();
    }

    public String getIncidentNumber() {
        return TimeHelper.getCurrentYear() + "-" + this.incidentNumber;
    }

    public long dispatchTime() { return this.dispatchTime; }
    public void dispatchTime(long newDispatchTime) { this.dispatchTime = newDispatchTime; }

    public IncidentType incidentType() {
        return this.type;
    }
    public void incidentType(IncidentType newType) {
        this.type = newType;
        if (!Arrays.stream(this.type.supportedPriorityResponses()).toList().contains(this.incidentPriority())) {
            // reset priority if the new IncidentType doesn't support a previous priority response
            this.priority = this.type.supportedPriorityResponses()[0];
        }
    }

    public IncidentPriority incidentPriority() {
        return this.priority;
    }
    public void incidentPriority(IncidentPriority newPriority) { this.priority = newPriority; }

    public Narrative narrative() { return this.narrative; }

    public CallerType callerType() {
        return this.callerType;
    }
    public void callerType(CallerType newCallerType) { this.callerType = newCallerType; }

    public IncidentAddress address() {
        return this.address;
    }

    public List<Unit> units() {
        return this.units;
    }

}
