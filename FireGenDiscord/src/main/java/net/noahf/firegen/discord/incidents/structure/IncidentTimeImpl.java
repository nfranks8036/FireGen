package net.noahf.firegen.discord.incidents.structure;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.noahf.firegen.api.incidents.IncidentTime;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@AllArgsConstructor
public class IncidentTimeImpl implements IncidentTime {

    private LocalDateTime dateTime;

    @Override
    public void setDate(LocalDate date, LocalTime time) {
        this.dateTime = date.atTime(time);
    }

    @Override
    public void setTime(LocalTime time) {
        this.setDate(LocalDate.now(), time);
    }
}
