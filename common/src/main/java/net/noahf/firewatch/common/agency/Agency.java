package net.noahf.firewatch.common.agency;

import net.noahf.firewatch.common.units.Unit;

public class Agency {

    private Unit[] units;
    private String name;
    private String abbreviation;

    public Agency(String name, String abbreviation) {
        this.name = name;
        this.abbreviation = abbreviation;
    }

    public String name() { return this.name; }
    public String abbreviation() { return this.abbreviation; }

}
