package net.noahf.firewatch.common.units;

import net.noahf.firewatch.common.data.UnitAssignmentStatus;

import java.time.Instant;

public class UnitTimings {

    private Instant time;
    private final UnitAssignmentStatus status;

    public UnitTimings(Instant time, UnitAssignmentStatus assignmentStatus) {
        this.time = time;
        this.status = assignmentStatus;
    }

    public Instant time() { return this.time; }
    public void time(Instant time) { this.time = time; }

    public UnitAssignmentStatus assignmentStatus() { return this.status; }

}
