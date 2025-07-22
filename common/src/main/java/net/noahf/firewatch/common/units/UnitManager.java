package net.noahf.firewatch.common.units;

import net.noahf.firewatch.common.agency.Agency;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UnitManager {

    private final List<Unit> allUnits;

    public UnitManager() {
        this.allUnits = new ArrayList<>();

        Agency blacksburgFire = new Agency("Blacksburg Fire", "BFD");
        this.allUnits.addAll(List.of(
                new Unit(19, UnitType.FIRE_ENGINE, UnitStatus.IN_SERVICE, blacksburgFire),
                new Unit(17, UnitType.FIRE_ENGINE, UnitStatus.IN_SERVICE, blacksburgFire),
                new Unit(13, UnitType.FIRE_ENGINE, UnitStatus.IN_SERVICE, blacksburgFire),
                new Unit(12, UnitType.FIRE_ENGINE, UnitStatus.IN_SERVICE, blacksburgFire),
                new Unit(11, UnitType.FIRE_ENGINE, UnitStatus.IN_SERVICE, blacksburgFire),
                new Unit(10, UnitType.FIRE_ENGINE, UnitStatus.IN_SERVICE, blacksburgFire),

                new Unit(12, UnitType.FIRE_LADDER, UnitStatus.IN_SERVICE, blacksburgFire),
                new Unit(11, UnitType.FIRE_LADDER, UnitStatus.IN_SERVICE, blacksburgFire),

                new Unit(13, UnitType.FIRE_TANKER, UnitStatus.IN_SERVICE, blacksburgFire),
                new Unit(12, UnitType.FIRE_TANKER, UnitStatus.IN_SERVICE, blacksburgFire),
                new Unit(11, UnitType.FIRE_TANKER, UnitStatus.IN_SERVICE, blacksburgFire),

                new Unit(13, UnitType.FIRE_BRUSH, UnitStatus.IN_SERVICE, blacksburgFire),
                new Unit(12, UnitType.FIRE_BRUSH, UnitStatus.IN_SERVICE, blacksburgFire),
                new Unit(11, UnitType.FIRE_BRUSH, UnitStatus.IN_SERVICE, blacksburgFire),

                new Unit(11, UnitType.FIRE_ATTACK, UnitStatus.IN_SERVICE, blacksburgFire),

                new Unit(13, UnitType.FIRE_RESPONSE, UnitStatus.IN_SERVICE, blacksburgFire),
                new Unit(12, UnitType.FIRE_RESPONSE, UnitStatus.IN_SERVICE, blacksburgFire),
                new Unit(11, UnitType.FIRE_RESPONSE, UnitStatus.IN_SERVICE, blacksburgFire),

                new Unit(12, UnitType.FIRE_AIR, UnitStatus.IN_SERVICE, blacksburgFire),

                new Unit(11, UnitType.FIRE_HAZMAT, UnitStatus.IN_SERVICE, blacksburgFire)
        ));
    }

    public List<Unit> match(String word, UnitStatus... statuses) {
        List<Unit> matches = new ArrayList<>();
        for (Unit u : this.getAllUnits(statuses)) {
            if (u.callsign().primaryCallsign().contains(word)) {
                matches.add(u);
            } else if (u.callsign().fullCallsign().contains(word)) {
                matches.add(u);
            } else if (word.contains(String.valueOf(u.callsign().unitNumber()))) {
                matches.add(u);
            }
        }

        return matches;
    }

    public List<Unit> getAllUnits(UnitStatus... statuses) {
        if (statuses.length == 0) {
            return this.allUnits;
        }
        List<Unit> matches = new ArrayList<>();
        for (Unit u : this.allUnits) {
            if (Arrays.stream(statuses).toList().contains(u.status())) {
                matches.add(u);
            }
        }
        return matches;
    }

}
