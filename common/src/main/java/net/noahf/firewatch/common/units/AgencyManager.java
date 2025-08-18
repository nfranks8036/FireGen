package net.noahf.firewatch.common.units;

import net.noahf.firewatch.common.data.units.UnitStatus;

import java.util.ArrayList;
import java.util.List;

public class AgencyManager {

    private final List<Agency> agencies;
    private final List<Unit> units;

    public AgencyManager(List<Agency> agencies) {
        this.agencies = new ArrayList<>(agencies);

        this.units = new ArrayList<>();
        for (Agency agency : agencies) {
            for (Unit unit : agency.units()) {
                unit.initialize(agency);
                this.units.add(unit);
            }
        }

    }

    public List<Unit> findAvailableUnits() {
        return this.units
                .stream()
                .filter(u -> u.assignment() == null)
                .toList();
    }

    public List<Unit> findAllUnits() {
        return this.units;
    }

    public List<Unit> findUnitsByStatus(UnitStatus status) {
        return this.units.stream()
                .filter(unit -> unit.operation().equals(status) || (unit.assignment() != null && unit.assignment().equals(status)))
                .toList();
    }

    public Unit findUnitByCallsign(String callsign) {
        return this.units.stream()
                .filter(unit -> unit.matches(callsign))
                .findFirst()
                .orElse(null);
    }

    public Unit findUnitByCallsign(String callsign, boolean abbreviated, boolean space) {
        return this.units.stream()
                .filter(unit -> unit.callsign(abbreviated, space).equalsIgnoreCase(callsign))
                .findFirst()
                .orElse(null);
    }

    public List<Agency> findAgencies() { return this.agencies; }

    public Agency findAgencyByName(String name) {
        return this.agencies.stream().filter(a -> a.name().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public Agency findAgencyByAbbreviation(String abbreviation) {
        return this.agencies.stream().filter(a -> a.abbreviation().equalsIgnoreCase(abbreviation)).findFirst().orElse(null);
    }

}
