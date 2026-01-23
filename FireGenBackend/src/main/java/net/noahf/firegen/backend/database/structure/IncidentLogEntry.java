package net.noahf.firegen.backend.database.structure;

import dev.morphia.annotations.Entity;
import net.noahf.firegen.backend.database.structure.helper.IncidentLogType;

import java.time.Instant;

@Entity
public class IncidentLogEntry {

    public static IncidentLogEntry of(IncidentLogType logType, String text) {
        IncidentLogEntry entry = new IncidentLogEntry();
        entry.time = Instant.now();
        entry.text = text;
        entry.type = logType;
        return entry;
    }

    public Instant time;
    public String text;

    public IncidentLogType type;

}
