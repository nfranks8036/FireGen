package net.noahf.firewatch.common.units;

import java.util.List;

public class Agency {

    private String name;
    private String simplified;
    private String abbreviation;
    private String agency_type;
    private List<Unit> units;

    public List<Unit> units() { return this.units; }

    public String name() { return this.name; }
    public String simplified() { return this.simplified; }
    public String abbreviation() { return this.abbreviation; }

}
