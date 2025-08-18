package net.noahf.firewatch.common.units;

import net.noahf.firewatch.common.data.units.UnitAssignmentStatus;

import java.time.Instant;

public record AssignmentEvent(UnitAssignmentStatus status, Instant timestamp, String narrative) {
}
