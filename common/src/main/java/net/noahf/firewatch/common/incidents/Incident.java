package net.noahf.firewatch.common.incidents;

import net.noahf.firewatch.common.utils.FireGenInternalHelper;
import net.noahf.firewatch.common.incidents.location.Address;
import net.noahf.firewatch.common.incidents.narrative.Narrative;
import net.noahf.firewatch.common.units.Unit;

import java.util.Random;

public class Incident {

    private long incidentNumber;
    private long dispatchTime;
    private IncidentType type;
    private IncidentPriority priority;
    private Narrative narrative;
    private CallerType callerType;
    private Address address;
    private Unit[] units;

    public Incident(long dispatchTime, IncidentType type, IncidentPriority priority, CallerType caller, Address address, Unit[] units) {
        this.incidentNumber = new Random().nextLong(100000000, 1000000000);
        this.dispatchTime = dispatchTime;
        this.type = type;
        this.priority = priority;
        this.narrative = new Narrative();
        this.callerType = caller;
        this.address = address;
        this.units = units;
    }

    public String getIncidentNumber() {
        return FireGenInternalHelper.firegen.getCurrentYear() + "-" + this.incidentNumber;
    }

    public long dispatchTime() {
        return this.dispatchTime;
    }

    public IncidentType incidentType() {
        return this.type;
    }
    public void incidentType(IncidentType newType) { this.type = newType; }

    public IncidentPriority incidentPriority() {
        return this.priority;
    }

    public Narrative narrative() { return this.narrative; }

    public CallerType callerType() {
        return this.callerType;
    }

    public Address address() {
        return this.address;
    }

    public Unit[] units() {
        return this.units;
    }

}
