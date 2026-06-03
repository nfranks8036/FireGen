package net.noahf.firegen.discord.database;

import net.noahf.firegen.api.database.IncidentQuery;
import net.noahf.firegen.api.database.IncidentRepository;
import net.noahf.firegen.api.incidents.Incident;
import net.noahf.firegen.api.incidents.types.IncidentType;

import java.util.List;
import java.util.Optional;

public class IncidentDatabase implements IncidentRepository {
    @Override
    public Incident save(Incident incident) {
        return null;
    }

    @Override
    public List<Incident> save(Incident... incidents) {
        return List.of();
    }

    @Override
    public void delete(long id) {

    }

    @Override
    public void delete(Incident incident) {

    }

    @Override
    public Optional<Incident> findIncidentByNumber(String incidentNumber) {
        return Optional.empty();
    }

    @Override
    public Optional<Incident> findIncidentById(int id) {
        return Optional.empty();
    }

    @Override
    public List<Incident> search(IncidentQuery query) {
        return List.of();
    }

    @Override
    public List<Incident> findOpenIncidents() {
        return List.of();
    }

    @Override
    public List<Incident> findByType(IncidentType type) {
        return List.of();
    }
}
