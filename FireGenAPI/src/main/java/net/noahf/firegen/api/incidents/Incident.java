package net.noahf.firegen.api.incidents;

import net.noahf.firegen.api.Contributor;
import net.noahf.firegen.api.Identifiable;
import net.noahf.firegen.api.incidents.location.IncidentLocation;
import net.noahf.firegen.api.incidents.status.IncidentStatus;
import net.noahf.firegen.api.incidents.units.Unit;

import java.util.List;

public interface Incident extends Identifiable {

    IncidentStatus getStatus();

    void setStatus(IncidentStatus status);

    String getFormattedId();

    IncidentTime getTime();

    IncidentLocation getLocation();

    void setLocation(IncidentLocation location);

    IncidentType getType();

    void setType(IncidentType type);

    void addLog(IncidentLogEntry entry);

    void addLog(Contributor<?> account, IncidentLogEntry.EntryType type, String narrative);

    void injectLog(IncidentLogEntry entry);

    List<IncidentLogEntry> getLog();

    List<Unit> getAttachedUnits();

    List<Contributor<?>> getContributors();

    void addContributor(Contributor<?> contributor);

    IncidentPublishedStatus getPublished();

    void setPublished(IncidentPublishedStatus newStatus);

    void update();

}
