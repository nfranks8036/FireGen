package net.noahf.firewatch.common.units;

public class Callsign {


    private transient final Unit parentUnit;
    private final int unitNumber;

    Callsign(Unit unit, int unitNumber) {
        this.parentUnit = unit;
        this.unitNumber = unitNumber;
    }

    public String primaryCallsign() {
        String prefix = this.parentUnit.unitType().callsignPrefix();
        if (prefix == null) {
            prefix = this.parentUnit.agency().abbreviation();
        }
        return prefix + this.unitNumber();
    }

    public String fullCallsign() {
        String full = this.parentUnit.unitType().callsignFull();
        if (full == null) {
            full = this.parentUnit.agency().name();
        }
        return full + " " + this.unitNumber();
    }

    public int unitNumber() { return this.unitNumber; }

}
