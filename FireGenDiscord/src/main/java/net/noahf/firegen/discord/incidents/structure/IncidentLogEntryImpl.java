package net.noahf.firegen.discord.incidents.structure;

import lombok.Getter;
import lombok.Setter;
import net.noahf.firegen.api.Contributor;
import net.noahf.firegen.api.incidents.IncidentLogEntry;
import net.noahf.firegen.api.utilities.IdGenerator;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class IncidentLogEntryImpl implements IncidentLogEntry {

    private static final String NARRATIVE_TIME_FORMAT = "HH:mm";

    private final @Getter long id;
    private final @Getter LocalDateTime time;
    private final @Getter Contributor user;
    private final @Getter String entry;

    private @Getter @Setter IncidentLogEntry.EntryType type;

    IncidentLogEntryImpl(LocalDateTime time, Contributor user, String entry, IncidentLogEntry.EntryType type) {
        this.id = IdGenerator.generateNarrativeId(this);
        this.time = time;
        this.user = user;
        this.entry = entry.toUpperCase()
                .strip()
                .replace("\n", "") // don't allow newLine characters
                .replace("*", "\\*") // remove Discord formatting involving *
                .replace("_", "\\_"); // remove Discord formatting involving _
        this.type = type;
    }

    public String formatReceiver() {
        return "`" + this.time.format(DateTimeFormatter.ofPattern(NARRATIVE_TIME_FORMAT)) + "` " + entry;
    }

    public String formatAdmin() {
        return "`" + this.time.format(DateTimeFormatter.ofPattern("HH:mm:ss")) + "` `"+ type.name() + "` <@" + user.getId() + "> " + entry;
    }

}