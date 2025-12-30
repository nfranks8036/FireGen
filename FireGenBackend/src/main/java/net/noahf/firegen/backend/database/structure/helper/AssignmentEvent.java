package net.noahf.firegen.backend.database.structure.helper;

import java.time.Instant;

public record AssignmentEvent(String status, Instant timestamp, String narrative) {
}