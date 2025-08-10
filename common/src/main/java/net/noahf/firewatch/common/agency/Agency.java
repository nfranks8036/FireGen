package net.noahf.firewatch.common.agency;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.noahf.firewatch.common.newincidents.AgencyType;
import net.noahf.firewatch.common.newincidents.IncidentStructure;
import net.noahf.firewatch.common.units.Unit;
import net.noahf.firewatch.common.units.UnitStatus;
import net.noahf.firewatch.common.units.UnitType;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class Agency {

    private Unit[] units;
    private String name;
    private String abbreviation;
    private AgencyType agencyType;

    Agency() {
        this.units = new Unit[]{};
        this.name = "Unspecified-Agency-" + UUID.randomUUID();
        this.abbreviation = this.name.split("-")[2];
        this.agencyType = null;
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
