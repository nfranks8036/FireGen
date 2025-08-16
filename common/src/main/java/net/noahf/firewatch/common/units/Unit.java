package net.noahf.firewatch.common.units;

import net.noahf.firewatch.common.FireGen;
import net.noahf.firewatch.common.data.UnitOperationStatus;
import net.noahf.firewatch.common.data.UnitType;
import net.noahf.firewatch.common.utils.Identifier;
import org.jetbrains.annotations.Nullable;

public class Unit {

    private String unit_type;
    private int number;

    private transient Agency agency;
    private transient UnitType unitType;
    private transient UnitOperationStatus operationStatus;
    private transient @Nullable UnitAssignment assignmentStatus;

    void initialize(Agency agency) {
        this.agency = agency;
        this.unitType = FireGen.get().incidentStructure().unitTypes().getFromName(this.unit_type);
        this.operationStatus = FireGen.get().incidentStructure().unitOperationStatuses().marked(UnitOperationStatus.IN_SERVICE).one();
        this.assignmentStatus = null;

        if (this.unitType == null) {
            throw new IllegalArgumentException("Illegal unit type as provided in incident structure: " + unit_type);
        }
    }

    public UnitType unitType() {
        return FireGen.get().incidentStructure().unitTypes().getFromName(this.unit_type);
    }

    public void assignment(UnitAssignment assignment) { this.assignmentStatus = assignment; }
    public UnitAssignment assignment() { return this.assignmentStatus; }

    public String callsign(boolean abbreviated, boolean space) {
        StringBuilder callsignBuilder = new StringBuilder();
        if (abbreviated) {
            if (this.unitType.abbreviation() != null)
                callsignBuilder.append(this.unitType.abbreviation());
            else callsignBuilder.append(this.agency.abbreviation());
        }
        else callsignBuilder.append(this.unitType.callsign());

        if (space)
            callsignBuilder.append(" ");

        return callsignBuilder.append(this.number).toString();
    }

    @Override
    public String toString() {
        return this.callsign(true, false);
    }
}
