package net.noahf.firegen.api.incidents;

import net.noahf.firegen.api.utilities.FireGenVariables;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public interface IncidentTime {

    LocalDateTime getDateTime();

    void setDate(LocalDate date, LocalTime time);

    void setTime(LocalTime time);

    default long getUnix() {
        return this.getDateTime().toEpochSecond(OffsetDateTime.now().getOffset());
    }

    default LocalDate getDate() {
        return this.getDateTime().toLocalDate();
    }

    default LocalTime getTime() {
        return this.getDateTime().toLocalTime();
    }

    default String formatTimeLong(FireGenVariables vars) {
        return this.getDateTime().format(DateTimeFormatter.ofPattern(vars.longTimeFormat()));
    }

    default String formatTimeShort(FireGenVariables vars) {
        return this.getDateTime().format(DateTimeFormatter.ofPattern(vars.shortTimeFormat()));
    }

    default String formatDate(FireGenVariables vars) {
        return this.getDateTime().format(DateTimeFormatter.ofPattern(vars.dateFormat()));
    }

}
