package net.noahf.firegen.api.database;

import net.noahf.firegen.api.incidents.Incident;
import net.noahf.firegen.api.incidents.types.IncidentType;
import net.noahf.firegen.api.incidents.status.IncidentStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface IncidentRepository {

    Incident save(Incident incident);

    void delete(long id);

    void delete(Incident incident);

    Optional<Incident> findIncidentByNumber(String incidentNumber);

    Optional<Incident> findIncidentById(long id);

    List<Incident> search(IncidentQuery query);

    List<Incident> findOpenIncidents();

    List<Incident> findByType(IncidentType type);

    default List<Incident> findByStatus(IncidentStatus status) {
        return this.search(IncidentQuery.search().byStatus(status).finish());
    }

    default List<Incident> findRecent(int limit) {
        return this.search(IncidentQuery.search().finish()).stream().limit(limit).toList();
    }

    default List<Incident> findBetween(LocalDateTime start, LocalDateTime end) {
        return this.search(IncidentQuery.search().byStartTime(start).byEndTime(end).finish());
    }

}
