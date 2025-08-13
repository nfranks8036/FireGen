package net.noahf.firewatch.common.agency;

import net.noahf.firewatch.common.newincidents.AgencyType;
import net.noahf.firewatch.common.units.Unit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Agency {

    private String name;
    private String simplified;
    private String abbreviation;
    private String agency_type;
    private List<Unit> units;

    private AgencyType agencyType;



    public List<Unit> matchUnits(String match, UnitStatus... statuses) {
        List<Unit> matches = new ArrayList<>();
        for (Unit u : this.matchUnits(statuses)) {
            if (u.matches(match)) {
                matches.add(u);
            }
        }
        return matches;
    }

    public List<Unit> matchUnits(UnitStatus... statuses) {
        if (statuses == null || statuses.length == 0) {
            return Arrays.stream(this.units).toList();
        }
        List<Unit> matches = new ArrayList<>();
        for (Unit u : this.units) {
            if (Arrays.stream(statuses).toList().contains(u.status())) {
                matches.add(u);
            }
        }
        return matches;
    }

}
