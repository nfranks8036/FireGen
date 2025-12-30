package net.noahf.firegen.backend.database.structure;

import dev.morphia.annotations.Entity;
import net.noahf.firegen.backend.database.structure.helper.IncidentLogType;

import java.time.Instant;

@Entity
public class IncidentLogEntry {

    public Instant time;
    public String text;

    public IncidentLogType logType;

}
