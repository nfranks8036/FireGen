package net.noahf.firewatch.common.units;

import net.noahf.firewatch.common.agency.Agency;

public class Unit {

    private final Callsign callsign;
    private final UnitType type;

    private Agency agency;
    private UnitStatus status;

    public Unit(int numberDesignation, UnitType type) {
        this.callsign = new Callsign(this, numberDesignation);
        this.type = type;
        this.status = UnitStatus.OUT_OF_SERVICE;
    }

    public Callsign callsign() { return this.callsign; }
    public UnitType unitType() { return this.type; }

    public UnitStatus status() { return this.status; }
    public void status(UnitStatus newStatus) { this.status = newStatus; }

    public Agency agency() { return this.agency; }
    public void agency(Agency newAgency) { this.agency = newAgency; }

    public boolean matches(String text) {
        if (this.callsign().primaryCallsign().contains(text)) {
            return true;
        } else if (this.callsign().fullCallsign().contains(text)) {
            return true;
        } else return false;
    }

}
