package net.noahf.firegen.discord.database;

import jakarta.data.repository.*;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import net.noahf.firegen.api.database.IncidentQuery;
import net.noahf.firegen.api.database.IncidentRepository;
import net.noahf.firegen.api.incidents.Incident;
import net.noahf.firegen.api.incidents.status.IncidentStatus;
import net.noahf.firegen.api.incidents.types.IncidentType;
import net.noahf.firegen.discord.incidents.structure.IncidentImpl;

import java.util.List;
import java.util.Optional;

@Repository @Transactional
public interface IncidentDatabase {

    @Save
    IncidentImpl save(IncidentImpl incident);

    @Delete
    void delete(long id);

    @Delete
    void delete(IncidentImpl incident);

    @Find
    Optional<IncidentImpl> findIncidentById(long id);

    @Find
    List<IncidentImpl> findByStatus(IncidentStatus status);


    default Optional<IncidentImpl> findIncidentByNumber(String incidentNumber) {
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

}
