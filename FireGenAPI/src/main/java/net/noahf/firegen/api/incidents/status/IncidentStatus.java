package net.noahf.firegen.api.incidents.status;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.noahf.firegen.api.utilities.IgnoreStringSelector;
import net.noahf.firegen.api.utilities.StringSelectors;

import java.util.List;

/**
 * Represents the current incident status of an {@link net.noahf.firegen.api.incidents.Incident}
 */
@AllArgsConstructor @Getter
public enum IncidentStatus implements StringSelectors {

    /**
     * Represents an Incident which is Pending (no units attached OR not dispatched yet)
     */
    PENDING("PND"),

    /**
     * Represents an Incident which is Active (units attached and dispatched)
     */
    ACTIVE("ACT"),

    /**
     * Represents an Incident which is Stale (no changes have been made in *X* time)
     */
    STALE("STA"),

    /**
     * Represents an Incident which is Closed (an incident in the past, has been resolved)
     */
    CLOSED("CLO");

    private final String shortName;

    /**
     * @return returns {@code true} if the Incident Status is in progress or {@code false} if it is not active
     */
    public boolean isInProgress() {
        return this != CLOSED;
    }

    /**
     * @return returns the opposite of what the current incident is (if {@code ACTIVE} or {@code PENDING} then
     *         it will return {@code CLOSED}, if {@code CLOSED} or {@code CLOSED_TIMED_OUT} it will return
     *         {@code PENDING})
     */
    @IgnoreStringSelector
    public IncidentStatus opposite() {
        return this != CLOSED ? CLOSED : PENDING;
    }

    @Override
    public List<String> asStringSelectors() {
        return List.of(name(), isInProgress() ? "statusInProgress" : "statusNotInProgress");
    }

}
