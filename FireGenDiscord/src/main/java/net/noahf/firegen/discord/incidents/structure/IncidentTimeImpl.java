package net.noahf.firegen.discord.incidents.structure;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dv8tion.jda.api.components.selections.SelectOption;
import net.noahf.firegen.api.incidents.IncidentTime;
import net.noahf.firegen.api.incidents.units.AgencyType;
import net.noahf.firegen.api.incidents.units.Unit;
import net.noahf.firegen.api.utilities.IdGenerator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

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
