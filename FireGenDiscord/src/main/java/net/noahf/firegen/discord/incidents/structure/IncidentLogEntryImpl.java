package net.noahf.firegen.discord.incidents.structure;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.noahf.firegen.api.Contributor;
import net.noahf.firegen.api.incidents.IncidentLogEntry;
import net.noahf.firegen.api.utilities.IdGenerator;
import net.noahf.firegen.discord.users.FireGenUser;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@NoArgsConstructor(force = true)
@Getter
@Entity
public class IncidentLogEntryImpl implements IncidentLogEntry {

    private static final String NARRATIVE_TIME_FORMAT = "HH:mm";

    @Id
    private final long id;

    private final LocalDateTime time;

    @OneToOne(cascade = CascadeType.ALL)
    private final FireGenUser user;

    private final String entry;

    @Setter
    @Enumerated
    private IncidentLogEntry.EntryType type;

    IncidentLogEntryImpl(LocalDateTime time, Contributor<?> user, String entry, IncidentLogEntry.EntryType type) {
        if (!(user instanceof FireGenUser fireGenUser)) {
            throw new IllegalArgumentException("Expected user to be of type " + FireGenUser.class + ": " + (user != null ? user.toString() : "<null>"));
        }

        this.id = IdGenerator.generateNarrativeId(this);
        this.time = time;
        this.user = fireGenUser;
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