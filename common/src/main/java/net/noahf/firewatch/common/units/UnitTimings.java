package net.noahf.firewatch.common.units;

import net.noahf.firewatch.common.data.UnitAssignmentStatus;

import java.time.Instant;

public record UnitTimings(Instant time, UnitAssignmentStatus assignmentStatus) {

}
