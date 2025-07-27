package net.noahf.firewatch.common.incidents;

import net.noahf.firewatch.common.agency.Agency;
import net.noahf.firewatch.common.units.Unit;

import java.util.ArrayList;
import java.util.List;

public class IncidentAgency {

    private final Agency agency;
    private final List<Unit> units;

    IncidentAgency(Agency agency) {
        this.agency = agency;
        this.units = new ArrayList<>();
    }

    public Agency agency() { return this.agency; }
    public List<Unit> attachedUnits() { return this.units; }

}
