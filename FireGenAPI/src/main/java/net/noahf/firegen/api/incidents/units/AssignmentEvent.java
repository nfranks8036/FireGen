package net.noahf.firegen.api.incidents.units;

import java.time.LocalDateTime;

public interface AssignmentEvent {

    String getName();

    String getShortName();

    LocalDateTime getTime();

    String getNarrative();

    int ordinal();

}
