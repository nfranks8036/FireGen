package net.noahf.firegen.discord.incidents.structure;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.noahf.firegen.api.Contributor;
import net.noahf.firegen.api.incidents.IncidentLogEntry;
import net.noahf.firegen.api.utilities.IdGenerator;
import net.noahf.firegen.discord.users.FireGenUser;
import net.noahf.firegen.discord.utilities.ImmutablePair;
import net.noahf.firegen.discord.utilities.Log;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@NoArgsConstructor(force = true)
@Getter
@Entity
public class IncidentLogEntryImpl implements IncidentLogEntry {

    public static IncidentLogEntry of(Contributor<?> user, String entry, EntryType type) {
        return new IncidentLogEntryImpl(LocalDateTime.now(), user, entry, type);
    }

    private static final String NARRATIVE_TIME_FORMAT = "HH:mm";

    @Id
    private final long id;

    private final LocalDateTime time;

    @OneToOne(cascade = CascadeType.ALL)
    private final FireGenUser user;

    private String entry;

    @Setter
    @Enumerated
    private IncidentLogEntry.EntryType type;

    private LocalDateTime customTime;

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
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(NARRATIVE_TIME_FORMAT);
        return "`" + this.getCustomTimeOrDefault().format(formatter) + "` " + this.entry.replaceFirst("^<[^>]+>\\s+", "");
    }

    public String formatAdmin() {
        return "`" + this.time.format(DateTimeFormatter.ofPattern("HH:mm:ss")) + "` `"+ type.name() + "` <@" + user.getId() + "> " + entry;
    }

    @Override
    public int compareTo(@NotNull IncidentLogEntry o) {
        if (this.customTime == null) {
            return this.getTime().compareTo(o.getTime());
        }
        return this.getCustomTime().compareTo(((IncidentLogEntryImpl)o).getCustomTime());
    }

    public LocalDateTime getCustomTimeOrDefault() {
        if (this.customTime != null) {
            return this.customTime;
        }

        if (!this.entry.startsWith("T") && !this.entry.startsWith("D")) {
            return this.time;
        }

        ImmutablePair<LocalDateTime, String> time = this.extractTime(this.entry);
        this.customTime = time.getFirstElement();
        this.entry = "<" +
                (this.customTime.toLocalDate().isEqual(LocalDate.now()) ? "" :
                        this.customTime.format(DateTimeFormatter.ofPattern("MM/dd/yyyy")) + "@"
                ) +
                this.customTime.format(DateTimeFormatter.ofPattern("HH:mm")) + "> " +
                time.getSecondElement();

        return this.customTime;
    }

    private final Pattern pattern = Pattern.compile(
            "^(?:D(?<month>\\d{2})(?<day>\\d{2})(?<year>\\d{2}))?T(?<hour>\\d{2})(?<minute>\\d{2})\\s*"
    );

    private ImmutablePair<LocalDateTime, String> extractTime(String text) {
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            int hour = Integer.parseInt(matcher.group("hour"));
            int minute = Integer.parseInt(matcher.group("minute"));

            LocalDateTime time = LocalDate.now().atTime(hour, minute);
            if (matcher.group("month") != null) {
                int month = Integer.parseInt(matcher.group("month"));
                int day = Integer.parseInt(matcher.group("day"));
                int year = 2_000 + Integer.parseInt(matcher.group("year"));
                time = LocalDate.of(year, month, day).atTime(hour, minute);
            }

            text = text.substring(matcher.end()).stripLeading();
            return ImmutablePair.of(time, text);
        }
        return ImmutablePair.of(LocalDateTime.now(), text);
    }

}