package net.noahf.firegen.api.incidents.status;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.noahf.firegen.api.incidents.Incident;

@AllArgsConstructor @Getter
public enum IncidentStatus {

    PENDING("PND"),

    ACTIVE("ACT"),

    CLOSED("CLO"),

    CLOSED_TIMED_OUT("CTO");

    private final String shortName;

    public boolean isInProgress() {
        return switch (this) {
            case PENDING, ACTIVE -> true;
            case CLOSED, CLOSED_TIMED_OUT -> false;
        };
    }

    public IncidentStatus opposite() {
        return switch (this) {
            case ACTIVE, PENDING -> CLOSED;
            case CLOSED, CLOSED_TIMED_OUT -> PENDING;
        };
    }

}
