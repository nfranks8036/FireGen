package net.noahf.firegen.api.incidents;

import net.noahf.firegen.api.Identifiable;
import net.noahf.firegen.api.Contributor;

import java.time.LocalDateTime;

public interface IncidentLogEntry extends Identifiable {

    LocalDateTime getTime();

    Contributor getUser();

    String getEntry();

    EntryType getType();

    default boolean isNarrative() {
        return switch (this.getType()) {
            case UPDATE -> false;
            case NARRATIVE, HIDDEN -> true;
        };
    }


    enum EntryType {
        UPDATE,
        NARRATIVE,
        HIDDEN
    }

}
