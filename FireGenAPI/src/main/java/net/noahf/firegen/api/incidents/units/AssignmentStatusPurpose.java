package net.noahf.firegen.api.incidents.units;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter @AllArgsConstructor
public enum AssignmentStatusPurpose {

    UNIT_CLEAR(true),

    UNIT_REMOVED(true),

    UNIT_ATTACHED(false);

    private final boolean terminalState;

}
