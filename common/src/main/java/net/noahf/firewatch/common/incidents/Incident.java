package net.noahf.firewatch.common.incidents;

import net.noahf.firewatch.common.agency.Agency;
import net.noahf.firewatch.common.geolocation.GeoAddress;
import net.noahf.firewatch.common.geolocation.IncidentAddress;
import net.noahf.firewatch.common.incidents.medical.MedicalCallDetail;
import net.noahf.firewatch.common.incidents.narrative.Narrative;
import net.noahf.firewatch.common.units.Unit;
import net.noahf.firewatch.common.utils.TimeHelper;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class Incident {

    private final long incidentNumber;
    private final Narrative narrative;
    private MedicalCallDetail medicalCallDetails;
    private long dispatchTime;
    private IncidentType type;
    private IncidentPriority priority;
    private CallerType callerType;
    private IncidentAddress address;

    public Incident(long dispatchTime, IncidentType type, IncidentPriority priority, CallerType caller, IncidentAddress address) {
        this.incidentNumber = new Random().nextLong(100000000, 1000000000);
        this.narrative = new Narrative();
        this.medicalCallDetails = null;
        this.dispatchTime = dispatchTime;
        this.type = type;
        this.priority = priority;
        this.callerType = caller;
        this.address = address;
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
        if (this.address == null)
            this.address = IncidentAddress.blankAddress();
        return this.address;
    }

    public Optional<MedicalCallDetail> ems() {
        if (this.type == IncidentType.EMS && this.medicalCallDetails == null) {
            this.medicalCallDetails = new MedicalCallDetail();
        }
        return Optional.ofNullable(this.medicalCallDetails);
    }

}
