package net.noahf.firegen.api.incidents;

import lombok.Getter;
import net.noahf.firegen.api.utilities.AutofilledCharSequence;
import org.jetbrains.annotations.NotNull;

@Getter
public enum IncidentStatus implements AutofilledCharSequence {

    ACTIVE("This incident is considered active, which means there are units attached, en-route, and/or actively on-scene."),

    PENDING("This incident is considered pending, which means there are no current units attached or active at this scene."),

    CLOSED("This incident is considered closed, which means no other changes will be made to this incident."),

    TIMED_OUT("This incident is considered timed out, which means no changes were made to this incident, meaning it's likely closed.");


    private final String description;

    IncidentStatus(String description) {
        this.description = description;
    }

    public boolean isInProgress() {
        return this == ACTIVE || this == PENDING;
    }

    public IncidentStatus opposite(Incident incident) {
        return switch (this) {
            case ACTIVE, PENDING -> CLOSED;
            case CLOSED, TIMED_OUT -> {
                if (incident.getAttachedAgencies().isEmpty()) {
                    yield ACTIVE;
                } else {
                    yield PENDING;
                }
            }
        };
    }

    @Override @NotNull
    public String toString() {
        return this.name().replace("_", " ");
    }
}
