package net.noahf.firewatch.common.units;

import net.noahf.firewatch.common.utils.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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


    public List<Agency> findAgencies() { return this.agencies; }

    public Agency findAgencyByName(String name) {
        return this.agencies.stream().filter(a -> a.name().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public Agency findAgencyByAbbreviation(String abbreviation) {
        return this.agencies.stream().filter(a -> a.abbreviation().equalsIgnoreCase(abbreviation)).findFirst().orElse(null);
    }

}
