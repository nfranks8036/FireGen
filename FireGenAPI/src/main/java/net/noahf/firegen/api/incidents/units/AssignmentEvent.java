package net.noahf.firegen.api.incidents.units;

import java.time.LocalDateTime;

public interface AssignmentEvent {

    String getStatus();

    LocalDateTime getTime();

    String getNarrative();

}
