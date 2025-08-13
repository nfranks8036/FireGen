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
    private transient UnitOperationStatus operationStatus;
    private transient @Nullable UnitAssignment assignmentStatus;

    void initialize(Agency agency) {
        this.agency = agency;
        this.operationStatus = FireGen.get().incidentStructure().unitOperationStatuses().marked(UnitOperationStatus.IN_SERVICE).one();
        this.assignmentStatus = null;
    }

    public void assignment(UnitAssignment assignment) { this.assignmentStatus = assignment; }
    public UnitAssignment assignment() { return this.assignmentStatus; }

}
