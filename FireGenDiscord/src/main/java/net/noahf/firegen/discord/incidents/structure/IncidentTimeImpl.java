package net.noahf.firegen.discord.incidents.structure;

import lombok.Getter;
import lombok.NoArgsConstructor;
import net.noahf.firegen.api.incidents.IncidentTime;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@NoArgsConstructor
public class IncidentTimeImpl implements IncidentTime {

    public IncidentTimeImpl(LocalDateTime time) {
        this.dateTime = time;
    }

    private long id = 0L;

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
