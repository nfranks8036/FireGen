package net.noahf.firegen.api.incidents;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public interface IncidentTime {

    LocalDateTime getDateTime();

    void setDate(LocalDate date, LocalTime time);

    void setTime(LocalTime time);

}
