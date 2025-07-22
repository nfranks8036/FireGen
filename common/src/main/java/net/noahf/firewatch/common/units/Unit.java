package net.noahf.firewatch.common.units;

import net.noahf.firewatch.common.agency.Agency;

public class Unit {

    private final Callsign callsign;
    private final UnitType type;
    private final UnitStatus status;

    private final Agency agency;

    public Unit(int numberDesignation, UnitType type, UnitStatus initialStatus, Agency agency) {
        this.callsign = new Callsign(this, numberDesignation);
        this.type = type;
        this.status = initialStatus;

        this.agency = agency;
    }

    public Callsign callsign() { return this.callsign; }
    public UnitType unitType() { return this.type; }
    public UnitStatus status() { return this.status; }
    public Agency agency() { return this.agency; }

}
