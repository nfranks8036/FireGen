package net.noahf.firewatch.common.agency;

import net.noahf.firewatch.common.units.Unit;
import net.noahf.firewatch.common.units.UnitStatus;

import java.util.ArrayList;
import java.util.List;

public class AgencyManager {

    private final List<Agency> agencies;

    public AgencyManager() {
        this.agencies = new ArrayList<>();
    }

    public List<Agency> agencies() { return this.agencies; }

    public List<Unit> allUnits(String match, UnitStatus... statuses) {
        List<Unit> units = new ArrayList<>();
        for (Agency agency : this.agencies) {
            units.addAll(agency.matchUnits(match, statuses));
        }
        return units;
    }

    public List<Unit> allUnits(UnitStatus... statuses) {
        List<Unit> units = new ArrayList<>();
        for (Agency agency : this.agencies) {
            units.addAll(agency.matchUnits(statuses));
        }
        return units;
    }

    public Agency findAgency(String name) {
        for (Agency agency : this.agencies) {
            if (agency.name().equalsIgnoreCase(name)) {
                return agency;
            }
            if (agency.abbreviation().equalsIgnoreCase(name)) {
                return agency;
            }
        }
        return null;
    }

}
