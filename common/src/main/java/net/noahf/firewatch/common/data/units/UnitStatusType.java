package net.noahf.firewatch.common.data.units;

public enum UnitStatusType {

    OPERATION(UnitOperationStatus.class),

    ASSIGNMENT(UnitAssignmentStatus.class);

    private final Class<?> type;

    UnitStatusType(Class<?> type) {
        this.type = type;
    }

    public Class<?> type() { return this.type; }

}
