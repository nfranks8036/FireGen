package net.noahf.firegen.api.incidents;

import net.noahf.firegen.api.Identifiable;
import net.noahf.firegen.api.Contributor;
import net.noahf.firegen.api.incidents.units.Agency;
import net.noahf.firegen.api.incidents.units.Unit;

import java.util.List;

public interface Incident extends Identifiable {

    IncidentStatus getStatus();

    void setStatus(IncidentStatus status);

    String getFormattedId();

    IncidentTime getTime();

    void addLog(IncidentLogEntry entry);

    void addLog(Contributor account, IncidentLogEntry.EntryType type, String narrative);

    void injectLog(IncidentLogEntry entry);

    List<IncidentLogEntry> getLog();

    List<IncidentLogEntry> getNarrative();

    List<Agency> getAttachedAgencies();

    List<Unit> getAttachedUnits();

    List<Contributor> getContributors();

    void addContributor(Contributor userAccount);



}
