package net.noahf.firegen.api.incidents;

import net.noahf.firegen.api.Contributor;
import net.noahf.firegen.api.Identifiable;

import java.time.LocalDateTime;

public interface IncidentLogEntry extends Identifiable {

    LocalDateTime getTime();

    Contributor getUser();

    String getEntry();

    EntryType getType();

    void setType(EntryType newType);

    default boolean isNarrative() {
        return this.getType() == EntryType.NARRATIVE ||
                this.getType() == EntryType.HIDDEN;
    }


    enum EntryType {
        CREATE,
        UPDATE,
        UNIT,
        NARRATIVE,
        HIDDEN
    }

}
