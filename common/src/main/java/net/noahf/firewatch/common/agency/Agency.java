package net.noahf.firewatch.common.agency;

import net.noahf.firewatch.common.units.Unit;
import net.noahf.firewatch.common.units.UnitStatus;
import net.noahf.firewatch.common.units.UnitType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class Agency {

    public static Agency BLACKSBURG_VOLUNTEER_FIRE_DEPARTMENT = new Agency()
            .name("Blacksburg Volunteer Fire Department")
            .abbreviation("BFD")
            .agencyType(AgencyType.FIRE)
            .units(
                    new Unit(19, UnitType.FIRE_ENGINE),
                    new Unit(17, UnitType.FIRE_ENGINE),
                    new Unit(13, UnitType.FIRE_ENGINE),
                    new Unit(12, UnitType.FIRE_ENGINE),
                    new Unit(11, UnitType.FIRE_ENGINE),
                    new Unit(10, UnitType.FIRE_ENGINE),

                    new Unit(12, UnitType.FIRE_LADDER),
                    new Unit(11, UnitType.FIRE_LADDER),

                    new Unit(13, UnitType.FIRE_TANKER),
                    new Unit(12, UnitType.FIRE_TANKER),
                    new Unit(11, UnitType.FIRE_TANKER),

                    new Unit(13, UnitType.FIRE_BRUSH),
                    new Unit(12, UnitType.FIRE_BRUSH),
                    new Unit(11, UnitType.FIRE_BRUSH),

                    new Unit(11, UnitType.FIRE_ATTACK),

                    new Unit(13, UnitType.FIRE_RESPONSE),
                    new Unit(12, UnitType.FIRE_RESPONSE),
                    new Unit(11, UnitType.FIRE_RESPONSE),

                    new Unit(12, UnitType.FIRE_AIR),

                    new Unit(11, UnitType.FIRE_HAZMAT),

                    new Unit(100, UnitType.FIRE_BATTALION_CHIEF),
                    new Unit(101, UnitType.FIRE_BATTALION_CHIEF),
                    new Unit(102, UnitType.FIRE_BATTALION_CHIEF),
                    new Unit(103, UnitType.FIRE_BATTALION_CHIEF),
                    new Unit(104, UnitType.FIRE_BATTALION_CHIEF),
                    new Unit(105, UnitType.FIRE_BATTALION_CHIEF),
                    new Unit(106, UnitType.FIRE_BATTALION_CHIEF),
                    new Unit(107, UnitType.FIRE_BATTALION_CHIEF),
                    new Unit(108, UnitType.FIRE_BATTALION_CHIEF),
                    new Unit(109, UnitType.FIRE_BATTALION_CHIEF)
            );

    public static Agency BLACKSBURG_VOLUNTEER_RESCUE_SQUAD = new Agency()
            .name("Blacksburg Volunteer Rescue Squad")
            .abbreviation("BVRS")
            .agencyType(AgencyType.EMS)
            .units(
                    new Unit(51, UnitType.EMS_RESCUE),
                    new Unit(52, UnitType.EMS_RESCUE),
                    new Unit(53, UnitType.EMS_RESCUE),
                    new Unit(54, UnitType.EMS_RESCUE),
                    new Unit(55, UnitType.EMS_RESCUE),
                    new Unit(56, UnitType.EMS_RESCUE),

                    new Unit(51, UnitType.EMS_MEDIC),
                    new Unit(52, UnitType.EMS_MEDIC),
                    new Unit(53, UnitType.EMS_MEDIC),
                    new Unit(54, UnitType.EMS_MEDIC),
                    new Unit(55, UnitType.EMS_MEDIC),
                    new Unit(56, UnitType.EMS_MEDIC)
            );

    private Unit[] units;
    private String name;
    private String abbreviation;
    private AgencyType agencyType;

    Agency() {
        this.units = new Unit[]{};
        this.name = "Unspecified-Agency-" + UUID.randomUUID();
        this.abbreviation = this.name.split("-")[2];
        this.agencyType = AgencyType.OTHER;
    }

    public String name() { return this.name; }
    private Agency name(String newName) { this.name = newName; return this; }

    public String abbreviation() { return this.abbreviation; }
    private Agency abbreviation(String newAbbreviation) { this.abbreviation = newAbbreviation; return this; }

    public AgencyType agencyType() { return this.agencyType; }
    public Agency agencyType(AgencyType newAgencyType) { this.agencyType = newAgencyType; return this; }

    public Unit[] units() { return this.units; }
    private Agency units(Unit... units) {
        for (Unit u : units) {
            u.agency(this);
        }
        this.units = units;
        return this;
    }

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
