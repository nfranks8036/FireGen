package net.noahf.firewatch.common.incidents;

import net.noahf.firewatch.common.FireGenWrapper;
import net.noahf.firewatch.common.units.Unit;

import java.util.Random;
import java.util.UUID;

public class Incident {

    private long incidentNumber;
    private long dispatchTime;
    private IncidentType callType;
    private Address address;
    private Unit[] units;

    public Incident(long dispatchTime, IncidentType type, Address address, Unit[] units) {
        this.incidentNumber = new Random().nextLong(100000000, 1000000000);
        this.dispatchTime = dispatchTime;
        this.callType = type;
        this.address = address;
        this.units = units;
    }

    public String getIncidentNumber() {
        return FireGenWrapper.firegen.getCurrentYear() + "-" + this.incidentNumber;
    }

    public long dispatchTime() {
        return this.dispatchTime;
    }

    public IncidentType callType() {
        return this.callType;
    }

    public Address address() {
        return this.address;
    }

    public Unit[] units() {
        return this.units;
    }

}
