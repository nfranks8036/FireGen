package net.noahf.firegen.discord.database;

import jakarta.data.repository.*;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import net.noahf.firegen.api.database.IncidentQuery;
import net.noahf.firegen.api.database.IncidentRepository;
import net.noahf.firegen.api.incidents.Incident;
import net.noahf.firegen.api.incidents.types.IncidentType;

import java.util.List;
import java.util.Optional;

@Repository
public interface IncidentDatabase extends IncidentRepository {

    @Override
    @Save
    Incident save(Incident incident);

    @Override
    @Delete
    void delete(long id);

    @Override
    @Delete
    void delete(Incident incident);

    @Override
    default Optional<Incident> findIncidentByNumber(String incidentNumber) {
        final String INVALID_FORM = "Invalid form of incident number. Expected: YYYY-IIIIIIII, where Y is the year and I is the identifier.";

        long id;
        if (!incidentNumber.contains("-")) {
            throw new IllegalArgumentException(INVALID_FORM + " (failed to find '-')");
        }

        String number =  incidentNumber.split("-")[1];
        try {
            id = Long.parseLong(number);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(INVALID_FORM + " (number is not valid '" + number + "')", e);
        }

        return this.findIncidentById(id);
    }

    @Override
    @Find
    Optional<Incident> findIncidentById(@By(value = "id") long id);
}
